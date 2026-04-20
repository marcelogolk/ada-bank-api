package br.com.ada.quarkus.resource.account;

import br.com.ada.quarkus.model.Account;
import br.com.ada.quarkus.model.LoggedUser;
import br.com.ada.quarkus.model.Transaction;
import br.com.ada.quarkus.resource.PageResponse;
import br.com.ada.quarkus.service.AccountService;
import br.com.ada.quarkus.service.CurrentUserService;
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

import java.math.BigDecimal;
import java.net.URI;

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
    public AccountResponse findById(@PathParam("id") Long id) {
        Account account = accountService.findById(id);
        validateAccountOwnership(account);
        return toResponse(account);
    }

    /**
     * Realiza um depósito em uma conta.
     *
     * <p>Endpoint público. Contas do tipo ELETRONICA não permitem depósitos.</p>
     *
     * @param id identificador da conta receptora.
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
        return toTransactionResponse(transaction);
    }

    /**
     * Realiza um saque em uma conta.
     *
     * <p>Apenas o proprietário ou um gerente podem sacar. Contas do tipo
     * ELETRONICA não permitem saques. Valida saldo suficiente.</p>
     *
     * @param id identificador da conta de origem.
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
        return toTransactionResponse(transaction);
    }

    /**
     * Realiza uma transferência entre duas contas.
     *
     * <p>Apenas o proprietário da conta de origem ou um gerente podem transferir.
     * Valida saldo suficiente e que as contas são diferentes.</p>
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
        return toTransactionResponse(transaction);
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
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getBalance()
        );
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
    private TransactionResponse toTransactionResponse(Transaction transaction) {
        Account account = accountService.findById(
                transaction.getSourceAccountId() != null
                        ? transaction.getSourceAccountId()
                        : transaction.getDestinationAccountId()
        );

        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                account.getBalance(),
                transaction.getDateTime()
        );
    }
}