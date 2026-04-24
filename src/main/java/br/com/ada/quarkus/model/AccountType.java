package br.com.ada.quarkus.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Define os tipos de conta disponíveis no sistema bancário.
 *
 * <p>Este enum padroniza os tipos aceitos pela aplicação e ajuda a manter
 * a integridade das regras de negócio associadas a cada modalidade de conta.</p>
 *
 * @author Marcelo
 * @version 1.0
 */
public enum AccountType {

    /**
     * Conta Corrente: modalidade padrão para movimentações diárias.
     */
    CORRENTE("CORRENTE"),

    /**
     * Conta Poupança: modalidade voltada para reserva financeira.
     */
    POUPANCA("POUPANCA"),

    /**
     * Conta Eletrônica: modalidade exclusiva para operações digitais,
     * conforme regras específicas do projeto.
     */
    ELETRONICA("ELETRONICA");

    private final String value;

    /**
     * Associa o valor textual usado na serialização JSON.
     *
     * @param value valor textual do tipo de conta.
     */
    AccountType(String value) {
        this.value = value;
    }

    /**
     * Retorna o valor textual utilizado na serialização JSON.
     *
     * @return valor textual do tipo de conta.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Retorna uma descrição amigável do tipo de conta.
     *
     * @return descrição formatada do tipo de conta.
     */
    public String getDescription() {
        return switch (this) {
            case CORRENTE -> "Conta Corrente";
            case POUPANCA -> "Conta Poupança";
            case ELETRONICA -> "Conta Eletrônica";
        };
    }
}