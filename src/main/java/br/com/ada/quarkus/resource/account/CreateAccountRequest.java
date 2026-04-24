package br.com.ada.quarkus.resource.account;

import br.com.ada.quarkus.model.AccountType;
import jakarta.validation.constraints.NotNull;

/**
 * Representa os dados necessários para criação de uma conta.
 *
 * @param type tipo da conta a ser criada.
 * @param customerId identificador do cliente proprietário.
 */
public record CreateAccountRequest(

        @NotNull(message = "O tipo da conta é obrigatório")
        AccountType type,

        @NotNull(message = "O ID do cliente é obrigatório")
        Long customerId
) {
}