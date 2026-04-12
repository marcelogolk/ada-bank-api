package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.Transaction;
import br.com.ada.quarkus.model.TransactionType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class TransactionService {

    /**
     * Armazena as transações em memória.
     * Chave: id da transação
     * Valor: objeto Transaction
     */
    private final Map<Long, Transaction> transactions = new ConcurrentHashMap<>();

    /**
     * Gera IDs únicos para novas transações.
     */
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Busca uma transação pelo ID.
     *
     * Regra esperada:
     * - se não existir, lançar NotFoundException
     * - se existir, devolver cópia
     */
    public Transaction findById(Long id) {
        return copy(getRequiredTransaction(id));
    }

    /**
     * Lista todas as transações relacionadas a uma conta.
     *
     * Regra esperada:
     * - incluir transações em que a conta é origem
     * - incluir transações em que a conta é destino
     * - ordenar por data/hora
     * - devolver cópias
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
     * Regra esperada:
     * - filtrar transações da conta
     * - filtrar apenas as do dia atual
     * - ordenar por data/hora
     * - devolver cópias
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
     * Convenção adotada:
     * - sourceAccountId = null
     * - destinationAccountId = conta que recebeu o valor
     *
     * Regra esperada:
     * - gerar ID
     * - usar tipo DEPOSITO
     * - usar data/hora atual
     * - armazenar no Map
     * - devolver cópia
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

        transactions.put(id, transaction);
        return copy(transaction);
    }

    /**
     * Registra uma transação de saque.
     *
     * Convenção adotada:
     * - sourceAccountId = conta de onde o valor saiu
     * - destinationAccountId = null
     *
     * Regra esperada:
     * - gerar ID
     * - usar tipo SAQUE
     * - usar data/hora atual
     * - armazenar no Map
     * - devolver cópia
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

        transactions.put(id, transaction);
        return copy(transaction);
    }

    /**
     * Registra uma transação de transferência.
     *
     * Convenção adotada:
     * - sourceAccountId = conta de origem
     * - destinationAccountId = conta de destino
     *
     * Regra esperada:
     * - gerar ID
     * - usar tipo TRANSFERENCIA
     * - usar data/hora atual
     * - armazenar no Map
     * - devolver cópia
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

        transactions.put(id, transaction);
        return copy(transaction);
    }

    /**
     * Busca uma transação por ID e exige que ela exista.
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
     * Regra:
     * - a conta pode ser origem
     * - a conta pode ser destino
     */
    private boolean isRelatedToAccount(Transaction transaction, Long accountId) {
        return (transaction.getSourceAccountId() != null && transaction.getSourceAccountId().equals(accountId))
                || (transaction.getDestinationAccountId() != null && transaction.getDestinationAccountId().equals(accountId));
    }

    /**
     * Cria uma cópia da transação.
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