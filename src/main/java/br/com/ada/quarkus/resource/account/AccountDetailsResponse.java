package br.com.ada.quarkus.resource.account;

import br.com.ada.quarkus.model.AccountType;
import br.com.ada.quarkus.resource.transaction.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Representa os dados completos de uma conta bancária.
 *
 * <p>Inclui informações da conta, dados do titular,
 * transações recentes e links relacionados.</p>
 *
 * @param id identificador da conta.
 * @param accountNumber número da conta formatado para exibição.
 * @param type tipo da conta.
 * @param balance saldo atual da conta.
 * @param holder dados resumidos do titular da conta.
 * @param transactions lista de transações recentes.
 * @param _links links relacionados ao recurso.
 */
public record AccountDetailsResponse(
        Long id,
        String accountNumber,
        AccountType type,
        BigDecimal balance,
        CustomerSummaryResponse holder,
        List<TransactionResponse> transactions,
        AccountLinksResponse _links
) {
}