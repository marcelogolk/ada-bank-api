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

@ApplicationScoped
public class CustomerService {

    /**
     * Armazena os clientes em memória.
     * Chave: id do cliente
     * Valor: objeto Customer
     */
    private final Map<Long, Customer> customers = new ConcurrentHashMap<>();

    /**
     * Gera Identificadores únicos para novos clientes.
     */
    private final AtomicLong sequence = new AtomicLong();

    /**
     * Lista todos os clientes cadastrados.
     *
     * Regra esperada:
     * - retornar os clientes ordenados
     * - devolver cópias, não os objetos internos
     */
    public List<Customer> list(){
        return customers.values().stream()
                .sorted(Comparator.comparing(Customer::getId))
                .map(this:: copy)
                .toList();
    }

    /**
     * Busca um cliente pelo ID.
     *
     * Regra esperada:
     * - se não existir, lançar NotFoundException
     * - se existir, devolver cópia
     */
    public Customer findById(Long id){
        return copy(getRequiredCustomer(id));
    }

    /**
     * Busca um cliente pelo email.
     *
     * Regra esperada:
     * - normalizar o email antes da busca
     * - se não existir, lançar NotFoundException
     * - se existir, devolver cópia
     */
    public Customer findByEmail(String email){
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
     * Cria um novo cliente.
     *
     * Regra esperada:
     * - validar CPF único
     * - validar email único
     * - gerar ID
     * - criar um novo objeto Customer com o ID gerado
     * - armazenar no Map
     * - devolver cópia
     */
    public Customer create(Customer customer){
        validateUniqueCpf(customer.getCpf(), null);
        validateUniqueEmail(customer.getEmail(), null);
        long id = sequence.incrementAndGet();
        Customer newCustumer = new Customer(
                id,
                customer.getName(),
                customer.getCpf(),
                normalizeEmail(customer.getEmail()),
                customer.getPassword()
        );
        customers.put(id, newCustumer);
        return copy(newCustumer);
    }

    /**
     * Atualiza um cliente existente.
     *
     * Regra esperada:
     * - cliente precisa existir
     * - CPF não pode ser alterado
     * - email deve continuar único
     * - nome, email e senha podem ser alterados
     * - devolver cópia do cliente atualizado
     */
    public Customer update(Long id, Customer customer){
        Customer existingCustomer =  getRequiredCustomer(id);

        if (!existingCustomer.getCpf().equals(customer.getCpf())){
            throw new BadRequestException("O CPF não pode ser alterado");
        }
        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPassword(customer.getPassword());
        return copy(existingCustomer);
    }

    /**
     * Busca um cliente por ID e exige que ele exista.
     *
     * Uso:
     * - evitar repetição de código em findById e update
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
     * currentId:
     * - null no create
     * - id do cliente atual no update
     */
    private void validateUniqueCpf(String cpf, Long currentId) {
        boolean duplicate = customers.values().stream()
                .anyMatch(customer -> customer.getCpf()
                        .equals(cpf) && !customer.getId().equals(currentId));
        if (duplicate) {
            throw new BadRequestException("Já existe um cliente com o CPF informado");
        }
    }

    /**
     * Valida se o email já está em uso por outro cliente.
     *
     * currentId:
     * - null no create
     * - id do cliente atual no update
     */
    private void validateUniqueEmail(String email, Long currentId){
        String normalizedEmail = normalizeEmail(email);
        boolean duplicate = customers.values().stream()
                .anyMatch(customer -> customer.getEmail()
                        .equalsIgnoreCase(normalizedEmail)
                        && !customer.getId().equals(currentId));

        if (duplicate) {
            throw new BadRequestException("Já existe um cliente com o email informado");
        }
    }

    /**
     * Cria uma cópia do cliente.
     *
     * Objetivo:
     * - proteger o estado interno do Map
     */
    private Customer copy(Customer customer){
        return new Customer(
                customer.getId(),
                customer.getName(),
                customer.getCpf(),
                customer.getEmail(),
                customer.getPassword());
    }

    /**
     * Normaliza o email para comparação e armazenamento.
     *
     * Regra:
     * - remove espaços nas extremidades
     * - converte para minúsculas
     */
    private String normalizeEmail(String email){
        if (email== null){
            return null;
        }
        return email.trim().toLowerCase();

    }
}
