package br.com.ada.quarkus.resource.customer;

/**
 * Representa os dados públicos de um cliente retornados pela API.
 *
 * @param id identificador do cliente.
 * @param name nome do cliente.
 * @param email email do cliente.
 */
public record CustomerResponse(
        Long id,
        String name,
        String email
) {
}