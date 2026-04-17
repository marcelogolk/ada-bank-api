package br.com.ada.quarkus.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Define os tipos de transações financeiras suportadas pelo sistema.
 * <p>
 * Este enum é utilizado para garantir a integridade dos dados e
 * padronizar as regras de negócio associadas a cada tipo de operação.
 * </p>
 *
 * @author Marcelo
 */
public enum TransactionType {

    /**
     * Representa a entrada de fundos em uma conta.
     * Utilizado para depósitos de clientes ou transferências recebidas.
     */
    DEPOSITO("DEPOSITO"),

    /**
     * Representa a retirada de fundos de uma conta.
     * Utilizado para saques em caixa ou transferências enviadas.
     */
    SAQUE("SAQUE"),

    /**
     * Representa a movimentação de fundos entre duas contas distintas.
     * Pode ser entre contas do mesmo cliente ou de clientes diferentes.
     */
    TRANSFERENCIA("TRANSFERENCIA");

    private final String value;

    /**
     * Construtor do enum para associar o valor textual do tipo de transação.
     *
     * @param value O valor do tipo de transação utilizado na serialização JSON.
     */
    TransactionType(String value) {
        this.value = value;
    }

    /**
     * @return O valor em português para ser usado no JSON.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Retorna uma descrição amigável do tipo de transação em português.
     * Útil para exibir em relatórios ou interfaces de usuário.
     *
     * @return A descrição formatada do tipo de transação.
     */
    public String getDescription() {
        return switch (this) {
            case DEPOSITO -> "Depósito em conta";
            case SAQUE -> "Saque de numerário";
            case TRANSFERENCIA -> "Transferência entre contas";
        };
    }
}