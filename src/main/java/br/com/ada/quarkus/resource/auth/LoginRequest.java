package br.com.ada.quarkus.resource.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Representa os dados necessários para autenticação de um cliente.
 *
 * @param email email do cliente.
 * @param password senha do cliente.
 */
public record LoginRequest(

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email informado é inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}