package br.com.ada.quarkus.resource;

import java.util.List;
import br.com.ada.quarkus.model.PageResult;
import java.util.function.Function;

/**
 * Representa uma resposta paginada da API.
 *
 * <p>Utilizado para padronizar o retorno de listas com paginação,
 * desacoplando a camada de serviço ({@link PageResult}) da camada de apresentação.</p>
 *
 * @param content lista de elementos da página atual.
 * @param page número da página atual (iniciando em 0).
 * @param size quantidade de elementos por página.
 * @param totalElements total de elementos disponíveis.
 * @param totalPages total de páginas disponíveis.
 *
 * @param <T> tipo dos elementos retornados.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * Converte um {@link PageResult} em {@link PageResponse},
     * aplicando um mapper para transformar os dados de domínio em DTOs.
     *
     * @param result resultado paginado da camada de serviço.
     * @param mapper função de transformação de domínio para DTO.
     * @param <D> tipo de entrada (domínio).
     * @param <R> tipo de saída (response).
     * @return resposta paginada convertida.
     */
    public static <D, R> PageResponse<R> from(PageResult<D> result, Function<D, R> mapper) {

        if (result == null) {
            throw new IllegalArgumentException("PageResult não pode ser null");
        }

        List<R> mapped = result.content()
                .stream()
                .map(mapper)
                .toList();

        return new PageResponse<>(
                mapped,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}