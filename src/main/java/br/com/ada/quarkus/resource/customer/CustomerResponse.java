package br.com.ada.quarkus.resource.customer;

import br.com.ada.quarkus.model.UserRole;

/**
 * Representa os dados públicos de um cliente retornados pela API.
 *
 * <p>Dados sensíveis como CPF e senha não são expostos nesta resposta.</p>
 *
 * @param id identificador do cliente.
 * @param name nome do cliente.
 * @param email email do cliente.
 */
public record CustomerResponse(
        Long id,
        String name,
        String email
    ){

}