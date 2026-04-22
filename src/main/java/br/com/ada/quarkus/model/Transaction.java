package br.com.ada.quarkus.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa uma movimentação financeira realizada no sistema bancário.
 * <p>
 * Esta classe é responsável por registrar o fluxo de capitais, detalhando o tipo da
 * operação, o montante envolvido, as contas de origem e destino, além do registro
 * temporal exato da execução.
 * </p>
 * <p>
 * As transações são imutáveis após criação, não possuindo versioning, pois representam
 * eventos históricos que não devem ser alterados.
 * </p>
 *
 * @author Marcelo
 * @version 1.0
 */
@Entity
@Table(name = "bank_transaction")
public class Transaction extends PanacheEntityBase {

    /**
     * Identificador único e imutável da transação no banco de dados.
     * Gerado automaticamente pelo banco (auto-increment).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo da operação financeira.
     * Define a lógica de movimentação (DEPOSITO, SAQUE ou TRANSFERENCIA)
     * conforme o Enum {@link TransactionType}.
     * Campo obrigatório e mapeado para a coluna "type" do banco.
     */
    @NotNull(message = "O tipo da transação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 25)
    private TransactionType type;

    /**
     * Valor monetário da operação.
     * Utiliza {@link BigDecimal} para garantir precisão decimal absoluta,
     * evitando erros de arredondamento comuns em tipos de ponto flutuante.
     * Campo obrigatório com mínimo de 0,01 e mapeado para a coluna "amount" do banco.
     */
    @NotNull(message = "O valor da transação é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor da transação deve ser no mínimo 0,01")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Carimbo de data e hora da transação.
     * Registra o momento em que a transação foi processada pelo servidor.
     * Campo obrigatório e mapeado para a coluna "date_time" do banco.
     */
    @NotNull(message = "A data e hora da transação é obrigatória")
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    /**
     * Identificador da conta de origem da transação.
     * Representa a chave estrangeira para a entidade Account.
     * Opcional em operações de DEPOSITO.
     * Mapeado para a coluna "source_account_id" do banco.
     */
    @Column(name = "source_account_id")
    private Long sourceAccountId;

    /**
     * Identificador da conta que recebe os fundos.
     * Representa a chave estrangeira para a entidade Account.
     * Opcional em operações de SAQUE.
     * Mapeado para a coluna "destination_account_id" do banco.
     */
    @Column(name = "destination_account_id")
    private Long destinationAccountId;

    /**
     * Construtor padrão (sem argumentos).
     * Requisito obrigatório para frameworks de serialização (Jackson) e persistência (JPA).
     */
    public Transaction() {
    }

    /**
     * Construtor completo para instanciar uma transação com todos os seus atributos.
     *
     * @param id                   O identificador da transação.
     * @param type                 O tipo da operação financeira.
     * @param amount               O valor monetário envolvido.
     * @param dateTime             A data e hora da execução.
     * @param sourceAccountId      O ID da conta de origem.
     * @param destinationAccountId O ID da conta de destino.
     */
    public Transaction(Long id, TransactionType type, BigDecimal amount, LocalDateTime dateTime,
                       Long sourceAccountId, Long destinationAccountId) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.dateTime = dateTime;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
    }

    // ========== Getters e Setters ==========

    /** @return O identificador único da transação. */
    public Long getId() {
        return id;
    }

    /** @param id O novo identificador da transação. */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return O tipo da transação (Enum). */
    public TransactionType getType() {
        return type;
    }

    /** @param type O novo tipo da transação. */
    public void setType(TransactionType type) {
        this.type = type;
    }

    /** @return O valor monetário da transação. */
    public BigDecimal getAmount() {
        return amount;
    }

    /** @param amount O novo valor da transação. */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /** @return A data e hora da transação. */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /** @param dateTime A nova data e hora da transação. */
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /** @return O ID da conta de origem. */
    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    /** @param sourceAccountId O novo identificador da conta de origem. */
    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    /** @return O ID da conta de destino. */
    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    /** @param destinationAccountId O novo ID da conta de destino. */
    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    // ========== Métodos Object ==========

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type=" + type +
                ", amount=" + amount +
                ", dateTime=" + dateTime +
                ", sourceAccountId=" + sourceAccountId +
                ", destinationAccountId=" + destinationAccountId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}