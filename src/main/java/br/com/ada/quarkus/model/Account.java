package br.com.ada.quarkus.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representa uma conta bancária dentro do sistema da ADA.
 * <p>
 * Esta classe vincula um número de conta e um tipo específico a um cliente,
 * garantindo que as regras de integridade para identificação bancária sejam respeitadas.
 * </p>
 *
 * @author Marcelo
 * @version 1.0
 */
public class Account {

    /** Identificador único da conta no sistema. */
    private Long id;

    /**
     * Número identificador da conta.
     * Deve conter apenas dígitos, com extensão de 5 a 10 caracteres.
     */
    @NotBlank(message = "O número da conta é obrigatório")
    @Pattern(regexp = "\\d{5,10}", message = "O número da conta deve conter entre 5 e 10 dígitos numéricos")
    private String accountNumber;

    /**
     * Tipo da conta bancária.
     * Define se a conta é CORRENTE, POUPANCA ou ELETRONICA através do Enum {@link AccountType}.
     */
    @NotNull(message = "O tipo da conta é obrigatório")
    private AccountType type;

    /**
     * Identificador do cliente (Customer) proprietário desta conta.
     * Representa a chave estrangeira para a entidade Customer.
     */
    @NotNull(message = "O cliente da conta é obrigatório")
    private Long customerId;

    @NotNull(message = "O saldo da conta é obrigatório")
    private BigDecimal balance;

    /**
     * Construtor padrão (sem argumentos).
     * Essencial para o funcionamento de frameworks de persistência e serialização.
     */
    public Account() {
    }

    /**
     * Construtor para inicialização dos campos principais da conta,
     * o saldo é inicializado automaticamente com zero.
     *
     * @param id            O identificador único da conta.
     * @param accountNumber O número da conta bancária.
     * @param type          O tipo da conta (Enum AccountType).
     * @param customerId    O identificador do cliente dono da conta.
     */
    public Account(Long id, String accountNumber, AccountType type, Long customerId) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.customerId = customerId;
        this.balance = BigDecimal.ZERO;
    }

    /** @return O identificador único da conta. */
    public Long getId() {
        return id;
    }

    /** @param id O novo identificador da conta. */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return O número da conta bancária. */
    public String getAccountNumber() {
        return accountNumber;
    }

    /** @param accountNumber O novo número da conta. */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /** @return O tipo da conta (Enum). */
    public AccountType getType() {
        return type;
    }

    /** @param type O novo tipo da conta. */
    public void setType(AccountType type) {
        this.type = type;
    }

    /** @return O Identificador do cliente da conta. */
    public Long getCustomerId() {
        return customerId;
    }

    /** @param customerId O novo Identificador do cliente da conta. */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /** @return O saldo (balance) da conta. */
    public BigDecimal getBalance() {
        return balance;
    }

    /** @param balance O novo saldo da conta. */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * Retorna uma representação textual dos dados da conta.
     * @return String formatada com os atributos da conta.
     */
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", type=" + type +
                ", customerId=" + customerId +
                ", balance=" + balance +
                '}';
    }

    /**
     * Verifica a igualdade entre duas contas.
     * A comparação é baseada primordialmente no ID e no número da conta.
     *
     * @param o Objeto a ser comparado.
     * @return true se as contas forem idênticas, false caso contrário.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id) &&
                Objects.equals(accountNumber, account.accountNumber);
    }

    /**
     * Gera o código hash para a conta, mantendo a consistência com equals.
     * @return Valor do hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber);
    }
}