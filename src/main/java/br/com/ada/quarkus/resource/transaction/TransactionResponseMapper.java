package br.com.ada.quarkus.resource.transaction;

import br.com.ada.quarkus.model.Account;
import br.com.ada.quarkus.model.Transaction;
import br.com.ada.quarkus.resource.account.AccountResponse;
import br.com.ada.quarkus.service.AccountService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Componente responsável por converter entidades Transaction
 * em TransactionResponse.
 *
 * <p>Centraliza a lógica de montagem da resposta detalhada de transações,
 * evitando duplicação entre resources.</p>
 *
 * @author Marcelo
 * @version 2.0
 */
@ApplicationScoped
public class TransactionResponseMapper {

    @Inject
    AccountService accountService;

    /**
     * Converte uma entidade Transaction em TransactionResponse.
     *
     * @param transaction entidade de transação.
     * @return resposta detalhada da transação.
     */
    public TransactionResponse toResponse(Transaction transaction) {
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
    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getBalance()
        );
    }
}