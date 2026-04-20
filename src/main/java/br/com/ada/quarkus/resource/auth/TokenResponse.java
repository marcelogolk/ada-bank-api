package br.com.ada.quarkus.resource.auth;

/**
 * Representa a resposta de autenticação contendo o token JWT.
 *
 * @param token token JWT gerado para autenticação.
 * @param email email do cliente autenticado.
 * @param name nome completo do cliente.
 * @param cpf CPF do cliente.
 */
public record TokenResponse(
        String token,
        String email,
        String name,
        String cpf
) {
}