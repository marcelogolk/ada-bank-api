package br.com.ada.quarkus.resource.account;

/**
 * Representa um resumo dos dados de um cliente.
 *
 * <p>Utilizado para evitar exposição de dados sensíveis
 * em respostas da API.</p>
 *
 * @param id identificador do cliente.
 * @param name nome do cliente.
 * @param email email do cliente.
 */
public record CustomerSummaryResponse(
        Long id,
        String name,
        String email
) {
}