package br.com.ada.quarkus.util;

public class OutputMaskFormatter {
    private OutputMaskFormatter() {
    }

    public static String formatCpf(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return cpf;
        }

        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    public static String formatAccountNumber(String accountNumber) {
        if (accountNumber == null || !accountNumber.matches("\\d{10}")) {
            return accountNumber;
        }

        return accountNumber.replaceAll("(\\d{9})(\\d)", "$1-$2");
    }
}
