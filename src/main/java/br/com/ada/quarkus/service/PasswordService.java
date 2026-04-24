package br.com.ada.quarkus.service;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Serviço responsável pela proteção de senhas com Argon2.
 *
 * <p>Centraliza a geração de hash e a verificação de senhas,
 * evitando duplicação de lógica entre os serviços de autenticação
 * e gerenciamento de clientes.</p>
 *
 * <p>Utiliza o algoritmo Argon2id, apropriado para armazenamento seguro
 * de senhas em aplicações modernas.</p>
 *
 * @author Marcelo
 * @version 2.0
 */
@ApplicationScoped
public class PasswordService {

    private Argon2 argon2;

    /**
     * Inicializa a instância do Argon2.
     *
     * <p>Executado uma única vez após a construção da classe.</p>
     */
    @PostConstruct
    void init() {
        argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    /**
     * Gera hash Argon2 para uma senha em texto puro.
     *
     * @param rawPassword senha em texto puro.
     * @return hash Argon2 da senha.
     */
    public String hash(String rawPassword) {
        return argon2.hash(2, 65536, 1, rawPassword.toCharArray());
    }

    /**
     * Verifica se a senha em texto puro corresponde ao hash armazenado.
     *
     * @param hashedPassword hash armazenado no banco.
     * @param rawPassword senha informada em texto puro.
     * @return {@code true} quando a senha é válida; {@code false} caso contrário.
     */
    public boolean verify(String hashedPassword, String rawPassword) {
        return hashedPassword != null
                && rawPassword != null
                && argon2.verify(hashedPassword, rawPassword.toCharArray());
    }
}