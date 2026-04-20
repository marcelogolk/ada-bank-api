package br.com.ada.quarkus.resource.auth;

import br.com.ada.quarkus.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Recurso responsável pelos endpoints de autenticação.
 *
 * <p>Recebe as requisições HTTP de login e retorna um token JWT
 * para autenticação em endpoints protegidos.</p>
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
     * Realiza autenticação de um cliente.
     *
     * <p>Endpoint público que valida as credenciais (email e senha)
     * e retorna um token JWT válido por 30 minutos.</p>
     *
     * @param request dados de autenticação (email e senha).
     * @return resposta contendo o token JWT e dados do cliente.
     * @throws NotAuthorizedException quando as credenciais são inválidas.
     */
    @POST
    @Path("/login")
    @PermitAll
    @Transactional
    public TokenResponse login(@Valid LoginRequest request) {
        return authService.login(request.email(), request.password());
    }
}