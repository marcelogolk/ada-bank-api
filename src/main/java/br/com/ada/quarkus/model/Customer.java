package br.com.ada.quarkus.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
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
 * Utiliza otimistic locking através da anotação @Version para garantir
 * consistência em operações concorrentes.
 * </p>
 *
 * @author Marcelo
 * @version 1.0
 */
@Entity
@Table(name = "customer")
public class Customer extends PanacheEntityBase {

    /**
     * Identificador único do cliente no banco de dados.
     * Gerado automaticamente pelo banco (auto-increment).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do cliente.
     * Deve conter entre 3 e 100 caracteres alfabéticos.
     * Campo obrigatório e mapeado para a coluna "name" do banco.
     */
    @NotBlank(message = "O nome do cliente é obrigatório")
    @Size(min = 3, max = 100, message = "O nome do cliente deve conter entre 3 e 100 caracteres")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Cadastro de Pessoa Física (CPF) do cliente.
     * Deve conter exatamente 11 dígitos numéricos, sem formatação.
     * Campo obrigatório, único e mapeado para a coluna "cpf" do banco.
     */
    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos numéricos")
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    /**
     * Endereço de e-mail eletrônico do cliente.
     * Utilizado para contato, notificações e autenticação.
     * Campo obrigatório, único e validado como e-mail válido.
     */
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email informado é inválido")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Senha para autenticação do cliente no sistema.
     * Campo obrigatório com mínimo de 6 caracteres.
     * Não é serializado em respostas JSON por segurança (@JsonIgnore).
     */
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve conter pelo menos 6 caracteres")
    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Versão do registro para controle de concorrência (Optimistic Locking).
     * Incrementado automaticamente a cada atualização pelo Hibernate.
     * Previne conflitos em operações concorrentes.
     */
    @Version
    private Long version;

    /**
     * Construtor padrão (sem argumentos).
     * Necessário para frameworks de persistência (Hibernate/JPA) e serialização JSON (Jackson).
     */
    public Customer() {
    }

    /**
     * Construtor completo para inicialização de todos os campos principais.
     * A versão é inicializada automaticamente pelo Hibernate.
     *
     * @param id       O identificador único do cliente.
     * @param name     O nome completo do cliente.
     * @param cpf      O documento de identificação (CPF) com 11 dígitos.
     * @param email    O endereço de e-mail eletrônico.
     * @param password A senha para autenticação no sistema.
     */
    public Customer(Long id, String name, String cpf, String email, String password) {
        this.id = id;
        this.name = name;
        this.cpf = cpf;
        this.email = email;
        this.password = password;
    }

    // ========== Getters e Setters ==========

    /** @return O identificador único do cliente. */
    public Long getId() {
        return id;
    }

    /** @param id O novo identificador do cliente. */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return O nome completo do cliente. */
    public String getName() {
        return name;
    }

    /** @param name O novo nome do cliente. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return O CPF do cliente (11 dígitos). */
    public String getCpf() {
        return cpf;
    }

    /** @param cpf O novo CPF do cliente. */
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    /** @return O e-mail do cliente. */
    public String getEmail() {
        return email;
    }

    /** @param email O novo e-mail do cliente. */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retorna a senha do cliente.
     * <p>
     * <strong>Nota de Segurança:</strong> Esta senha NÃO é serializada em respostas JSON
     * devido à anotação @JsonIgnore no atributo.
     * </p>
     *
     * @return A senha de autenticação do cliente.
     */
    public String getPassword() {
        return password;
    }

    /** @param password A nova senha de autenticação. */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @return A versão atual do registro (Optimistic Locking). */
    public Long getVersion() {
        return version;
    }

    // ========== Métodos Object ==========

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", version=" + version +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) &&
                Objects.equals(cpf, customer.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cpf);
    }
}