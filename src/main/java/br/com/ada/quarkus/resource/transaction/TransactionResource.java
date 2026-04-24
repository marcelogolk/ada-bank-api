package br.com.ada.quarkus.resource.transaction;

import br.com.ada.quarkus.model.LoggedUser;
import br.com.ada.quarkus.model.Transaction;
import br.com.ada.quarkus.resource.PageResponse;
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
 * com autenticação, autorização e paginação.</p>
 *
 * @author Marcelo
 * @version 2.0
 */
@Path("/transacoes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {

    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    TransactionService transactionService;

    @Inject
    AccountService accountService;

    @Inject
    CurrentUserService currentUserService;

    @Inject
    TransactionResponseMapper transactionResponseMapper;

    /**
     * Busca uma transação pelo identificador.
     *
     * <p>Gerentes podem visualizar qualquer transação.
     * Clientes só podem visualizar transações vinculadas às suas próprias contas.</p>
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
        return transactionResponseMapper.toResponse(transaction);
    }

    /**
     * Lista todas as transações de uma conta com paginação.
     *
     * <p>Gerentes podem visualizar o histórico de qualquer conta.
     * Clientes só podem visualizar o histórico das próprias contas.</p>
     *
     * @param accountId identificador da conta.
     * @param page número da página (0-indexed). Padrão: 0.
     * @param size quantidade de transações por página. Padrão: 10.
     * @return página de transações da conta.
     */
    @GET
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public PageResponse<TransactionResponse> listByAccountId(
            @QueryParam("accountId") Long accountId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        validateListParameters(accountId, page, size);
        validateAccountAccess(accountId);

        return PageResponse.from(
                transactionService.listByAccountId(accountId, page, size),
                transactionResponseMapper::toResponse
        );
    }

    /**
     * Lista as transações do dia atual de uma conta com paginação.
     *
     * <p>Gerentes podem visualizar as transações do dia de qualquer conta.
     * Clientes só podem visualizar as transações do dia das próprias contas.</p>
     *
     * @param accountId identificador da conta.
     * @param page número da página (0-indexed). Padrão: 0.
     * @param size quantidade de transações por página. Padrão: 10.
     * @return página de transações do dia atual.
     */
    @GET
    @Path("/hoje")
    @RolesAllowed({"GERENTE", "CLIENTE"})
    public PageResponse<TransactionResponse> listTodayByAccountId(
            @QueryParam("accountId") Long accountId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        validateListParameters(accountId, page, size);
        validateAccountAccess(accountId);

        return PageResponse.from(
                transactionService.listTodayByAccountId(accountId, page, size),
                transactionResponseMapper::toResponse
        );
    }

    /**
     * Valida os parâmetros de listagem paginada.
     *
     * @param accountId identificador da conta.
     * @param page número da página.
     * @param size tamanho da página.
     * @throws BadRequestException quando algum parâmetro é inválido.
     */
    private void validateListParameters(Long accountId, int page, int size) {
        if (accountId == null) {
            throw new BadRequestException("O parâmetro accountId é obrigatório");
        }

        if (page < 0) {
            throw new BadRequestException("O parâmetro page deve ser maior ou igual a zero");
        }

        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("O parâmetro size deve estar entre 1 e " + MAX_PAGE_SIZE);
        }
    }

    /**
     * Valida se o usuário logado tem acesso à transação.
     *
     * <p>Gerentes podem acessar qualquer transação.
     * Clientes só podem acessar transações vinculadas às suas próprias contas,
     * seja como origem ou destino.</p>
     *
     * @param transaction transação a validar.
     * @throws ForbiddenException quando o usuário não tem permissão.
     */
    private void validateTransactionAccess(Transaction transaction) {
        LoggedUser currentUser = currentUserService.getLoggedUser();

        if (currentUser.isManager()) {
            return;
        }

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

        if (currentUser.isManager()) {
            return;
        }

        Long customerId = accountService.findById(accountId).getCustomerId();
        if (!currentUser.id().equals(customerId)) {
            throw new ForbiddenException(
                    "Acesso negado: você não tem permissão para visualizar o histórico desta conta"
            );
        }
    }
}