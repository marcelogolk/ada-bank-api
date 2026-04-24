package br.com.ada.quarkus.resource.auth;

import br.com.ada.quarkus.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Recurso responsável pelos endpoints de autenticação.
 *
 * <p>Recebe requisições HTTP de login, valida as credenciais por meio da
 * camada de serviço e retorna um token JWT para acesso aos endpoints protegidos.</p>
 *
 * @author Marcelo
 * @version 2.0
 */
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    /**
     * Realiza autenticação de um usuário.
     *
     * <p>Endpoint público que recebe email e senha, valida as credenciais
     * e retorna um token JWT quando a autenticação é bem-sucedida.</p>
     *
     * <p>O token retornado deve ser utilizado no header {@code Authorization}
     * das próximas requisições protegidas.</p>
     *
     * @param request dados de autenticação contendo email e senha.
     * @return resposta HTTP 200 contendo o token JWT e dados públicos do usuário autenticado.
     */
    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) {
        TokenResponse tokenResponse = authService.login(
                request.email(),
                request.password()
        );

        return Response.ok(tokenResponse).build();
    }
}