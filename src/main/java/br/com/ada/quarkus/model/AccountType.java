package br.com.ada.quarkus.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Define os tipos de conta disponíveis no sistema bancário.
 * <p>
 * Este enum é utilizado para garantir a integridade dos dados e
 * padronizar as regras de negócio associadas a cada modalidade de conta.
 * </p>
 *
 * @author Marcelo
 */
public enum AccountType {

    /**
     * Conta Corrente: Modalidade padrão para movimentações diárias,
     * saques e pagamentos.
     */
    CORRENTE("CORRENTE"),

    /**
     * Conta Poupança: Modalidade focada em reserva financeira e rendimentos.
     */
    POUPANCA("POUPANCA"),

    /**
     * Conta Eletrônica: Modalidade exclusiva para transações digitais
     * e pagamentos online.
     */
    ELETRONICA("ELETRONICA");

    private final String value;

    /**
     * Construtor do enum para associar o valor textual do tipo de conta.
     *
     * @param value O valor do tipo de conta utilizado na serialização JSON.
     */
    AccountType(String value) {
        this.value = value;
    }

    /**
     * @return O valor em português para ser usado no JSON.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Retorna uma descrição amigável do tipo de conta em português.
     * Útil para exibir em relatórios ou interfaces de usuário.
     *
     * @return A descrição formatada do tipo de conta.
     */
    public String getDescription() {
        return switch (this) {
            case CORRENTE -> "Conta Corrente";
            case POUPANCA -> "Conta Poupança";
            case ELETRONICA -> "Conta Eletronica";
        };
    }
}