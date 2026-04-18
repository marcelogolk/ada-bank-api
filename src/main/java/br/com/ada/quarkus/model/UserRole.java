package br.com.ada.quarkus.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Define os papéis/permissões disponíveis no sistema bancário.
 * <p>
 * Este enum é utilizado para controlar o acesso e as operações permitidas
 * para cada tipo de usuário no sistema. Garante a integridade das regras
 * de autorização através de uma enumeração de valores válidos.
 * </p>
 * <p>
 * Os papéis são armazenados como String no banco de dados para facilitar
 * a serialização em JWT Tokens e respostas JSON.
 * </p>
 *
 * @author Marcelo
 * @version 1.0
 */
public enum UserRole {

    /**
     * Papel de gerente do banco.
     * Gerentes possuem acesso administrativo total ao sistema e podem:
     * - Acessar qualquer conta bancária
     * - Visualizar todas as transações
     * - Gerenciar clientes e contas
     */
    MANAGER("GERENTE"),

    /**
     * Papel de cliente do banco.
     * Clientes possuem acesso restrito apenas à sua própria conta e podem:
     * - Acessar apenas sua própria conta bancária
     * - Visualizar apenas suas próprias transações
     * - Realizar operações limitadas à sua conta
     */
    CUSTOMER("CLIENTE");

    /**
     * Valor em texto do papel para serialização em JSON e armazenamento no banco.
     */
    private final String value;

    /**
     * Construtor do enum para associar o valor textual do papel.
     *
     * @param value O valor do papel utilizado na serialização JSON e banco de dados.
     */
    UserRole(String value) {
        this.value = value;
    }

    /**
     * Retorna o valor em texto do papel para serialização JSON.
     * <p>
     * Esta anotação garante que ao serializar um objeto contendo este enum,
     * o valor retornado será a String associada, não o nome da constante.
     * </p>
     *
     * @return O valor em texto do papel (ex: "GERENTE" ou "CLIENTE").
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Retorna uma descrição amigável do papel em português.
     * <p>
     * Útil para exibir em relatórios, interfaces de usuário ou logs
     * de forma mais legível para usuários finais.
     * </p>
     *
     * @return A descrição formatada do papel.
     */
    public String getDescription() {
        return switch (this) {
            case MANAGER -> "Gerente do Banco";
            case CUSTOMER -> "Cliente do Banco";
        };
    }
}