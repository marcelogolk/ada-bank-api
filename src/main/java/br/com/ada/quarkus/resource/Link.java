package br.com.ada.quarkus.resource;

/**
 * Representa um link HATEOAS para navegação entre recursos da API.
 *
 * <p>Utilizado para indicar ações disponíveis relacionadas ao recurso atual.</p>
 *
 * @param rel tipo da relação do link (ex: self, update, delete).
 * @param href URL do recurso.
 * @param method método HTTP a ser utilizado (GET, POST, etc).
 */
public record Link(
        String rel,
        String href,
        String method
) {
}