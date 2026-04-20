package br.com.ada.quarkus.resource.transaction;

import br.com.ada.quarkus.model.LoggedUser;
import br.com.ada.quarkus.model.Transaction;
import br.com.ada.quarkus.resource.PageResponse;
import br.com.ada.quarkus.resource.account.AccountResponse;
import br.com.ada.quarkus.service.AccountService;
import br.com.ada.quarkus.service.CurrentUserService;
import br.com.ada.quarkus.service.TransactionService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * Recurso responsável pelos endpoints de transações.
 *
 * <p>Recebe as requisições HTTP relacionadas à consulta de transações
 * com autenticação e autorização.</p>
 *
 * @author Marcelo
 * @version 2.0
 */
@Path("/transacoes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {

    @Inject
    TransactionService transactionService;

    @Inject
    AccountService accountService;

    @Inject
    CurrentUserService currentUserService;

    /**
     * Busca uma transação pelo identificador.
     *
     * <p>Apenas o proprietário da conta ou um gerente podem visualizar a transação.</p>
     *
     * @param id identificador da transação.
     * @return dados completos da transação.
     */
    @GET
    @Path("/{id}")
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public TransactionResponse findById(@PathParam("id") Long id) {
        Transaction transaction = transactionService.findById(id);
        validateTransactionAccess(transaction);
        return toResponse(transaction);
    }

    /**
     * Lista todas as transações de uma conta com paginação.
     *
     * <p>Apenas o proprietário da conta ou um gerente podem visualizar o histórico.</p>
     *
     * @param accountId identificador da conta.
     * @param page número da página (0-indexed). Padrão: 0
     * @param size quantidade de transações por página. Padrão: 10
     * @return página de transações da conta.
     */
    @GET
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public PageResponse<TransactionResponse> listByAccountId(
            @QueryParam("contaId") Long accountId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        validateAccountAccess(accountId);

        return PageResponse.from(
                transactionService.listByAccountId(accountId, page, size),
                this::toResponse
        );
    }

    /**
     * Lista as transações do dia atual de uma conta com paginação.
     *
     * <p>Apenas o proprietário da conta ou um gerente podem visualizar.</p>
     *
     * @param accountId identificador da conta.
     * @param page número da página (0-indexed). Padrão: 0
     * @param size quantidade de transações por página. Padrão: 10
     * @return página de transações do dia atual.
     */
    @GET
    @Path("/hoje")
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public PageResponse<TransactionResponse> listTodayByAccountId(
            @QueryParam("contaId") Long accountId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        validateAccountAccess(accountId);

        return PageResponse.from(
                transactionService.listTodayByAccountId(accountId, page, size),
                this::toResponse
        );
    }

    /**
     * Valida se o usuário logado tem acesso à transação.
     *
     * <p>Gerentes podem acessar qualquer transação.
     * Clientes só podem acessar transações de suas próprias contas.</p>
     *
     * @param transaction transação a validar.
     * @throws ForbiddenException quando o usuário não tem permissão.
     */
    private void validateTransactionAccess(Transaction transaction) {
        LoggedUser currentUser = currentUserService.getLoggedUser();

        // Gerente pode fazer qualquer coisa
        if (currentUser.isManager()) {
            return;
        }

        // Cliente só pode acessar transações de suas próprias contas
        Long sourceAccountId = transaction.getSourceAccountId();
        Long destinationAccountId = transaction.getDestinationAccountId();

        boolean hasAccess = false;

        if (sourceAccountId != null) {
            Long sourceCustomerId = accountService.findById(sourceAccountId).getCustomerId();
            if (currentUser.id().equals(sourceCustomerId)) {
                hasAccess = true;
            }
        }

        if (destinationAccountId != null && !hasAccess) {
            Long destinationCustomerId = accountService.findById(destinationAccountId).getCustomerId();
            if (currentUser.id().equals(destinationCustomerId)) {
                hasAccess = true;
            }
        }

        if (!hasAccess) {
            throw new ForbiddenException(
                    "Acesso negado: você não tem permissão para visualizar esta transação"
            );
        }
    }

    /**
     * Valida se o usuário logado tem acesso à conta.
     *
     * <p>Gerentes podem acessar qualquer conta.
     * Clientes só podem acessar suas próprias contas.</p>
     *
     * @param accountId identificador da conta.
     * @throws ForbiddenException quando o usuário não tem permissão.
     */
    private void validateAccountAccess(Long accountId) {
        LoggedUser currentUser = currentUserService.getLoggedUser();

        // Gerente pode fazer qualquer coisa
        if (currentUser.isManager()) {
            return;
        }

        // Cliente só pode acessar sua própria conta
        Long customerId = accountService.findById(accountId).getCustomerId();
        if (!currentUser.id().equals(customerId)) {
            throw new ForbiddenException(
                    "Acesso negado: você não tem permissão para visualizar o histórico desta conta"
            );
        }
    }

    /**
     * Converte a entidade Transaction em TransactionResponse.
     *
     * @param transaction entidade de transação.
     * @return resposta com dados completos da transação.
     */
    private TransactionResponse toResponse(Transaction transaction) {
        AccountResponse sourceAccount = null;
        AccountResponse destinationAccount = null;

        if (transaction.getSourceAccountId() != null) {
            sourceAccount = toAccountResponse(
                    accountService.findById(transaction.getSourceAccountId())
            );
        }

        if (transaction.getDestinationAccountId() != null) {
            destinationAccount = toAccountResponse(
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

    /**
     * Converte a entidade Account em AccountResponse simplificado.
     *
     * @param account entidade de conta.
     * @return resposta com dados da conta.
     */
    private AccountResponse toAccountResponse(br.com.ada.quarkus.model.Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getBalance()
        );
    }
}