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
 *
 * <p>Centraliza regras de criação de conta, consulta e movimentações
 * financeiras como depósito, saque e transferência.</p>
 *
 * <p>Utiliza Panache para persistência em banco de dados PostgreSQL.
 * O saldo das contas é calculado dinamicamente a partir das transações.</p>
 *
 * @author Marcelo
 * @version 2.0
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
     * Lista contas com filtro opcional por cliente.
     *
     * @param customerId ID do cliente (opcional).
     * @param page número da página.
     * @param size quantidade por página.
     * @return resultado paginado de contas.
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
     * @param id identificador único da conta.
     * @return conta encontrada.
     * @throws NotFoundException quando a conta não existe no banco.
     */
    public Account findById(Long id) {
        return getRequiredAccount(id);
    }

    /**
     * Cria uma nova conta para um cliente existente.
     *
     * <p>O número da conta é gerado automaticamente com base no ID da entidade,
     * seguindo o padrão: 9 dígitos zero-padded + 1 dígito verificador
     * (total 10 dígitos).</p>
     *
     * <p>Como o número da conta depende do ID gerado pelo banco, o serviço
     * força a geração do identificador antes de calcular e atribuir
     * o número final da conta.</p>
     *
     * @param account dados da conta a ser criada (tipo e cliente).
     * @return conta criada e persistida no banco.
     * @throws NotFoundException quando o cliente informado não existe.
     */
    /**
     * Cria uma nova conta para um cliente existente.
     *
     * <p>O número da conta é gerado automaticamente com base no próximo valor
     * da sequence de contas, seguindo o padrão: 9 dígitos zero-padded + 1 dígito
     * verificador (total 10 dígitos).</p>
     *
     * @param account dados da conta a ser criada (tipo e cliente).
     * @return conta criada e persistida no banco.
     * @throws NotFoundException quando o cliente informado não existe.
     */
    public Account create(Account account) {
        validateCustomerExists(account.getCustomerId());

        Long nextId = ((Number) Account.getEntityManager()
                .createNativeQuery("select nextval('account_id_seq')")
                .getSingleResult())
                .longValue();

        String baseNumber = String.format("%09d", nextId);
        int checkDigit = calculateCheckDigit(baseNumber);
        String fullAccountNumber = baseNumber + checkDigit;

        Account.getEntityManager()
                .createNativeQuery("""
                    INSERT INTO account (id, account_number, type, customer_id)
                    VALUES (:id, :accountNumber, :type, :customerId)
                    """)
                .setParameter("id", nextId)
                .setParameter("accountNumber", fullAccountNumber)
                .setParameter("type", account.getType().name())
                .setParameter("customerId", account.getCustomerId())
                .executeUpdate();

        return getRequiredAccount(nextId);
    }
    /**
     * Realiza um depósito em uma conta.
     *
     * <p>Depósitos podem ser realizados sem validação de propriedade,
     * pois o endpoint correspondente é público conforme regra do projeto.</p>
     *
     * @param accountId identificador da conta receptora.
     * @param amount valor do depósito.
     * @return transação de depósito criada.
     * @throws NotFoundException quando a conta não existe.
     * @throws BadRequestException quando o valor é inválido ou a conta é ELETRONICA.
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
     * <p>Gerentes podem realizar saques em qualquer conta.
     * Clientes só podem realizar saques em suas próprias contas.</p>
     *
     * @param accountId identificador da conta de origem.
     * @param amount valor do saque.
     * @return transação de saque criada.
     * @throws ForbiddenException quando o usuário não tem permissão.
     * @throws NotFoundException quando a conta não existe.
     * @throws BadRequestException quando o valor é inválido, a conta é ELETRONICA
     *                             ou o saldo é insuficiente.
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
     * <p>Gerentes podem transferir a partir de qualquer conta.
     * Clientes só podem transferir a partir de suas próprias contas.</p>
     *
     * @param sourceAccountId identificador da conta de origem.
     * @param destinationAccountId identificador da conta de destino.
     * @param amount valor da transferência.
     * @return transação de transferência criada.
     * @throws ForbiddenException quando o usuário não tem permissão.
     * @throws NotFoundException quando alguma das contas não existe.
     * @throws BadRequestException quando o valor é inválido, as contas são iguais
     *                             ou o saldo é insuficiente.
     */
    public Transaction transfer(Long sourceAccountId, Long destinationAccountId, BigDecimal amount) {
        Account sourceAccount = getRequiredAccount(sourceAccountId);
        getRequiredAccount(destinationAccountId);

        checkAccountOwnership(sourceAccount);

        validateAmount(amount);
        validateDifferentAccounts(sourceAccountId, destinationAccountId);
        validateSufficientBalance(sourceAccount, amount);

        return transactionService.createTransfer(sourceAccountId, destinationAccountId, amount);
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
     * Valida se o usuário logado é gerente ou proprietário da conta.
     *
     * <p>Gerentes podem realizar operações em qualquer conta.
     * Clientes só podem realizar operações em suas próprias contas.</p>
     *
     * @param account conta a validar.
     * @throws ForbiddenException quando o usuário não tem permissão.
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
                "Acesso negado: apenas o proprietário da conta ou um gerente pode realizar esta operação"
        );
    }

    /**
     * Retorna obrigatoriamente uma conta existente.
     *
     * @param id identificador da conta.
     * @return conta encontrada no banco.
     * @throws NotFoundException quando a conta não existe.
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
     * @param customerId identificador do cliente.
     * @throws NotFoundException quando o cliente não existe.
     */
    private void validateCustomerExists(Long customerId) {
        customerService.findById(customerId);
    }

    /**
     * Valida se a conta permite operação de depósito.
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
     * Valida se a conta permite operação de saque.
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
     * Valida se a conta de origem é diferente da conta de destino.
     *
     * @param sourceAccountId ID da conta de origem.
     * @param destinationAccountId ID da conta de destino.
     * @throws BadRequestException quando as contas são iguais.
     */
    private void validateDifferentAccounts(Long sourceAccountId, Long destinationAccountId) {
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new BadRequestException("A conta de origem deve ser diferente da conta de destino");
        }
    }

    /**
     * Valida se a conta possui saldo suficiente para a operação.
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
     * Valida se o valor da operação é válido.
     *
     * @param amount valor informado.
     * @throws BadRequestException quando o valor é nulo ou menor/igual a zero.
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("O valor da operação deve ser maior que zero");
        }
    }

    /**
     * Calcula o dígito verificador do número base da conta.
     *
     * @param baseNumber número base da conta com 9 dígitos.
     * @return dígito verificador calculado.
     */
    private int calculateCheckDigit(String baseNumber) {
        int sum = 0;

        for (char digit : baseNumber.toCharArray()) {
            sum += digit - '0';
        }

        return 9 - (sum % 10);
    }
}