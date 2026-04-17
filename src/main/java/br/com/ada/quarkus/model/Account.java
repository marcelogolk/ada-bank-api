package br.com.ada.quarkus.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representa uma conta bancária dentro do sistema da ADA.
 * <p>
 * Esta classe vincula um número de conta e um tipo específico a um cliente,
 * garantindo que as regras de integridade para identificação bancária sejam respeitadas.
 * O saldo é calculado dinamicamente a partir das transações registradas.
 * </p>
 *
 * @author Marcelo
 * @version 1.0
 */
@Entity
@Table(name = "account")
public class Account extends PanacheEntityBase {

    /**
     * Identificador único da conta no sistema.
     * Gerado automaticamente pelo banco (auto-increment).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Número identificador da conta.
     * Deve conter apenas dígitos, com extensão de 5 a 10 caracteres.
     * Campo obrigatório, único e mapeado para a coluna "account_number" do banco.
     */
    @NotBlank(message = "O número da conta é obrigatório")
    @Pattern(regexp = "\\d{5,10}", message = "O número da conta deve conter entre 5 e 10 dígitos numéricos")
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    /**
     * Tipo da conta bancária.
     * Define se a conta é CORRENTE, POUPANCA ou ELETRONICA através do Enum {@link AccountType}.
     * Campo obrigatório e mapeado para a coluna "type" do banco.
     */
    @NotNull(message = "O tipo da conta é obrigatório")
    @Column(name = "type", nullable = false, length = 20)
    private AccountType type;

    /**
     * Identificador do cliente (Customer) proprietário desta conta.
     * Representa a chave estrangeira para a entidade Customer.
     * Campo obrigatório e mapeado para a coluna "customer_id" do banco.
     */
    @NotNull(message = "O cliente da conta é obrigatório")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Saldo calculado dinamicamente a partir das transações.
     *
     * Este campo é calculado em tempo real através de uma fórmula SQL que:
     * - Soma todos os depósitos para esta conta
     * - Subtrai todos os saques desta conta
     * - Soma as transferências recebidas
     * - Subtrai as transferências enviadas
     *
     * O saldo NÃO é persistido no banco de dados, apenas calculado quando consultado.
     * Isso garante que o saldo sempre reflita o estado real das transações.
     */
    @Formula("(SELECT COALESCE(SUM(" +
            "CASE " +
            "WHEN t.type = 'DEPOSITO' AND t.destination_account_id = id THEN t.amount " +
            "WHEN t.type = 'SAQUE' AND t.source_account_id = id THEN -t.amount " +
            "WHEN t.type = 'TRANSFERENCIA' AND t.destination_account_id = id THEN t.amount " +
            "WHEN t.type = 'TRANSFERENCIA' AND t.source_account_id = id THEN -t.amount " +
            "ELSE 0 END" +
            "), 0) FROM transaction t WHERE t.source_account_id = id OR t.destination_account_id = id)")
    private BigDecimal balance;

    /**
     * Construtor padrão (sem argumentos).
     * Essencial para o funcionamento de frameworks de persistência e serialização.
     */
    public Account() {
    }

    /**
     * Construtor para inicialização dos campos principais da conta.
     * O saldo é calculado automaticamente pela fórmula.
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
    }

    // ========== Getters e Setters ==========

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

    /** @return O identificador do cliente da conta. */
    public Long getCustomerId() {
        return customerId;
    }

    /** @param customerId O novo identificador do cliente da conta. */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /**
     * Retorna o saldo calculado dinamicamente.
     *
     * Este valor é somente leitura (read-only) pois é calculado pela @Formula.
     * Não é possível atualizar manualmente o saldo; ele é sempre derivado
     * das transações registradas.
     *
     * @return O saldo atual da conta.
     */
    public BigDecimal getBalance() {
        return balance;
    }

    // ========== Métodos Object ==========

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id) &&
                Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountNumber);
    }
}