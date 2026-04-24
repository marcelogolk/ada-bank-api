package br.com.ada.quarkus.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Define os papéis/permissões disponíveis no sistema bancário.
 *
 * <p>Este enum é utilizado para controlar autorização e diferenciar
 * permissões administrativas de permissões de cliente.</p>
 *
 * <p>As constantes do enum usam nomes em inglês no código, enquanto os valores
 * serializados e persistidos seguem os papéis usados pela API: GERENTE e CLIENTE.</p>
 *
 * @author Marcelo
 * @version 1.0
 */
public enum UserRole {

    /**
     * Papel de gerente do banco.
     *
     * <p>Gerentes possuem acesso administrativo e podem gerenciar clientes,
     * contas e visualizar transações de qualquer conta.</p>
     */
    MANAGER("GERENTE"),

    /**
     * Papel de cliente do banco.
     *
     * <p>Clientes possuem acesso restrito aos próprios dados, contas
     * e transações.</p>
     */
    CUSTOMER("CLIENTE");

    private final String value;

    /**
     * Associa o valor textual usado no banco, no JWT e na serialização JSON.
     *
     * @param value valor textual do papel.
     */
    UserRole(String value) {
        this.value = value;
    }

    /**
     * Retorna o valor textual do papel.
     *
     * @return valor textual do papel, como "GERENTE" ou "CLIENTE".
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Retorna uma descrição amigável do papel.
     *
     * @return descrição formatada do papel.
     */
    public String getDescription() {
        return switch (this) {
            case MANAGER -> "Gerente do Banco";
            case CUSTOMER -> "Cliente do Banco";
        };
    }
}