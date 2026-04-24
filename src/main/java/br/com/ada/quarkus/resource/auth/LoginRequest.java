package br.com.ada.quarkus.resource.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Representa os dados necessários para autenticação no sistema.
 *
 * <p>É utilizado no endpoint de login para receber e validar as credenciais
 * informadas pelo usuário.</p>
 *
 * @param email email do usuário, obrigatório e com formato válido.
 * @param password senha em texto puro informada no momento do login.
 */
public record LoginRequest(

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email informado é inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}