package br.com.ada.quarkus.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @NotBlank(message = "O nome do cliente é obrigatório")
    @Size(min = 3, max = 100, message = "O nome do cliente deve conter entre 3 e 100 caracteres")
    private String name;

    /** Cadastro de Pessoa Física (CPF).
     * Deve conter exatamente 11 dígitos numéricos.
     */
    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos numéricos")
    private String cpf;

    /** Endereço de e-mail eletrônico para contato e notificações. */
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email informado é inválido")
    private String email;

    /** Senha para login no sistema. */
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve conter pelo menos 6 caracteres")
    @JsonIgnore
    private String password;


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
     * @param cpf   O documento de identificação (CPF).
     * @param email O endereço de e-mail.
     * @param password A senha para login.
     *
     */
    public Customer(Long id, String name, String cpf, String email, String password) {
        this.id = id;
        this.name = name;
        this.cpf = cpf;
        this.email = email;
        this.password = password;
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
    public String getCpf() {
        return cpf;
    }

    /** @param cpf O novo CPF a ser definido. */
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    /** @return O e-mail do cliente. */
    public String getEmail() {
        return email;
    }

    /** @param email O novo e-mail a ser definido. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return A Senha de login do cliente. */
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    /** @param password A nova senha de login a ser definida. */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gera uma representação textual do objeto Customer (Cliente).
     * @return String contendo os dados do cliente.
     */
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    /**
     * Compara este cliente com outro objeto para verificar igualdade.
     * A igualdade é baseada no ID e no CPF.
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
                Objects.equals(cpf, customer.cpf);
    }

    /**
     * Gera o código hash para o cliente, consistente com o método equals.
     * @return Valor do hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, cpf);
    }
}