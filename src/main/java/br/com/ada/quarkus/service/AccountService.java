package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.*;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;

/**
 * Serviço responsável pelas operações relacionadas às contas bancárias.
 * <p>
 * Centraliza regras de criação de conta, consulta e movimentações
 * financeiras como depósito, saque e transferência.
 * </p>
 * <p>
 * Utiliza Panache para persistência em banco de dados PostgreSQL,
 * garantindo que todas as operações sejam transacionais e auditáveis.
 * </p>
 *
 * @author Marcelo
 * @version 1.0
 */
@ApplicationScoped
public class AccountService {

    @Inject
    CustomerService customerService;

    @Inject
    CurrentUserService currentUserService;

    @Inject
    TransactionService transactionService;

    /**
     * Valida se o usuário logado é o proprietário da conta.
     *
     * <p>Apenas o proprietário (cliente dono) pode realizar operações
     * de saque ou transferência. Depósitos podem ser feitos por qualquer um.</p>
     *
     * @param account conta a validar.
     * @throws ForbiddenException quando o usuário não é o proprietário.
     */
    private void checkAccountOwnership(Account account) {
        LoggedUser currentUser = loggedUser();

        if (currentUser.isManager()) {
            return;
        }

        if (currentUser.id().equals(account.getCustomerId())) {
            return;
        }

        throw new ForbiddenException(
                "Acesso negado: apenas o proprietário da conta pode realizar esta operação"
        );
    }

    /**
     * Retorna o usuário logado no momento.
     *
     * @return usuário logado.
     * @throws NotFoundException quando nenhum usuário está autenticado.
     */
    public LoggedUser loggedUser() {
        return currentUserService.getLoggedUser();
    }

    /**
     * Lista contas com filtro opcional por cliente.
     *
     * @param customerId ID do cliente (opcional).
     * @param page Número da página.
     * @param size Quantidade por página.
     * @return Resultado paginado.
     */
    public PageResult<Account> list(Long customerId, int page, int size) {
        var query = (customerId != null)
                ? Account.find("customerId", customerId)
                : Account.findAll(Sort.by("id"));
        var result = query.page(Page.of(page, size));
        return new PageResult<>(result.list(), page, size, result.count());
    }

    /**
     * Busca uma conta pelo identificador.
     *
     * @param id Identificador único da conta.
     * @return Conta encontrada.
     * @throws NotFoundException Quando a conta não existe no banco.
     */
    public Account findById(Long id) {
        return getRequiredAccount(id);
    }

    /**
     * Cria uma nova conta para um cliente existente.
     * <p>
     * O número da conta é gerado automaticamente com base no ID auto-increment,
     * seguindo o padrão: 9 dígitos zero-padded + 1 dígito verificador (total 10 dígitos).
     * Exemplo: ID=1 → número base="000000001" → dígito=8 → número completo="0000000018"
     * </p>
     *
     * @param account Dados da conta a ser criada (tipo e cliente).
     * @return Conta criada e persistida no banco.
     * @throws NotFoundException Quando o cliente informado não existe.
     */
    public Account create(Account account) {
        validateCustomerExists(account.getCustomerId());

        Account newAccount = new Account();
        newAccount.setType(account.getType());
        newAccount.setCustomerId(account.getCustomerId());

        newAccount.persist();

        String baseNumber = String.format("%09d", newAccount.getId());
        newAccount.setAccountNumber(baseNumber);

        int checkDigit = newAccount.calculateCheckDigit();
        String fullAccountNumber = baseNumber + checkDigit;

        newAccount.setAccountNumber(fullAccountNumber);

        return newAccount;
    }

    /**
     * Realiza um depósito em uma conta.
     *
     * @param accountId Identificador da conta receptora.
     * @param amount Valor do depósito.
     * @return Transação de depósito criada.
     * @throws NotFoundException Quando a conta não existe.
     * @throws BadRequestException Quando o valor é inválido ou conta é ELETRONICA.
     */
    public Transaction deposit(Long accountId, BigDecimal amount) {
        Account account = getRequiredAccount(accountId);

        validateAmount(amount);
        validateElectronicAccountForDeposit(account);

        return transactionService.createDeposit(accountId, amount);
    }

