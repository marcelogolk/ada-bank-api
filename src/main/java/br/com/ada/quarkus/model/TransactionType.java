package br.com.ada.quarkus.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Define os tipos de transações financeiras suportadas pelo sistema.
 * * @author Marcelo
 */
public enum TransactionType {

    /** Representa a entrada de fundos em uma conta. */
    DEPOSIT("DEPÓSITO"),

    /** Representa a retirada de fundos de uma conta. */
    WITHDRAWAL("SAQUE"),

    /** Representa a movimentação de fundos entre duas contas distintas. */
    TRANSFER("TRANSFERÊNCIA");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * @return Descrição detalhada do tipo de transação em português.
     */
    public String getDescription() {
        return switch (this) {
            case DEPOSIT -> "Depósito em conta";
            case WITHDRAWAL -> "Saque de numerário";
            case TRANSFER -> "Transferência entre contas";
        };
    }
}