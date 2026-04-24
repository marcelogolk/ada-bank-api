package br.com.ada.quarkus.model;

import java.util.List;

/**
 * Representa o resultado paginado de uma consulta.
 *
 * <p>Este record encapsula o conteúdo da página, o número da página,
 * o tamanho solicitado e o total de elementos disponíveis.</p>
 *
 * <p>É utilizado pela camada de serviço para padronizar consultas paginadas
 * antes da conversão para DTOs de resposta.</p>
 *
 * @param content lista de elementos da página atual.
 * @param page número da página atual, iniciando em 0.
 * @param size quantidade máxima de elementos por página.
 * @param totalElements total de elementos disponíveis.
 * @param <T> tipo dos elementos da página.
 *
 * @author Marcelo
 * @version 1.0
 */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {

    /**
     * Calcula o total de páginas disponíveis.
     *
     * @return total de páginas. Retorna 0 quando {@code size <= 0}.
     */
    public int totalPages() {
        if (size <= 0) {
            return 0;
        }

        return (int) Math.ceil((double) totalElements / size);
    }
}