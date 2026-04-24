package br.com.ada.quarkus.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter JPA responsável por persistir {@link UserRole} como texto no banco
 * e reconstruir o enum a partir do valor armazenado.
 *
 * <p>Os valores persistidos seguem os textos definidos em {@link UserRole#getValue()},
 * como "GERENTE" e "CLIENTE".</p>
 *
 * @author Marcelo
 * @version 1.0
 */
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    /**
     * Converte o enum {@link UserRole} para o valor textual persistido no banco.
     *
     * @param role papel do usuário.
     * @return valor textual do papel ou {@code null}.
     */
    @Override
    public String convertToDatabaseColumn(UserRole role) {
        if (role == null) {
            return null;
        }

        return role.getValue();
    }

    /**
     * Converte o valor textual armazenado no banco para {@link UserRole}.
     *
     * @param dbData valor armazenado no banco.
     * @return enum correspondente ao valor armazenado.
     * @throws IllegalArgumentException quando o valor armazenado não corresponde a nenhum papel válido.
     */
    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        for (UserRole role : UserRole.values()) {
            if (role.getValue().equals(dbData)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Valor inválido para UserRole: " + dbData);
    }
}