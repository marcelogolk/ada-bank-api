package br.com.ada.quarkus.resource.account;

import br.com.ada.quarkus.model.Account;
import br.com.ada.quarkus.model.LoggedUser;
import br.com.ada.quarkus.model.Transaction;
import br.com.ada.quarkus.resource.transaction.TransactionResponse;
import br.com.ada.quarkus.resource.transaction.TransactionResponseMapper;
import br.com.ada.quarkus.service.AccountService;
import br.com.ada.quarkus.service.CurrentUserService;
import br.com.ada.quarkus.service.CustomerService;
import br.com.ada.quarkus.service.TransactionService;
import br.com.ada.quarkus.util.OutputMaskFormatter;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

/**
 * Recurso responsável pelos endpoints de contas bancárias.
 *
 * <p>Gerencia operações relacionadas a contas, incluindo:
 * criação, consulta, depósito, saque e transferência.</p>
 *
 * <p>Aplica regras de segurança baseadas em papéis:
 * <ul>
 *     <li>GERENTE: acesso completo</li>
 *     <li>CLIENTE: acesso restrito às próprias contas</li>
 * </ul>
 * </p>
 *
 * @author Marcelo
 * @version 2.0
 */
@Path("/contas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    AccountService accountService;

    @Inject
    CurrentUserService currentUserService;

    @Inject
    CustomerService customerService;

    @Inject
    TransactionService transactionService;

    @Inject
    TransactionResponseMapper transactionResponseMapper;

    /**
     * Cria uma nova conta bancária.
     *
     * <p>Apenas usuários com papel GERENTE podem criar contas.</p>
     *
     * <p>O número da conta é gerado automaticamente pelo sistema,
     * incluindo o dígito verificador.</p>
     *
     * @param request dados da conta a ser criada.
     * @param uriInfo informações da URI da requisição.
     * @return resposta HTTP 201 contendo a conta criada.
     */
    @POST
    @Transactional
    @RolesAllowed("GERENTE")
    public Response create(
            @Valid CreateAccountRequest request,
            @Context UriInfo uriInfo) {

        Account account = accountService.create(toAccount(request));
        AccountResponse response = toResponse(account);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(response.id().toString())
                .build();

        return Response.created(location)
                .entity(response)
                .build();
    }

    /**
     * Busca uma conta pelo identificador.
     *
     * <p>Retorna dados completos da conta, incluindo:
     * saldo, titular e transações recentes.</p>
     *
     * <p>Apenas o proprietário da conta ou um gerente podem acessar.</p>
     *
     * @param id identificador da conta.
     * @return dados detalhados da conta.
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public AccountDetailsResponse findById(@PathParam("id") Long id) {

        if (id == null) {
            throw new BadRequestException("O ID da conta é obrigatório");
        }

        Account account = accountService.findById(id);
        validateAccountOwnership(account);

        var customer = customerService.findById(account.getCustomerId());

        CustomerSummaryResponse holder =
                new CustomerSummaryResponse(
                        customer.getId(),
                        customer.getName(),
                        customer.getEmail()
                );

        var todayTransactions =
                transactionService
                        .listTodayByAccountId(id, 0, 10)
                        .content()
                        .stream()
                        .map(transactionResponseMapper::toResponse)
                        .toList();

        AccountLinksResponse links =
                new AccountLinksResponse(
                        "/transacoes?accountId=" + id
                );

        return new AccountDetailsResponse(
                account.getId(),
                OutputMaskFormatter.formatAccountNumber(account.getAccountNumber()),
                account.getType(),
                account.getBalance(),
                holder,
                todayTransactions,
                links
        );
    }

    /**
     * Realiza um depósito em uma conta.
     *
     * <p>Endpoint público (não requer autenticação).</p>
     *
     * <p>A validação da conta e do valor é feita na camada de serviço.</p>
     *
     * @param id identificador da conta.
     * @param request dados do depósito.
     * @return dados da transação realizada.
     */
    @POST
    @Path("/{id}/deposito")
    @Transactional
    @PermitAll
    public TransactionResponse deposit(
            @PathParam("id") Long id,
            @Valid DepositRequest request) {

        Transaction transaction = accountService.deposit(id, request.amount());
        return transactionResponseMapper.toResponse(transaction);
    }

    /**
     * Realiza um saque em uma conta.
     *
     * <p>Somente o proprietário da conta ou um gerente podem realizar saques.</p>
     *
     * <p>Regras de negócio:
     * <ul>
     *     <li>Conta ELETRONICA não permite saque</li>
     *     <li>Deve haver saldo suficiente</li>
     * </ul>
     * </p>
     *
     * @param id identificador da conta.
     * @param request dados do saque.
     * @return dados da transação realizada.
     */
    @POST
    @Path("/{id}/saque")
    @Transactional
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public TransactionResponse withdraw(
            @PathParam("id") Long id,
            @Valid WithdrawRequest request) {

        Account account = accountService.findById(id);
        validateAccountOwnership(account);

        Transaction transaction = accountService.withdraw(id, request.amount());
        return transactionResponseMapper.toResponse(transaction);
    }

    /**
     * Realiza uma transferência entre contas.
     *
     * <p>Somente o proprietário da conta de origem ou um gerente podem transferir.</p>
     *
     * <p>Regras de negócio:
     * <ul>
     *     <li>Saldo suficiente</li>
     *     <li>Contas não podem ser iguais</li>
     * </ul>
     * </p>
     *
     * @param id identificador da conta de origem.
     * @param request dados da transferência.
     * @return dados da transação realizada.
     */
    @POST
    @Path("/{id}/transferencia")
    @Transactional
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public TransactionResponse transfer(
            @PathParam("id") Long id,
            @Valid TransferRequest request) {

        Account sourceAccount = accountService.findById(id);
        validateAccountOwnership(sourceAccount);

        Transaction transaction = accountService.transfer(
                id,
                request.destinationAccountId(),
                request.amount()
        );

        return transactionResponseMapper.toResponse(transaction);
    }

    /**
     * Valida se o usuário logado pode acessar a conta.
     *
     * <p>Gerentes têm acesso total.
     * Clientes só podem acessar suas próprias contas.</p>
     *
     * @param account conta a ser validada.
     */
    private void validateAccountOwnership(Account account) {
        LoggedUser currentUser = currentUserService.getLoggedUser();

        if (currentUser.isManager()) {
            return;
        }

        if (!currentUser.id().equals(account.getCustomerId())) {
            throw new ForbiddenException(
                    "Acesso negado: apenas o proprietário da conta ou um gerente pode realizar esta operação"
            );
        }
    }

    /**
     * Converte entidade Account para AccountResponse.
     *
     * @param account entidade de conta.
     * @return DTO de resposta.
     */
    private AccountResponse toResponse(Account account) {
        return AccountResponse.fromEntity(account);
    }

    /**
     * Converte CreateAccountRequest para entidade Account.
     *
     * @param request dados recebidos na criação.
     * @return entidade Account.
     */
    private Account toAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setType(request.type());
        account.setCustomerId(request.customerId());
        return account;
    }
}