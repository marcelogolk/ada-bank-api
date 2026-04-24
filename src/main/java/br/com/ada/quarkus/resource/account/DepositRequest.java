package br.com.ada.quarkus.resource.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Representa os dados necessários para realizar um depósito.
 *
 * @param amount valor a ser depositado.
 */
public record DepositRequest(

        @NotNull(message = "O valor do depósito é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor do depósito deve ser maior que zero")
        BigDecimal amount
) {
}