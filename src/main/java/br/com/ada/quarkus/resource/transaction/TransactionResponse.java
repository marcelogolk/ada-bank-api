package br.com.ada.quarkus.resource.transaction;

import br.com.ada.quarkus.model.TransactionType;
import br.com.ada.quarkus.resource.account.AccountResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa os dados completos de uma transação retornados pela API.
 *
 * <p>Para depósitos, a conta de origem é nula.
 * Para saques, a conta de destino é nula.
 * Para transferências, ambas as contas são preenchidas.</p>
 *
 * @param id identificador da transação.
 * @param type tipo da transação.
 * @param amount valor da transação.
 * @param dateTime data e hora em que a transação foi processada.
 * @param sourceAccount conta de origem da operação, quando aplicável.
 * @param destinationAccount conta de destino da operação, quando aplicável.
 */
public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        LocalDateTime dateTime,
        AccountResponse sourceAccount,
        AccountResponse destinationAccount
) {
}