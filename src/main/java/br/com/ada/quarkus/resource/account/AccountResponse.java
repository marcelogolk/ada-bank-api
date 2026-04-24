package br.com.ada.quarkus.resource.account;

import br.com.ada.quarkus.model.Account;
import br.com.ada.quarkus.model.AccountType;
import br.com.ada.quarkus.util.OutputMaskFormatter;

import java.math.BigDecimal;

/**
 * Representa os dados básicos de uma conta bancária.
 *
 * <p>Utilizado em respostas simples onde não é necessário
 * retornar detalhes completos da conta.</p>
 *
 * @param id identificador da conta.
 * @param accountNumber número da conta formatado para exibição.
 * @param type tipo da conta.
 * @param balance saldo atual da conta.
 */
public record AccountResponse(
        Long id,
        String accountNumber,
        AccountType type,
        BigDecimal balance
) {

    /**
     * Converte uma entidade {@link Account} em {@link AccountResponse}.
     *
     * @param account entidade de conta.
     * @return resposta com dados públicos da conta.
     */
    public static AccountResponse fromEntity(Account account) {
        return new AccountResponse(
                account.getId(),
                OutputMaskFormatter.formatAccountNumber(account.getAccountNumber()),
                account.getType(),
                account.getBalance()
        );
    }
}