package br.com.ada.quarkus.resource.account;

import br.com.ada.quarkus.model.AccountType;
import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String accountNumber,
        AccountType type,
        BigDecimal balance
) {
}