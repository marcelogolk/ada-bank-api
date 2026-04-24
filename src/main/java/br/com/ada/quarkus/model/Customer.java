package br.com.ada.quarkus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.Objects;

/**
 * Representa um cliente dentro do sistema bancário da ADA.
 *
 * <p>Esta classe armazena informações cadastrais do cliente e aplica validações
 * de integridade para CPF, email, senha e papel de acesso.</p>
 *
 * <p>Utiliza optimistic locking através da anotação {@link Version} para reduzir
 * conflitos em operações concorrentes.</p>
 *
 * @author Marcelo
 * @version 1.0
 */
@Entity
@Table(name = "customer")
public class Customer extends PanacheEntityBase {

    /**
     * Identificador único do cliente no banco de dados.
     * Gerado automaticamente pelo banco.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome completo do cliente.
     */
    @NotBlank(message = "O nome do cliente é obrigatório")
    @Size(min = 3, max = 100, message = "O nome do cliente deve conter entre 3 e 100 caracteres")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * CPF do cliente, armazenado com exatamente 11 dígitos numéricos e sem máscara.
     */
    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos numéricos")
    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    /**
     * Email do cliente.
     * Utilizado para autenticação e comunicação.
     */
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email informado é inválido")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Hash da senha do cliente.
     *
     * <p>A senha em texto puro nunca deve ser persistida. O valor armazenado
     * deve ser gerado pela camada de serviço usando Argon2.</p>
     *
     * <p>Este campo não é serializado em respostas JSON por segurança.</p>
     */
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve conter pelo menos 6 caracteres")
    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Papel/permissão do cliente no sistema.
     * Valores persistidos: GERENTE ou CLIENTE.
     */
    @NotNull(message = "O papel do cliente é obrigatório")
    @Column(name = "role", nullable = false)
    @Convert(converter = UserRoleConverter.class)
    private UserRole role;

    /**
     * Versão do registro para controle de concorrência.
     */
    @Version
    private Long version;

    /**
     * Construtor padrão necessário para JPA/Hibernate.
     */
    public Customer() {
    }

    /**
     * Construtor para criação de cliente.
     *
     * <p>Todo novo cliente criado por este fluxo nasce com papel CUSTOMER,
     * que é persistido como CLIENTE no banco.</p>
     *
     * @param id identificador único do cliente.
     * @param name nome completo do cliente.
     * @param cpf CPF com 11 dígitos.
     * @param email endereço de email.
     * @param password senha ou hash da senha, conforme fluxo de criação.
     */
    public Customer(Long id, String name, String cpf, String email, String password) {
        this.id = id;
        this.name = name;
        this.cpf = cpf;
        this.email = email;
        this.password = password;
        this.role = UserRole.CUSTOMER;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
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