package br.com.ada.quarkus.resource.account;

import br.com.ada.quarkus.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        BigDecimal currentBalance,
        LocalDateTime dateTime
) {
}