package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.Account;
import br.com.ada.quarkus.model.AccountType;
import br.com.ada.quarkus.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class AccountService {

    /**
     * Armazena as contas em memória.
     * Chave: id da conta.
     * Valor: objeto Account.
     */
    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();

    /**
     * Gera IDs únicos para novas contas.
     */
    private final AtomicLong sequence = new AtomicLong();

    @Inject
    CustomerService customerService;

    @Inject
    TransactionService transactionService;

    /**
     * Lista todas as contas cadastradas.
     *
     * Regra esperada:
     * - retornar as contas ordenadas por id
     * - devolver cópias, não os objetos internos
     */
    public List<Account> list() {
        return accounts.values().stream()
                .sorted(Comparator.comparing(Account::getId))
                .map(this::copy)
                .toList();
    }

    /**
     * Busca uma conta pelo ID.
     *
     * Regra esperada:
     * - se não existir, lançar NotFoundException
     * - se existir, devolver cópia
     */
    public Account findById(Long id) {
        return copy(getRequiredAccount(id));
    }

    /**
     * Cria uma nova conta.
     *
     * Regra esperada:
     * - validar se o cliente existe
     * - gerar ID
     * - gerar número da conta
     * - inicializar saldo com zero
     * - armazenar no Map
     * - devolver cópia
     */
    public Account create(Account account) {
        validateCustomerExists(account.getCustomerId());

        long id = sequence.incrementAndGet();
        String accountNumber = generateAccountNumber(id);

        Account newAccount = new Account(
                id,
                accountNumber,
                account.getType(),
                account.getCustomerId()
        );

        accounts.put(id, newAccount);
        return copy(newAccount);
    }

    /**
     * Realiza depósito em uma conta.
     *
     * Regra esperada:
     * - conta deve existir
     * - valor deve ser maior que zero
     * - conta ELETRONICA não permite depósito
     * - atualizar saldo
     * - registrar transação de depósito
     * - retornar a transação gerada
     */
    public Transaction deposit(Long accountId, BigDecimal amount) {
        Account account = getRequiredAccount(accountId);

        validateAmount(amount);
        validateElectronicAccountForDeposit(account);

        account.setBalance(account.getBalance().add(amount));

        return transactionService.createDeposit(accountId, amount);
    }

    /**
     * Realiza saque em uma conta.
     *
     * Regra esperada:
     * - conta deve existir
     * - valor deve ser maior que zero
     * - conta ELETRONICA não permite saque
     * - saldo deve ser suficiente
     * - atualizar saldo
     * - registrar transação de saque
     * - retornar a transação gerada
     */
    public Transaction withdraw(Long accountId, BigDecimal amount) {
        Account account = getRequiredAccount(accountId);

        validateAmount(amount);
        validateElectronicAccountForWithdraw(account);
        validateSufficientBalance(account, amount);

        account.setBalance(account.getBalance().subtract(amount));

        return transactionService.createWithdraw(accountId, amount);
    }

    /**
     * Realiza transferência entre contas.
     *
     * Regra esperada:
     * - conta de origem deve existir
     * - conta de destino deve existir
     * - valor deve ser maior que zero
     * - contas devem ser diferentes
     * - saldo da origem deve ser suficiente
     * - debitar origem
     * - creditar destino
     * - registrar transação de transferência
     * - retornar a transação gerada
     */
    public Transaction transfer(Long sourceAccountId, Long destinationAccountId, BigDecimal amount) {
        Account sourceAccount = getRequiredAccount(sourceAccountId);
        Account destinationAccount = getRequiredAccount(destinationAccountId);

        validateAmount(amount);

        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new BadRequestException("A conta de origem deve ser diferente da conta de destino");
        }

        validateSufficientBalance(sourceAccount, amount);

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        return transactionService.createTransfer(sourceAccountId, destinationAccountId, amount);
    }

    /**
     * Busca uma conta por ID e exige que ela exista.
     */
    private Account getRequiredAccount(Long id) {
        Account account = accounts.get(id);

        if (account == null) {
            throw new NotFoundException("Conta não encontrada");
        }

        return account;
    }

    /**
     * Valida se o cliente informado existe.
     */
    private void validateCustomerExists(Long customerId) {
        customerService.findById(customerId);
    }

    /**
     * Valida se a conta permite depósito.
     */
    private void validateElectronicAccountForDeposit(Account account) {
        if (account.getType() == AccountType.ELETRONICA) {
            throw new BadRequestException("Conta do tipo ELETRONICA não permite depósito");
        }
    }

    /**
     * Valida se a conta permite saque.
     */
    private void validateElectronicAccountForWithdraw(Account account) {
        if (account.getType() == AccountType.ELETRONICA) {
            throw new BadRequestException("Conta do tipo ELETRONICA não permite saque");
        }
    }

    /**
     * Valida se a conta possui saldo suficiente.
     */
    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Saldo insuficiente para realizar a operação");
        }
    }

    /**
     * Valida se o valor da operação é maior que zero.
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("O valor da operação deve ser maior que zero");
        }
    }

    /**
     * Gera o número da conta.
     *
     * Estratégia inicial:
     * - usar o id gerado
     * - formatar no padrão 0001-1
     */
    private String generateAccountNumber(long id) {
        return String.format("%04d-%d", id, id % 10);
    }

    /**
     * Cria uma cópia da conta.
     */
    private Account copy(Account account) {
        Account copy = new Account(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getCustomerId()
        );

        copy.setBalance(account.getBalance());
        return copy;
    }
}