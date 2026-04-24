package br.com.ada.quarkus.model;

/**
 * Representa o usuário autenticado na aplicação.
 *
 * <p>Este record é imutável e armazena os dados extraídos do token JWT
 * durante uma requisição autenticada.</p>
 *
 * <p>A senha é deliberadamente omitida por segurança.</p>
 *
 * @param id identificador único do usuário autenticado.
 * @param email email utilizado como principal no JWT.
 * @param cpf CPF do usuário autenticado.
 * @param role papel/permissão do usuário no sistema.
 *
 * @author Marcelo
 * @version 2.0
 */
public record LoggedUser(
        Long id,
        String email,
        String cpf,
        String role
) {

    /**
     * Verifica se o usuário autenticado possui papel de gerente.
     *
     * @return {@code true} se o usuário for gerente; {@code false} caso contrário.
     */
    public boolean isManager() {
        return "GERENTE".equals(role);
    }

    /**
     * Verifica se o usuário autenticado possui papel de cliente.
     *
     * @return {@code true} se o usuário for cliente; {@code false} caso contrário.
     */
    public boolean isCustomer() {
        return "CLIENTE".equals(role);
    }
}