package br.com.ada.quarkus.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representa uma conta bancária dentro do sistema da ADA.
 *
 * <p>Esta classe vincula um número de conta e um tipo específico a um cliente,
 * garantindo a integridade da identificação bancária.</p>
 *
 * <p>O número da conta é gerado automaticamente pelo sistema durante a criação
 * da conta. O saldo é calculado dinamicamente a partir das transações registradas.</p>
 *
 * @author Marcelo
 * @version 1.0
 */
@Entity
@Table(name = "account")
public class Account extends PanacheEntityBase {

    /**
     * Identificador único da conta no sistema.
     * Gerado automaticamente por sequence do PostgreSQL.
     */
    @Id
    @SequenceGenerator(
            name = "account_seq",
            sequenceName = "account_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    private Long id;

    /**
     * Número identificador da conta com dígito verificador.
     *
     * <p>Formato: 9 dígitos base + 1 dígito verificador, totalizando 10 dígitos.
     * Exemplo: "0000000018".</p>
     *
     * <p>Este valor é gerado pelo sistema e armazenado sem máscara visual.</p>
     */
    @Pattern(
            regexp = "\\d{10}",
            message = "O número da conta deve conter exatamente 10 dígitos (9 base + 1 verificador)"
    )
    @Column(name = "account_number", nullable = false, unique = true, length = 10)
    private String accountNumber;

    /**
     * Tipo da conta bancária.
     * Define se a conta é CORRENTE, POUPANCA ou ELETRONICA.
     */
    @NotNull(message = "O tipo da conta é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AccountType type;

    /**
     * Identificador do cliente proprietário da conta.
     * Representa a chave estrangeira para a entidade Customer.
     */
    @NotNull(message = "O cliente da conta é obrigatório")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Saldo calculado dinamicamente a partir das transações.
     *
     * <p>Este campo não é persistido diretamente. O valor é calculado pelo banco
     * considerando depósitos, saques e transferências relacionados à conta.</p>
     */
    @Formula("(SELECT COALESCE(SUM(" +
            "CASE " +
            "WHEN t.type = 'DEPOSITO' AND t.destination_account_id = id THEN t.amount " +
            "WHEN t.type = 'SAQUE' AND t.source_account_id = id THEN -t.amount " +
            "WHEN t.type = 'TRANSFERENCIA' AND t.destination_account_id = id THEN t.amount " +
            "WHEN t.type = 'TRANSFERENCIA' AND t.source_account_id = id THEN -t.amount " +
            "ELSE 0 END" +
            "), 0) FROM bank_transaction t WHERE t.source_account_id = id OR t.destination_account_id = id)")
    private BigDecimal balance;

    /**
     * Construtor padrão necessário para JPA/Hibernate.
     */
    public Account() {
    }

    /**
     * Construtor para inicialização dos campos principais da conta.
     *
     * @param id identificador único da conta.
     * @param accountNumber número da conta bancária.
     * @param type tipo da conta.
     * @param customerId identificador do cliente dono da conta.
     */
    public Account(Long id, String accountNumber, AccountType type, Long customerId) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.customerId = customerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /**
     * Retorna o saldo calculado dinamicamente.
     *
     * @return saldo atual da conta.
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Calcula o dígito verificador com base no número atualmente definido em {@code accountNumber}.
     *
     * <p>Fórmula: 9 - (soma dos dígitos % 10).</p>
     *
     * @return dígito verificador calculado.
     */
    public int calculateCheckDigit() {
        int sum = 0;

        for (char c : accountNumber.toCharArray()) {
            sum += c - '0';
        }

        return 9 - (sum % 10);
    }

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