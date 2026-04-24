package br.com.ada.quarkus.resource.auth;

/**
 * Representa a resposta retornada após autenticação bem-sucedida.
 *
 * <p>Contém o token JWT e dados públicos básicos do usuário autenticado.
 * Dados sensíveis, como CPF e senha, não são retornados nesta resposta.</p>
 *
 * <p>O token deve ser enviado nas requisições protegidas usando o header:</p>
 *
 * <pre>
 * Authorization: Bearer {token}
 * </pre>
 *
 * @param token token JWT gerado para autenticação.
 * @param email email do usuário autenticado.
 * @param name nome completo do usuário autenticado.
 */
public record TokenResponse(
        String token,
        String email,
        String name
) {
}