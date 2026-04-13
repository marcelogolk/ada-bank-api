package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.Transaction;
import br.com.ada.quarkus.model.TransactionType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço responsável pelo registro e consulta de transações.
 *
 * <p>Armazena o histórico das movimentações financeiras e aplica
 * validações de consistência para cada tipo de transação.</p>
 */
@ApplicationScoped
public class TransactionService {

    /**
     * Armazena as transações em memória, indexadas pelo id.
     */
    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();

    /**
     * Gera identificadores únicos para novas transações.
     */
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Busca uma transação pelo id.
     *
     * @param id identificador da transação.
     * @return transação encontrada.
     * @throws NotFoundException quando a transação não existe.
     */
    public Transaction findById(Long id) {
        return copy(getRequiredTransaction(id));
    }

    /**
     * Lista todas as transações relacionadas a uma conta.
     *
     * @param accountId identificador da conta.
     * @return lista de transações da conta.
     */
    public List<Transaction> listByAccountId(Long accountId) {
        return transactions.values().stream()
                .filter(transaction -> isRelatedToAccount(transaction, accountId))
                .sorted(Comparator.comparing(Transaction::getDateTime)
                        .thenComparing(Transaction::getId))
                .map(this::copy)
                .toList();
    }

    /**
     * Lista as transações do dia atual relacionadas a uma conta.
     *
     * @param accountId identificador da conta.
     * @return lista de transações do dia.
     */
    public List<Transaction> listTodayByAccountId(Long accountId) {
        LocalDate today = LocalDate.now();

        return transactions.values().stream()
                .filter(transaction -> isRelatedToAccount(transaction, accountId))
                .filter(transaction -> transaction.getDateTime().toLocalDate().equals(today))
                .sorted(Comparator.comparing(Transaction::getDateTime)
                        .thenComparing(Transaction::getId))
                .map(this::copy)
                .toList();
    }

    /**
     * Registra uma transação de depósito.
     *
     * @param accountId conta que recebeu o valor.
     * @param amount valor depositado.
     * @return transação registrada.
     */
    public Transaction createDeposit(Long accountId, BigDecimal amount) {
        long id = sequence.incrementAndGet();

        Transaction transaction = new Transaction(
                id,
                TransactionType.DEPOSITO,
                amount,
                LocalDateTime.now(),
                null,
                accountId
        );

        validateTransactionConsistency(transaction);
        transactions.put(id, transaction);

        return copy(transaction);
    }

    /**
     * Registra uma transação de saque.
     *
     * @param accountId conta de origem.
     * @param amount valor sacado.
     * @return transação registrada.
     */
    public Transaction createWithdraw(Long accountId, BigDecimal amount) {
        long id = sequence.incrementAndGet();

        Transaction transaction = new Transaction(
                id,
                TransactionType.SAQUE,
                amount,
                LocalDateTime.now(),
                accountId,
                null
        );

        validateTransactionConsistency(transaction);
        transactions.put(id, transaction);

        return copy(transaction);
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
        long id = sequence.incrementAndGet();

        Transaction transaction = new Transaction(
                id,
                TransactionType.TRANSFERENCIA,
                amount,
                LocalDateTime.now(),
                sourceAccountId,
                destinationAccountId
        );

        validateTransactionConsistency(transaction);
        transactions.put(id, transaction);

        return copy(transaction);
    }

    /**
     * Retorna obrigatoriamente uma transação existente.
     *
     * @param id identificador da transação.
     * @return transação encontrada.
     * @throws NotFoundException quando a transação não existe.
     */
    private Transaction getRequiredTransaction(Long id) {
        Transaction transaction = transactions.get(id);

        if (transaction == null) {
            throw new NotFoundException("Transação não encontrada");
        }

        return transaction;
    }

    /**
     * Verifica se a transação está relacionada à conta informada.
     *
     * @param transaction transação analisada.
     * @param accountId identificador da conta.
     * @return true quando a conta participa da transação.
     */
    private boolean isRelatedToAccount(Transaction transaction, Long accountId) {
        return (transaction.getSourceAccountId() != null && transaction.getSourceAccountId().equals(accountId))
                || (transaction.getDestinationAccountId() != null && transaction.getDestinationAccountId().equals(accountId));
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
                throw new BadRequestException("Transação do tipo DEPOSITO não deve possuir conta de origem");
            }
            if (transaction.getDestinationAccountId() == null) {
                throw new BadRequestException("Transação do tipo DEPOSITO deve possuir conta de destino");
            }
        } else if (type == TransactionType.SAQUE) {
            if (transaction.getSourceAccountId() == null) {
                throw new BadRequestException("Transação do tipo SAQUE deve possuir conta de origem");
            }
            if (transaction.getDestinationAccountId() != null) {
                throw new BadRequestException("Transação do tipo SAQUE não deve possuir conta de destino");
            }
        } else if (type == TransactionType.TRANSFERENCIA) {
            if (transaction.getSourceAccountId() == null) {
                throw new BadRequestException("Transação do tipo TRANSFERENCIA deve possuir conta de origem");
            }
            if (transaction.getDestinationAccountId() == null) {
                throw new BadRequestException("Transação do tipo TRANSFERENCIA deve possuir conta de destino");
            }
        }
    }

    /**
     * Cria uma cópia defensiva da transação.
     *
     * @param transaction transação original.
     * @return cópia da transação.
     */
    private Transaction copy(Transaction transaction) {
        return new Transaction(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDateTime(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId()
        );
    }
}