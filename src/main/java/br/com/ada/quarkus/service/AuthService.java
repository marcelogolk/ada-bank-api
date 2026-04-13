package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

/**
 * Serviço responsável pela autenticação de clientes.
 *
 * <p>Valida as credenciais informadas com base no email e na senha
 * cadastrados no sistema.</p>
 */
@ApplicationScoped
public class AuthService {

    @Inject
    CustomerService customerService;

    /**
     * Autentica um cliente por email e senha.
     *
     * @param email email informado.
     * @param password senha informada.
     * @return cliente autenticado.
     * @throws BadRequestException quando as credenciais são inválidas.
     */
    public Customer authenticate(String email, String password) {
        Customer customer = customerService.findByEmail(email);

        validatePassword(customer, password);

        return customer;
    }

    /**
     * Valida se a senha informada corresponde à senha do cliente.
     *
     * @param customer cliente encontrado.
     * @param password senha informada.
     * @throws BadRequestException quando a senha é inválida.
     */
    private void validatePassword(Customer customer, String password) {
        if (password == null || !customer.getPassword().equals(password)) {
            throw new BadRequestException("Email ou senha inválidos");
        }
    }
}