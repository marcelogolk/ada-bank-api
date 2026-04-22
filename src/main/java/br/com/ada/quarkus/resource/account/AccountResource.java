package br.com.ada.quarkus.resource.account;

import br.com.ada.quarkus.model.Account;
import br.com.ada.quarkus.model.LoggedUser;
import br.com.ada.quarkus.model.Transaction;
import br.com.ada.quarkus.resource.PageResponse;
import br.com.ada.quarkus.service.AccountService;
import br.com.ada.quarkus.service.CurrentUserService;
import br.com.ada.quarkus.service.CustomerService;
import br.com.ada.quarkus.service.TransactionService;
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
import br.com.ada.quarkus.util.OutputMaskFormatter;

import java.math.BigDecimal;
import java.net.URI;
import br.com.ada.quarkus.resource.transaction.TransactionResponse;

/**
 * Recurso responsável pelos endpoints de contas bancárias.
 *
 * <p>Recebe as requisições HTTP relacionadas ao gerenciamento de contas,
 * operações de depósito, saque e transferência com autenticação e autorização.</p>
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

    /**
     * Cria uma nova conta bancária para um cliente.
     *
     * <p>Apenas gerentes podem criar contas. O número da conta é gerado
     * automaticamente com dígito verificador.</p>
     *
     * @param request dados da conta a ser criada.
     * @param uriInfo informações da URI da requisição.
     * @return resposta HTTP 201 com a conta criada.
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
     * <p>Apenas o proprietário ou um gerente podem visualizar a conta.</p>
     *
     * @param id identificador da conta.
     * @return dados da conta com saldo e transações do dia.
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public AccountDetailsResponse findById(@PathParam("id") Long id) {
        //busca conta
        Account account = accountService.findById(id);
        // valida proprietário da conta
        validateAccountOwnership(account);
        // busca titular
        var customer = customerService.findById(account.getCustomerId());
        // monta titular
        CustomerSummaryResponse holder =
                new CustomerSummaryResponse(
                        customer.getId(),
                        customer.getName(),
                        customer.getEmail()
                );
        // busca transaçoes do dia
        var todayTransactions =
                transactionService
                        .listTodayByAccountId(id, 0, 10)
                        .content()
                        .stream()
                        .map(this::toDetailedTransactionResponse)
                        .toList();
        // monta links
        AccountLinksResponse links =
                new AccountLinksResponse(
                        "/transacoes?contaId=" + id
                );
        //   retorna resposta completa
        return new AccountDetailsResponse(
                account.getId(),
                OutputMaskFormatter.formatAccountNumber(
                        account.getAccountNumber()
                ),
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
     * <p>Endpoint público. Contas do tipo ELETRONICA não permitem depósitos.</p>
     *
     * @param id      identificador da conta receptora.
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
        return toDetailedTransactionResponse(transaction);
    }

    /**
     * Realiza um saque em uma conta.
     *
     * <p>Apenas o proprietário ou um gerente podem sacar. Contas do tipo
     * ELETRONICA não permitem saques. Valida saldo suficiente.</p>
     *
     * @param id      identificador da conta de origem.
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
        return toDetailedTransactionResponse(transaction);
    }

    /**
     * Realiza uma transferência entre duas contas.
     *
     * <p>Apenas o proprietário da conta de origem ou um gerente podem transferir.
     * Valida saldo suficiente e que as contas são diferentes.</p>
     *
     * @param id      identificador da conta de origem.
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
        return toDetailedTransactionResponse(transaction);
    }

    /**
     * Valida se o usuário logado é o proprietário da conta ou é gerente.
     *
     * <p>Gerentes podem acessar qualquer conta.
     * Clientes só podem acessar suas próprias contas.</p>
     *
     * @param account conta a validar.
     * @throws ForbiddenException quando o usuário não tem permissão.
     */
    private void validateAccountOwnership(Account account) {
        LoggedUser currentUser = currentUserService.getLoggedUser();

        // Gerente pode fazer qualquer coisa
        if (currentUser.isManager()) {
            return;
        }

        // Cliente só pode acessar sua própria conta
        if (!currentUser.id().equals(account.getCustomerId())) {
            throw new ForbiddenException(
                    "Acesso negado: apenas o proprietário da conta ou um gerente pode realizar esta operação"
            );
        }
    }

    /**
     * Converte a entidade Account em AccountResponse.
     *
     * @param account entidade de conta.
     * @return resposta com dados públicos da conta.
     */
    private AccountResponse toResponse(Account account) {
        return AccountResponse.fromEntity(account);
    }

    /**
     * Converte o request de criação em entidade Account.
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

    /**
     * Converte a entidade Transaction em TransactionResponse.
     *
     * @param transaction entidade de transação.
     * @return resposta com dados da transação.
     */
    private TransactionResponse toDetailedTransactionResponse(Transaction transaction) {
        AccountResponse sourceAccount = null;
        AccountResponse destinationAccount = null;

        if (transaction.getSourceAccountId() != null) {
            sourceAccount = AccountResponse.fromEntity(
                    accountService.findById(transaction.getSourceAccountId())
            );
        }

        if (transaction.getDestinationAccountId() != null) {
            destinationAccount = AccountResponse.fromEntity(
                    accountService.findById(transaction.getDestinationAccountId())
            );
        }

        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDateTime(),
                sourceAccount,
                destinationAccount
        );
    }
}
