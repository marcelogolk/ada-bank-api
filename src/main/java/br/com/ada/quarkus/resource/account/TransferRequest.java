package br.com.ada.quarkus.resource.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Representa os dados necessários para realizar uma transferência.
 *
 * @param destinationAccountId identificador da conta de destino.
 * @param amount valor a ser transferido.
 */
public record TransferRequest(

        @NotNull(message = "O ID da conta de destino é obrigatório")
        Long destinationAccountId,

        @NotNull(message = "O valor da transferência é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor da transferência deve ser maior que zero")
        BigDecimal amount
) {
}