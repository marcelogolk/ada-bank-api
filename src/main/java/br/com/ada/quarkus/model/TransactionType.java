package br.com.ada.quarkus.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Define os tipos de transações financeiras suportadas pelo sistema.
 *
 * <p>Este enum padroniza as operações aceitas pela aplicação e ajuda
 * a preservar a integridade das regras de negócio.</p>
 *
 * @author Marcelo
 * @version 1.0
 */
public enum TransactionType {

    /**
     * Representa entrada direta de fundos em uma conta.
     */
    DEPOSITO("DEPOSITO"),

    /**
     * Representa retirada direta de fundos de uma conta.
     */
    SAQUE("SAQUE"),

    /**
     * Representa movimentação de fundos entre duas contas distintas.
     */
    TRANSFERENCIA("TRANSFERENCIA");

    private final String value;

    /**
     * Associa o valor textual usado na serialização JSON.
     *
     * @param value valor textual do tipo de transação.
     */
    TransactionType(String value) {
        this.value = value;
    }

    /**
     * Retorna o valor textual utilizado na serialização JSON.
     *
     * @return valor textual do tipo de transação.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Retorna uma descrição amigável do tipo de transação.
     *
     * @return descrição formatada do tipo de transação.
     */
    public String getDescription() {
        return switch (this) {
            case DEPOSITO -> "Depósito em conta";
            case SAQUE -> "Saque de numerário";
            case TRANSFERENCIA -> "Transferência entre contas";
        };
    }
}