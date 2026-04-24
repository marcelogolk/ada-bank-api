package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.Customer;
import br.com.ada.quarkus.model.LoggedUser;
import br.com.ada.quarkus.resource.auth.TokenResponse;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;

/**
 * Serviço responsável pela autenticação de clientes.
 *
 * <p>Valida as credenciais informadas com base no email e na senha
 * cadastrados no sistema, gera tokens JWT e extrai informações do usuário
 * autenticado a partir do token.</p>
 *
 * @author Marcelo
 * @version 2.0
 */
@ApplicationScoped
public class AuthService implements CurrentUserService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Inject
    CustomerService customerService;

    @Inject
    PasswordService passwordService;

    @Inject
    JsonWebToken jwt;

    /**
     * Retorna o usuário logado extraindo informações do token JWT.
     *
     * @return usuário logado com informações do token.
     * @throws NotAuthorizedException quando nenhum usuário está autenticado.
     */
    @Override
    public LoggedUser getLoggedUser() {
        if (jwt.getName() == null) {
            throw new NotAuthorizedException("Nenhum usuário autenticado na requisição atual");
        }

        return new LoggedUser(
                getUserId(),
                jwt.getName(),
                jwt.getClaim("cpf"),
                getRole()
        );
    }

    /**
     * Extrai o ID do usuário a partir do token JWT.
     *
     * @return ID do usuário.
     */
    private Long getUserId() {
        return Long.parseLong(jwt.getClaim("userId").toString());
    }

    /**
     * Extrai o papel do usuário a partir do token JWT.
     *
     * @return papel do usuário (GERENTE ou CLIENTE).
     */
    private String getRole() {
        return jwt.getGroups()
                .stream()
                .findFirst()
                .orElse("CLIENTE");
    }

    /**
     * Realiza login do cliente validando email e senha.
     *
     * <p>Valida as credenciais, gera um token JWT e retorna os dados
     * do cliente autenticado junto com o token.</p>
     *
     * @param email email do cliente.
     * @param password senha do cliente.
     * @return resposta contendo token e dados do cliente.
     * @throws NotAuthorizedException quando as credenciais são inválidas.
     */
    public TokenResponse login(String email, String password) {
        Customer customer = customerService.findByEmail(email);

        validatePassword(customer, password);

        String token = generateToken(customer);

        return new TokenResponse(
                token,
                customer.getEmail(),
                customer.getName(),
                customer.getCpf()
        );
    }

    /**
     * Valida se a senha informada corresponde à senha do cliente.
     *
     * <p>Utiliza o {@link PasswordService} para verificação segura com Argon2.</p>
     *
     * @param customer cliente encontrado.
     * @param password senha informada.
     * @throws NotAuthorizedException quando a senha é inválida.
     */
    private void validatePassword(Customer customer, String password) {
        boolean isValid = customer != null
                && passwordService.verify(customer.getPassword(), password);

        if (!isValid) {
            throw new NotAuthorizedException("Credenciais inválidas");
        }
    }

    /**
     * Gera um token JWT para o cliente autenticado.
     *
     * <p>O token contém informações do cliente e expira em 30 minutos.</p>
     *
     * @param customer cliente autenticado.
     * @return token JWT gerado.
     */
    private String generateToken(Customer customer) {
        return Jwt.issuer(issuer)
                .upn(customer.getEmail())
                .groups(customer.getRole().getValue())
                .claim("userId", customer.getId())
                .claim("cpf", customer.getCpf())
                .expiresIn(Duration.ofMinutes(30))
                .sign();
    }
}