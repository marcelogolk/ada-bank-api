package br.com.ada.quarkus.resource.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Representa os dados permitidos para atualização de um cliente.
 *
 * <p>O CPF não faz parte deste request, pois não pode ser alterado
 * após o cadastro.</p>
 *
 * @param name novo nome do cliente.
 * @param email novo email do cliente.
 * @param password nova senha do cliente.
 */
public record UpdateCustomerRequest(

        @NotBlank(message = "O nome do cliente é obrigatório")
        @Size(min = 3, max = 100, message = "O nome do cliente deve conter entre 3 e 100 caracteres")
        String name,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email informado é inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve conter pelo menos 6 caracteres")
        String password
) {
}