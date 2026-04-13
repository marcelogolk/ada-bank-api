package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Serviço responsável pelas operações de gerenciamento de clientes.
 *
 * <p>Centraliza as regras de negócio relacionadas a cadastro, consulta
 * e atualização de clientes, mantendo os dados em memória.</p>
 */
@ApplicationScoped
public class CustomerService {

    /**
     * Armazena os clientes em memória, indexados pelo id.
     */
    private final Map<Long, Customer> customers = new ConcurrentHashMap<>();

    /**
     * Gera identificadores únicos para novos clientes.
     */
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Lista todos os clientes cadastrados, ordenados por id.
     *
     * @return lista de clientes.
     */
    public List<Customer> list() {
        return customers.values().stream()
                .sorted(Comparator.comparing(Customer::getId))
                .map(this::copy)
                .toList();
    }

    /**
     * Busca um cliente pelo id.
     *
     * @param id identificador do cliente.
     * @return cliente encontrado.
     * @throws NotFoundException quando o cliente não existe.
     */
    public Customer findById(Long id) {
        return copy(getRequiredCustomer(id));
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

        return customers.values().stream()
                .filter(customer -> customer.getEmail().equalsIgnoreCase(normalizedEmail))
                .findFirst()
                .map(this::copy)
                .orElseThrow(() -> new NotFoundException(
                        "Cliente com o email informado não foi encontrado"
                ));
    }

    /**
     * Cadastra um novo cliente após validar unicidade de CPF e email.
     *
     * @param customer dados do cliente a ser criado.
     * @return cliente criado.
     * @throws BadRequestException quando CPF ou email já estão em uso.
     */
    public Customer create(Customer customer) {
        validateUniqueCpf(customer.getCpf(), null);
        validateUniqueEmail(customer.getEmail(), null);

        long id = sequence.incrementAndGet();

        Customer newCustomer = new Customer(
                id,
                customer.getName(),
                customer.getCpf(),
                normalizeEmail(customer.getEmail()),
                customer.getPassword()
        );

        customers.put(id, newCustomer);
        return copy(newCustomer);
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

        return copy(existingCustomer);
    }

    /**
     * Retorna obrigatoriamente um cliente existente.
     *
     * @param id identificador do cliente.
     * @return cliente encontrado.
     * @throws NotFoundException quando o cliente não existe.
     */
    private Customer getRequiredCustomer(Long id) {
        Customer customer = customers.get(id);

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
        boolean duplicate = customers.values().stream()
                .anyMatch(customer -> customer.getCpf().equals(cpf)
                        && !customer.getId().equals(currentId));

        if (duplicate) {
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

        boolean duplicate = customers.values().stream()
                .anyMatch(customer -> customer.getEmail().equalsIgnoreCase(normalizedEmail)
                        && !customer.getId().equals(currentId));

        if (duplicate) {
            throw new BadRequestException("Já existe um cliente com o email informado");
        }
    }

    /**
     * Cria uma cópia defensiva do cliente.
     *
     * @param customer cliente original.
     * @return cópia do cliente.
     */
    private Customer copy(Customer customer) {
        return new Customer(
                customer.getId(),
                customer.getName(),
                customer.getCpf(),
                customer.getEmail(),
                customer.getPassword()
        );
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