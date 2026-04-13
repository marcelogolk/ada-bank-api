package br.com.ada.quarkus.model;

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
 *
 * @author Marcelo
 * @version 1.0
 */
public class Transaction {

    /** Identificador único e imutável da transação no banco de dados. */
    private Long id;

    /**
     * Tipo da operação financeira.
     * Define a lógica de movimentação (DEPOSITO, SAQUE ou TRANSFERENCIA)
     * conforme o Enum {@link TransactionType}.
     */
    @NotNull(message = "O tipo da transação é obrigatório")
    private TransactionType type;

    /**
     * Valor monetário da operação.
     * Utiliza {@link BigDecimal} para garantir precisão decimal absoluta,
     * evitando erros de arredondamento comuns em tipos de ponto flutuante.
     */
    @NotNull(message = "O valor da transação é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor da transação deve ser no mínimo 0,01")
    private BigDecimal amount;

    /**
     * Carimbo de data e hora da transação.
     * Registra o momento em que a transação foi processada pelo servidor.
     */
    @NotNull(message = "A data e hora da transação é obrigatória")
    private LocalDateTime dateTime;

    /**
     * Identificador da conta de origem da transação.
     */

    private Long sourceAccountId;

    /**
     * Identificador da conta que recebe os fundos.
     * Este campo é opcional em operações de SAQUE, mas obrigatório em
     * TRANSFERENCIAS e DEPOSITOS.
     */
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

    /**
     * Gera uma representação textual detalhada da transação.
     * @return String contendo o estado atual do objeto Transaction.
     */
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

    /**
     * Compara a igualdade entre duas transações baseando-se no ID único.
     *
     * @param o Objeto a ser comparado.
     * @return true se as transações possuírem o mesmo ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Gera o código hash da transação.
     * @return Valor hash baseado no identificador ID.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}