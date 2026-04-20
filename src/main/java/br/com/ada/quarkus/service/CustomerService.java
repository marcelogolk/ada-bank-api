package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.Customer;
import br.com.ada.quarkus.model.LoggedUser;
import br.com.ada.quarkus.model.PageResult;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

/**
 * Serviço responsável pelas operações de gerenciamento de clientes.
 *
 * <p>Centraliza as regras de negócio relacionadas a cadastro, consulta
 * e atualização de clientes, com persistência em banco de dados PostgreSQL.</p>
 *
 * @author Marcelo
 * @version 2.0
 */
@ApplicationScoped
public class CustomerService {

    @Inject
    CurrentUserService currentUserService;

    /**
     * Lista todos os clientes cadastrados com paginação, ordenados por id.
     *
     * @param page Número da página (0-indexed).
     * @param size Quantidade de clientes por página.
     * @return resultado paginado de clientes.
     */
    public PageResult<Customer> list(int page, int size) {
        var query = Customer.findAll(Sort.by("id"));
        var result = query.page(Page.of(page, size));

        return new PageResult<>(result.list(), page, size, result.count());
    }

    /**
     * Busca um cliente pelo id.
     *
     * @param id identificador do cliente.
     * @return cliente encontrado.
     * @throws NotFoundException quando o cliente não existe.
     */
    public Customer findById(Long id) {
        return getRequiredCustomer(id);
    }

    /**
     * Busca um cliente pelo email.
     *
     * @param email email do cliente.
     * @return cliente encontrado.
     * @throws NotFoundException quando nenhum cliente é encontrado para o email informado.
     */
    public Customer findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return (Customer) Customer.find("email", normalizedEmail).firstResultOptional()
                .orElseThrow(() -> new NotFoundException(
                        "Cliente com o email informado não foi encontrado"
                ));
    }
//    public Customer findByEmail(String email) {
//        String normalizedEmail = normalizeEmail(email);
//
//        return (Customer) Customer.find("email = ?1", normalizedEmail).firstResultOptional()
//                .orElseThrow(() -> new NotFoundException(
//                        "Cliente com o email informado não foi encontrado"
//                ));
//    }

    /**
     * Cadastra um novo cliente após validar unicidade de CPF e email.
     *
     * @param customer dados do cliente a ser criado.
     * @return cliente criado e persistido no banco.
     * @throws BadRequestException quando CPF ou email já estão em uso.
     */
    public Customer create(Customer customer) {
        validateUniqueCpf(customer.getCpf(), null);
        validateUniqueEmail(customer.getEmail(), null);

        Customer newCustomer = new Customer();
        newCustomer.setName(customer.getName());
        newCustomer.setCpf(customer.getCpf());
        newCustomer.setEmail(normalizeEmail(customer.getEmail()));
        newCustomer.setPassword(customer.getPassword());

        newCustomer.persist();

        return newCustomer;
    }

    /**
     * Atualiza os dados permitidos de um cliente.
     *
     * <p>O CPF não pode ser alterado após o cadastro.</p>
     *
     * @param id identificador do cliente.
     * @param name novo nome.
     * @param email novo email.
     * @param password nova senha.
     * @return cliente atualizado.
     * @throws NotFoundException quando o cliente não existe.
     * @throws BadRequestException quando o email já está em uso por outro cliente.
     */
    public Customer update(Long id, String name, String email, String password) {
        Customer existingCustomer = getRequiredCustomer(id);

        validateUniqueEmail(email, id);

        existingCustomer.setName(name);
        existingCustomer.setEmail(normalizeEmail(email));
        existingCustomer.setPassword(password);

        return existingCustomer;
    }

    /**
     * Retorna o usuário logado no momento.
     *
     * @return usuário logado.
     * @throws NotFoundException quando nenhum usuário está autenticado.
     */
    public LoggedUser loggedUser() {
        return currentUserService.getLoggedUser();
    }

    /**
     * Retorna obrigatoriamente um cliente existente.
     *
     * @param id identificador do cliente.
     * @return cliente encontrado.
     * @throws NotFoundException quando o cliente não existe.
     */
    private Customer getRequiredCustomer(Long id) {
        Customer customer = Customer.findById(id);

        if (customer == null) {
            throw new NotFoundException("Cliente não encontrado");
        }

        return customer;
    }

    /**
     * Valida se o CPF já está em uso por outro cliente.
     *
     * @param cpf CPF a validar.
     * @param currentId id do cliente atual em atualização, ou null em criação.
     * @throws BadRequestException quando o CPF já está cadastrado.
     */
    private void validateUniqueCpf(String cpf, Long currentId) {
        Customer existingCustomer = Customer.find("cpf", cpf).firstResult();

        if (existingCustomer != null && !existingCustomer.getId().equals(currentId)) {
            throw new BadRequestException("Já existe um cliente com o CPF informado");
        }
    }

    /**
     * Valida se o email já está em uso por outro cliente.
     *
     * @param email email a validar.
     * @param currentId id do cliente atual em atualização, ou null em criação.
     * @throws BadRequestException quando o email já está cadastrado.
     */
    private void validateUniqueEmail(String email, Long currentId) {
        String normalizedEmail = normalizeEmail(email);
        Customer existingCustomer = Customer.find("email", normalizedEmail).firstResult();

        if (existingCustomer != null && !existingCustomer.getId().equals(currentId)) {
            throw new BadRequestException("Já existe um cliente com o email informado");
        }
    }

    /**
     * Normaliza o email para comparação e armazenamento.
     *
     * @param email email informado.
     * @return email normalizado.
     */
    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase();
    }
}