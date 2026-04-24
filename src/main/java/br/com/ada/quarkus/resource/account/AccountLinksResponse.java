package br.com.ada.quarkus.resource.account;

/**
 * Representa os links relacionados a uma conta (HATEOAS simplificado).
 *
 * @param transacoes endpoint para consulta de transações da conta.
 */
public record AccountLinksResponse(
        String transacoes
) {
}