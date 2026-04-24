package br.com.ada.quarkus.util;

/**
 * Classe utilitária responsável por aplicar máscaras de saída em dados sensíveis.
 *
 * <p>Esta classe deve ser utilizada apenas na camada de apresentação (DTOs/Responses),
 * nunca na persistência ou lógica de negócio.</p>
 *
 * <p>Os dados retornados continuam sendo os mesmos, apenas com formatação visual.</p>
 *
 * @author Marcelo
 * @version 1.0
 */
public final class OutputMaskFormatter {

    private OutputMaskFormatter() {
        // Evita instanciação
    }

    /**
     * Aplica máscara de CPF no formato XXX.XXX.XXX-XX.
     *
     * @param cpf CPF com 11 dígitos numéricos.
     * @return CPF formatado ou o valor original caso inválido.
     */
    public static String formatCpf(String cpf) {
        if (cpf == null) {
            return null;
        }

        if (!cpf.matches("\\d{11}")) {
            return cpf;
        }

        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    /**
     * Aplica máscara de número de conta no formato XXXXXXXXX-X.
     *
     * @param accountNumber número da conta com 10 dígitos.
     * @return número formatado ou o valor original caso inválido.
     */
    public static String formatAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return null;
        }

        if (!accountNumber.matches("\\d{10}")) {
            return accountNumber;
        }

        return accountNumber.replaceAll("(\\d{9})(\\d)", "$1-$2");
    }
}