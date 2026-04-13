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

/**
 * Serviço responsável pelas operações relacionadas às contas bancárias.
 *
 * <p>Centraliza regras de criação de conta, consulta e movimentações
 * financeiras como depósito, saque e transferência.</p>
 */
@ApplicationScoped
public class AccountService {

    /**
     * Armazena as contas em memória, indexadas pelo id.
     */
    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();

    /**
     * Gera identificadores únicos para novas contas.
     */
    private final AtomicLong sequence = new AtomicLong();

    @Inject
    CustomerService customerService;

    @Inject
    TransactionService transactionService;

    /**
     * Lista todas as contas cadastradas, ordenadas por id.
     *
     * @return lista de contas.
     */
    public List<Account> list() {
        return accounts.values().stream()
                .sorted(Comparator.comparing(Account::getId))
                .map(this::copy)
                .toList();
    }

    /**
     * Busca uma conta pelo id.
     *
     * @param id identificador da conta.
     * @return conta encontrada.
     * @throws NotFoundException quando a conta não existe.
     */
    public Account findById(Long id) {
        return copy(getRequiredAccount(id));
    }

    /**
     * Cria uma nova conta para um cliente existente.
     *
     * @param account dados da conta a ser criada.
     * @return conta criada.
     * @throws NotFoundException quando o cliente informado não existe.
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
     * Realiza um depósito em uma conta.
     *
     * @param accountId identificador da conta.
     * @param amount valor do depósito.
     * @return transação gerada.
     * @throws NotFoundException quando a conta não existe.
     * @throws BadRequestException quando o valor é inválido ou a conta é do tipo ELETRONICA.
     */
    public Transaction deposit(Long accountId, BigDecimal amount) {
        Account account = getRequiredAccount(accountId);

        validateAmount(amount);
        validateElectronicAccountForDeposit(account);

        account.setBalance(account.getBalance().add(amount));

        return transactionService.createDeposit(accountId, amount);
    }

    /**
     * Realiza um saque em uma conta.
     *
     * @param accountId identificador da conta.
     * @param amount valor do saque.
     * @return transação gerada.
     * @throws NotFoundException quando a conta não existe.
     * @throws BadRequestException quando o valor é inválido, a conta é do tipo ELETRONICA
     * ou o saldo é insuficiente.
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
     * Realiza uma transferência entre contas.
     *
     * @param sourceAccountId id da conta de origem.
     * @param destinationAccountId id da conta de destino.
     * @param amount valor da transferência.
     * @return transação gerada.
     * @throws NotFoundException quando alguma das contas não existe.
     * @throws BadRequestException quando o valor é inválido, as contas são iguais
     * ou o saldo da origem é insuficiente.
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
     * Retorna obrigatoriamente uma conta existente.
     *
     * @param id identificador da conta.
     * @return conta encontrada.
     * @throws NotFoundException quando a conta não existe.
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
     *
     * @param customerId identificador do cliente.
     */
    private void validateCustomerExists(Long customerId) {
        customerService.findById(customerId);
    }

    /**
     * Valida se a conta permite depósito.
     *
     * @param account conta a validar.
     * @throws BadRequestException quando a conta é do tipo ELETRONICA.
     */
    private void validateElectronicAccountForDeposit(Account account) {
        if (account.getType() == AccountType.ELETRONICA) {
            throw new BadRequestException("Conta do tipo ELETRONICA não permite depósito");
        }
    }

    /**
     * Valida se a conta permite saque.
     *
     * @param account conta a validar.
     * @throws BadRequestException quando a conta é do tipo ELETRONICA.
     */
    private void validateElectronicAccountForWithdraw(Account account) {
        if (account.getType() == AccountType.ELETRONICA) {
            throw new BadRequestException("Conta do tipo ELETRONICA não permite saque");
        }
    }

    /**
     * Valida se a conta possui saldo suficiente.
     *
     * @param account conta de origem.
     * @param amount valor da operação.
     * @throws BadRequestException quando o saldo é insuficiente.
     */
    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Saldo insuficiente para realizar a operação");
        }
    }

    /**
     * Valida se o valor da operação é maior que zero.
     *
     * @param amount valor informado.
     * @throws BadRequestException quando o valor é nulo ou menor que zero.
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("O valor da operação deve ser maior que zero");
        }
    }

    /**
     * Gera o número da conta com base no id gerado.
     *
     * @param id identificador da conta.
     * @return número formatado da conta.
     */
    private String generateAccountNumber(long id) {
        return String.format("%04d-%d", id, id % 10);
    }

    /**
     * Cria uma cópia defensiva da conta.
     *
     * @param account conta original.
     * @return cópia da conta.
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