package br.com.ada.quarkus.model;

/**
 * Representa um usuário autenticado no sistema bancário.
 * <p>
 * Este record é imutável e armazena apenas dados públicos do usuário
 * após autenticação bem-sucedida. A senha é deliberadamente omitida
 * por questões de segurança.
 * </p>
 * <p>
 * É utilizado para armazenar informações do usuário dentro do JWT Token,
 * permitindo validação rápida de permissões nos endpoints protegidos.
 * </p>
 *
 * @author Marcelo
 * @version 2.0
 */
public record LoggedUser(
        /**
         * Identificador único do usuário autenticado.
         */
        Long id,

        /**
         * Email do usuário utilizado para login.
         * Armazenado como upn (username principal) no JWT.
         */
        String email,

        /**
         * CPF do usuário para identificação.
         */
        String cpf,

        /**
         * Papel/permissão do usuário no sistema.
         * Valores possíveis: "GERENTE" ou "CLIENTE".
         * Armazenado como grupos no JWT Token.
         */
        String role
) {

    /**
     * Verifica se o usuário autenticado possui papel de gerente.
     * <p>
     * Gerentes têm acesso administrativo ao sistema e podem acessar
     * qualquer conta bancária e visualizar todas as transações.
     * </p>
     *
     * @return true se o usuário for gerente, false caso contrário
     */
    public boolean isManager() {
        return "GERENTE".equals(role);
    }

    /**
     * Verifica se o usuário autenticado possui papel de cliente.
     * <p>
     * Clientes têm acesso restrito apenas à sua própria conta bancária
     * e podem visualizar apenas suas próprias transações.
     * </p>
     *
     * @return true se o usuário for cliente, false caso contrário
     */
    public boolean isCustomer() {
        return "CLIENTE".equals(role);
    }
}