    /**
     * Realiza um saque em uma conta.
     *
     * @param accountId Identificador da conta de origem.
     * @param amount Valor do saque.
     * @return Transação de saque criada.
     * @throws ForbiddenException Quando o usuário não é o proprietário.
     * @throws BadRequestException Quando há problemas na operação.
     */
    public Transaction withdraw(Long accountId, BigDecimal amount) {
        Account account = getRequiredAccount(accountId);

        checkAccountOwnership(account);

        validateAmount(amount);
        validateElectronicAccountForWithdraw(account);
        validateSufficientBalance(account, amount);

        return transactionService.createWithdraw(accountId, amount);
    }

    /**
     * Realiza uma transferência entre duas contas.
     *
     * @param sourceAccountId Identificador da conta de origem.
     * @param destinationAccountId Identificador da conta de destino.
     * @param amount Valor da transferência.
     * @return Transação de transferência criada.
     * @throws ForbiddenException Quando o usuário não é o proprietário da conta de origem.
     * @throws BadRequestException Quando há problemas na operação.
     */
    public Transaction transfer(Long sourceAccountId, Long destinationAccountId, BigDecimal amount) {
        Account sourceAccount = getRequiredAccount(sourceAccountId);
        Account destinationAccount = getRequiredAccount(destinationAccountId);

        checkAccountOwnership(sourceAccount);

        validateAmount(amount);
        validateDifferentAccounts(sourceAccountId, destinationAccountId);
        validateSufficientBalance(sourceAccount, amount);

        return transactionService.createTransfer(sourceAccountId, destinationAccountId, amount);
    }

    /**
     * Retorna obrigatoriamente uma conta existente.
     *
     * @param id Identificador da conta.
     * @return Conta encontrada no banco.
     * @throws NotFoundException Quando a conta não existe.
     */
    private Account getRequiredAccount(Long id) {
        Account account = Account.findById(id);

        if (account == null) {
            throw new NotFoundException("Conta não encontrada");
        }

        return account;
    }

    /**
     * Valida se o cliente informado existe no banco.
     *
     * @param customerId Identificador do cliente.
     * @throws NotFoundException Quando o cliente não existe.
     */
    private void validateCustomerExists(Long customerId) {
        customerService.findById(customerId);
    }

    /**
     * Valida se a conta permite operação de depósito.
     *
     * @param account Conta a validar.
     * @throws BadRequestException Quando a conta é do tipo ELETRONICA.
     */
    private void validateElectronicAccountForDeposit(Account account) {
        if (account.getType() == AccountType.ELETRONICA) {
            throw new BadRequestException("Conta do tipo ELETRONICA não permite depósito");
        }
    }

    /**
     * Valida se a conta permite operação de saque.
     *
     * @param account Conta a validar.
     * @throws BadRequestException Quando a conta é do tipo ELETRONICA.
     */
    private void validateElectronicAccountForWithdraw(Account account) {
        if (account.getType() == AccountType.ELETRONICA) {
            throw new BadRequestException("Conta do tipo ELETRONICA não permite saque");
        }
    }

    /**
     * Valida se a conta de origem é diferente da conta de destino.
     *
     * @param sourceAccountId ID da conta de origem.
     * @param destinationAccountId ID da conta de destino.
     * @throws BadRequestException Quando as contas são iguais.
     */
    private void validateDifferentAccounts(Long sourceAccountId, Long destinationAccountId) {
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new BadRequestException("A conta de origem deve ser diferente da conta de destino");
        }
    }

    /**
     * Valida se a conta possui saldo suficiente para a operação.
     *
     * @param account Conta de origem.
     * @param amount Valor da operação.
     * @throws BadRequestException Quando o saldo é insuficiente.
     */
    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Saldo insuficiente para realizar a operação");
        }
    }

    /**
     * Valida se o valor da operação é válido.
     *
     * @param amount Valor informado.
     * @throws BadRequestException Quando o valor é nulo ou menor/igual a zero.
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("O valor da operação deve ser maior que zero");
        }
    }
}