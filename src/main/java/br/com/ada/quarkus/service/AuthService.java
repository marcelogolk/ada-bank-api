package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class AuthService {

    @Inject
    CustomerService customerService;

    /**
     * Autentica um cliente com email e senha.
     *
     * Regra esperada:
     * - buscar cliente pelo email
     * - validar a senha informada
     * - devolver o cliente autenticado
     */
    public Customer authenticate(String email, String password) {
        Customer customer = customerService.findByEmail(email);

        validatePassword(customer, password);

        return customer;
    }

    /**
     * Valida se a senha informada corresponde à senha do cliente.
     */
    private void validatePassword(Customer customer, String password) {
        if (password == null || !customer.getPassword().equals(password)) {
            throw new BadRequestException("Email ou senha inválidos");
        }
    }
}