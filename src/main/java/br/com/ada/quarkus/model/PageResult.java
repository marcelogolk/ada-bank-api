package br.com.ada.quarkus.model;

import java.util.List;

/**
 * Representa um resultado paginado de qualquer consulta no sistema.
 * <p>
 * Este record encapsula os dados de uma página específica, incluindo
 * o conteúdo da página, número da página, tamanho da página e o total
 * de elementos disponíveis no banco de dados.
 * </p>
 * <p>
 * É utilizado para padronizar respostas de endpoints que retornam listas
 * grandes, permitindo navegação eficiente entre páginas sem sobrecarregar
 * a aplicação ou a rede.
 * </p>
 *
 * @param <T> O tipo genérico dos elementos contidos na página
 * @author Marcelo
 * @version 1.0
 */
public record PageResult<T>(
        /**
         * Lista de elementos da página atual.
         * Contém apenas os itens que pertencem à página solicitada.
         */
        List<T> content,

        /**
         * Número da página atual (0-indexed).
         * A primeira página tem número 0, a segunda página tem número 1, etc.
         */
        int page,

        /**
         * Quantidade máxima de elementos por página.
         * Define quantos itens são retornados em cada página.
         */
        int size,

        /**
         * Total de elementos disponíveis no banco de dados.
         * Independente da página solicitada, este valor permanece o mesmo.
         */
        long totalElements
) {

    /**
     * Calcula automaticamente o total de páginas disponíveis.
     * <p>
     * Este método realiza o cálculo dividindo o total de elementos pelo
     * tamanho da página e arredondando para cima, garantindo que a última
     * página contenha os elementos restantes.
     * </p>
     * <p>
     * <strong>Exemplo:</strong> Se há 150 elementos e o tamanho da página é 10,
     * o resultado será 15 páginas (0 a 14).
     * </p>
     *
     * @return O total de páginas disponíveis. Retorna 0 se o tamanho da página
     *         for menor ou igual a zero (situação anômala que não deveria ocorrer).
     */
    public int totalPages() {
        // Verifica se o tamanho da página é válido antes de fazer o cálculo
        if (size <= 0) {
            return 0;
        }
        // Calcula o total de páginas arredondando para cima
        return (int) Math.ceil((double) totalElements / size);
    }
}