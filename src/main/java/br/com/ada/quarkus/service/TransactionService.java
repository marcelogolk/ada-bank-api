package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.PageResult;
import br.com.ada.quarkus.model.Transaction;
import br.com.ada.quarkus.model.TransactionType;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Serviço responsável pelo registro e consulta de transações.
 *
 * <p>Armazena o histórico das movimentações financeiras com persistência
 * em banco de dados PostgreSQL e aplica validações de consistência para
 * cada tipo de transação.</p>
 *
 * @author Marcelo
 * @version 2.0
 */
@ApplicationScoped
public class TransactionService {

    /**
     * Busca uma transação pelo id.
     *
     * @param id identificador da transação.
     * @return transação encontrada.
     * @throws NotFoundException quando a transação não existe.
     */
    public Transaction findById(Long id) {
        return getRequiredTransaction(id);
    }

    /**
     * Lista todas as transações relacionadas a uma conta com paginação,
     * ordenadas por data e hora (decrescente).
     *
     * @param accountId identificador da conta.
     * @param page Número da página (0-indexed).
     * @param size Quantidade de transações por página.
     * @return resultado paginado de transações da conta.
     */
    public PageResult<Transaction> listByAccountId(Long accountId, int page, int size) {
        var query = Transaction.find(
                "sourceAccountId = ?1 OR destinationAccountId = ?1",
                Sort.by("dateTime").descending(),
                accountId
        );
        var result = query.page(Page.of(page, size));

        return new PageResult<>(result.list(), page, size, result.count());
    }
    /**
     * Lista as transações do dia atual relacionadas a uma conta com paginação.
     *
     * @param accountId identificador da conta.
     * @param page Número da página (0-indexed).
     * @param size Quantidade de transações por página.
     * @return resultado paginado de transações do dia.
     */
    public PageResult<Transaction> listTodayByAccountId(Long accountId, int page, int size) {
        LocalDate today = LocalDate.now();

        var query = Transaction.find(
                "(sourceAccountId = ?1 OR destinationAccountId = ?1) AND CAST(dateTime AS DATE) = ?2",
                Sort.by("dateTime").descending(),
                accountId,
                today
        );
        var result = query.page(Page.of(page, size));

        return new PageResult<>(result.list(), page, size, result.count());
    }

    /**
     * Registra uma transação de depósito.
     *
     * @param accountId conta que recebeu o valor.
     * @param amount valor depositado.
     * @return transação registrada.
     */
    public Transaction createDeposit(Long accountId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.DEPOSITO);
        transaction.setAmount(amount);
        transaction.setDateTime(LocalDateTime.now());
        transaction.setSourceAccountId(null);
        transaction.setDestinationAccountId(accountId);

        validateTransactionConsistency(transaction);
        transaction.persist();

        return transaction;
    }

    /**
     * Registra uma transação de saque.
     *
     * @param accountId conta de origem.
     * @param amount valor sacado.
     * @return transação registrada.
     */
    public Transaction createWithdraw(Long accountId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.SAQUE);
        transaction.setAmount(amount);
        transaction.setDateTime(LocalDateTime.now());
        transaction.setSourceAccountId(accountId);
        transaction.setDestinationAccountId(null);

        validateTransactionConsistency(transaction);
        transaction.persist();

        return transaction;
    }

    /**
     * Registra uma transação de transferência.
     *
     * @param sourceAccountId conta de origem.
     * @param destinationAccountId conta de destino.
     * @param amount valor transferido.
     * @return transação registrada.
     */
    public Transaction createTransfer(Long sourceAccountId, Long destinationAccountId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.TRANSFERENCIA);
        transaction.setAmount(amount);
        transaction.setDateTime(LocalDateTime.now());
        transaction.setSourceAccountId(sourceAccountId);
        transaction.setDestinationAccountId(destinationAccountId);

        validateTransactionConsistency(transaction);
        transaction.persist();

        return transaction;
    }

    /**
     * Retorna obrigatoriamente uma transação existente.
     *
     * @param id identificador da transação.
     * @return transação encontrada.
     * @throws NotFoundException quando a transação não existe.
     */
    private Transaction getRequiredTransaction(Long id) {
        Transaction transaction = Transaction.findById(id);

        if (transaction == null) {
            throw new NotFoundException("Transação não encontrada");
        }

        return transaction;
    }

    /**
     * Valida a consistência estrutural da transação conforme seu tipo.
     *
     * @param transaction transação a validar.
     * @throws BadRequestException quando a combinação de tipo, conta de origem
     * e conta de destino é inválida.
     */
    private void validateTransactionConsistency(Transaction transaction) {
        if (transaction.getType() == null) {
            throw new BadRequestException("O tipo da transação é obrigatório");
        }

        TransactionType type = transaction.getType();

        if (type == TransactionType.DEPOSITO) {
            if (transaction.getSourceAccountId() != null) {
                throw new BadRequestException(
                        "Transação do tipo DEPOSITO não deve possuir conta de origem"
                );
            }
            if (transaction.getDestinationAccountId() == null) {
                throw new BadRequestException(
                        "Transação do tipo DEPOSITO deve possuir conta de destino"
                );
            }
        } else if (type == TransactionType.SAQUE) {
            if (transaction.getSourceAccountId() == null) {
                throw new BadRequestException(
                        "Transação do tipo SAQUE deve possuir conta de origem"
                );
            }
            if (transaction.getDestinationAccountId() != null) {
                throw new BadRequestException(
                        "Transação do tipo SAQUE não deve possuir conta de destino"
                );
            }
        } else if (type == TransactionType.TRANSFERENCIA) {
            if (transaction.getSourceAccountId() == null) {
                throw new BadRequestException(
                        "Transação do tipo TRANSFERENCIA deve possuir conta de origem"
                );
            }
            if (transaction.getDestinationAccountId() == null) {
                throw new BadRequestException(
                        "Transação do tipo TRANSFERENCIA deve possuir conta de destino"
                );
            }
        }
    }
}