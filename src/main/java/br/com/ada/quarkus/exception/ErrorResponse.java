package br.com.ada.quarkus.exception;

/**
 * Representa a estrutura padrão de erro retornada pela API.
 *
 * <p>Este DTO é utilizado por todos os ExceptionMappers para garantir
 * consistência nas respostas de erro.</p>
 *
 * @param status código HTTP do erro.
 * @param error descrição padrão do erro (ex: Bad Request, Forbidden).
 * @param message mensagem detalhada da exceção.
 * @param path caminho da requisição que originou o erro.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path
) {
}