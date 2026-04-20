package br.com.ada.quarkus.resource.transaction;

import br.com.ada.quarkus.model.TransactionType;
import br.com.ada.quarkus.resource.account.AccountResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa os dados completos de uma transação retornados pela API.
 *
 * @param id identificador da transação.
 * @param type tipo da transação (DEPOSITO, SAQUE, TRANSFERENCIA).
 * @param amount valor da transação.
 * @param dateTime data e hora da transação.
 * @param sourceAccount conta de origem (null para depósitos).
 * @param destinationAccount conta de destino (null para saques).
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