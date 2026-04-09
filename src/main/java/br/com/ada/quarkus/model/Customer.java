package br.com.ada.quarkus.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * Representa um cliente dentro do sistema bancário da ADA.
 * <p>
 * Esta classe é o modelo base para armazenamento de informações pessoais,
 * contendo validações específicas para conformidade com dados cadastrais.
 * </p>
 *
 * @author Marcelo
 * @version 1.0
 */
public class Customer {

    /** Identificador único do cliente no banco de dados. */
    private Long id;

    /** Nome completo do cliente. Deve conter entre 3 e 100 caracteres. */
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    /** * Cadastro de Pessoa Física (CPF).
     * Deve conter exatamente 11 dígitos numéricos.
     */
    @NotBlank(message = "Tax ID is required")
    @Pattern(regexp = "\\d{11}", message = "Tax ID must be exactly 11 numeric digits (CPF format)")
    private String taxId;

    /** Endereço de e-mail eletrônico para contato e notificações. */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Construtor padrão (sem argumentos).
     * Necessário para frameworks de persistência e serialização JSON.
     */
    public Customer() {
    }

    /**
     * Construtor completo para inicialização de todos os campos.
     *
     * @param id    O identificador único.
     * @param name  O nome completo do cliente.
     * @param taxId O documento de identificação (CPF).
     * @param email O endereço de e-mail.
     */
    public Customer(Long id, String name, String taxId, String email) {
        this.id = id;
        this.name = name;
        this.taxId = taxId;
        this.email = email;
    }

    /** @return O ID do cliente. */
    public Long getId() {
        return id;
    }

    /** @param id O novo ID a ser definido. */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return O nome do cliente. */
    public String getName() {
        return name;
    }

    /** @param name O novo nome a ser definido. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return O CPF do cliente. */
    public String getTaxId() {
        return taxId;
    }

    /** @param taxId O novo CPF a ser definido. */
    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    /** @return O e-mail do cliente. */
    public String getEmail() {
        return email;
    }

    /** @param email O novo e-mail a ser definido. */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gera uma representação textual do objeto Customer.
     * @return String contendo os dados do cliente.
     */
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", taxId='" + taxId + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    /**
     * Compara este cliente com outro objeto para verificar igualdade.
     * A igualdade é baseada no ID e no TaxID (CPF).
     *
     * @param o Objeto a ser comparado.
     * @return true se forem iguais, false caso contrário.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) &&
                Objects.equals(taxId, customer.taxId);
    }

    /**
     * Gera o código hash para o cliente, consistente com o método equals.
     * @return Valor do hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, taxId);
    }
}