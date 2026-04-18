# Decisões de Projeto: ada-bank-api vs meeting-room

**Data:** Abril 2026  
**Projeto:** ADA Bank API  
**Desenvolvedor:** Marcelo  
**Versão:** 1.0

---

## 1. ESTRUTURA DE PACOTES

### Decisão: Manter DTOs em `resource/` (não em `dto/`)

**Template (meeting-room):**
resource/ 
├── auth/
│├── AuthResource.java
│├── LoginRequest.java
│└── TokenResponse.java
│
│├── room/
│├── RoomResource.java
│├── CreateRoomRequest.java
│└── RoomResponse.java

**Seu Projeto (ada-bank-api):**
resource/ 
├── auth/ 
│ ├── AuthResource.java 
│ ├── AuthRequest.java 
│ └── AuthResponse.java 
├── customer/ 
│ ├── CustomerResource.java 
│ ├── CreateCustomerRequest.java 
│ ├── UpdateCustomerRequest.java 
│ └── CustomerResponse.java 
├── account/ 
│ ├── AccountResource.java 
│ ├── CreateAccountRequest.java 
│ ├── UpdateAccountRequest.java 
│ └── AccountResponse.java 
├── transaction/ 
│ ├── TransactionResource.java 
│ ├── CreateTransactionRequest.java 
│ └── TransactionResponse.java

**Justificativa:** DTOs são parte da camada de apresentação (Resource), não uma camada separada. Mantém coesão: tudo que é necessário para um endpoint fica junto (Resource + seus DTOs).

---

## 2. ENUMS: VALORES EM PORTUGUÊS

### Decisão: AccountType, TransactionType e UserRole com valores em PORTUGUÊS

**Template (meeting-room):**

```java

public enum UserRole {
    USER,
    ADMIN
}
```


**Seu Projeto (ada-bank-api):**

```java

public enum UserRole {
    MANAGER("GERENTE"),
    CUSTOMER("CLIENTE")
}

public enum AccountType {
    CORRENTE("CORRENTE"),
    POUPANCA("POUPANCA"),
    ELETRONICA("ELETRONICA")
}

public enum TransactionType {
    DEPOSITO("DEPOSITO"),
    SAQUE("SAQUE"),
    TRANSFERENCIA("TRANSFERENCIA")
}

```

**Justificativa:** 
- **Domínio bancário brasileiro:** Os tipos de conta e transação são conceitos do negócio brasileiro (Conta Corrente, Poupança, Eletrônica, Depósito, Saque, Transferência)
- **Padronização com banco de dados:** O arquivo SQL usa valores em português
- **Consistência com requisitos:** O documento `projeto_final.md` especifica `CORRENTE | POUPANCA | ELETRONICA` e `DEPOSITO | SAQUE | TRANSFERENCIA`
- **Métodos `getValue()` e `getDescription()`:** Adicionados para facilitar serialização JSON e exibição amigável ao usuário

---

## 3. ENUMS: MÉTODOS AUXILIARES

### Decisão: Adicionar `getValue()` e `getDescription()` em todos os Enums

**Template (meeting-room):**

```java

public enum UserRole {
    USER,
    ADMIN
}
// Sem métodos auxiliares

```

**Seu Projeto (ada-bank-api):**

```java

public enum AccountType {
    CORRENTE("CORRENTE"),
    POUPANCA("POUPANCA"),
    ELETRONICA("ELETRONICA");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDescription() {
        return switch (this) {
            case CORRENTE -> "Conta Corrente";
            case POUPANCA -> "Conta Poupança";
            case ELETRONICA -> "Conta Eletronica";
        };
    }
}

```

**Justificativa:**
- **Serialização JSON controlada:** `@JsonValue` garante que o enum seja serializado como String, não como nome da constante
- **Exibição amigável:** `getDescription()` retorna texto em português para logs, relatórios e interfaces
- **Padronização:** Todos os três enums seguem o mesmo padrão

---

## 4. ENTIDADES: CAMPOS ADICIONAIS

### Decisão: Adicionar `@Version` em `Customer` para Optimistic Locking

**Template (meeting-room):**

```java

@Entity
public class User extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    // Sem versioning
}

```

**Seu Projeto (ada-bank-api):**

```java

@Entity
public class Customer extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;
    // ...
}

```

**Justificativa:**
- **Concorrência:** Múltiplos gerentes podem atualizar dados de um cliente simultaneamente
- **Integridade de dados:** Optimistic Locking previne conflitos de atualização (race conditions)
- **Padrão bancário:** Sistemas financeiros exigem controle de concorrência rigoroso
- **Sem versioning em Transaction:** Transações são imutáveis (eventos históricos), não precisam de versioning

---

## 5. ENTIDADES: SALDO CALCULADO DINAMICAMENTE

### Decisão: Usar `@Formula` em `Account.balance` em vez de persistir o saldo

**Template (meeting-room):**

```java

@Entity
public class Room extends PanacheEntityBase {
    @Formula("(SELECT COALESCE(v.horas_utilizadas, 0) FROM vw_room_occupancy v WHERE v.room_id = id)")
    private double hoursUsed;
}

```

**Seu Projeto (ada-bank-api):**

``` java

@Entity
public class Account extends PanacheEntityBase {
    @Formula("(SELECT COALESCE(SUM(" +
            "CASE " +
            "WHEN t.type = 'DEPOSITO' AND t.destination_account_id = id THEN t.amount " +
            "WHEN t.type = 'SAQUE' AND t.source_account_id = id THEN -t.amount " +
            "WHEN t.type = 'TRANSFERENCIA' AND t.destination_account_id = id THEN t.amount " +
            "WHEN t.type = 'TRANSFERENCIA' AND t.source_account_id = id THEN -t.amount " +
            "ELSE 0 END" +
            "), 0) FROM transaction t WHERE t.source_account_id = id OR t.destination_account_id = id)")
    private BigDecimal balance;
}

```

**Justificativa:**
- **Consistência garantida:** O saldo é sempre derivado das transações, nunca fica desatualizado
- **Sem sincronização manual:** Não precisa atualizar saldo em múltiplos lugares
- **Auditoria:** Todas as mudanças de saldo são rastreáveis via tabela `transaction`
- **Padrão bancário:** Bancos reais calculam saldo a partir do extrato, não armazenam saldo separado

---

## 6. ENTIDADES: RELACIONAMENTOS

### Decisão: Usar `Long customerId` em vez de `@ManyToOne` em `Account`

**Template (meeting-room):**

```java

@Entity
public class Reservation extends PanacheEntityBase {
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}

```

**Seu Projeto (ada-bank-api):**

```java

@Entity
public class Account extends PanacheEntityBase {
    @NotNull(message = "O cliente da conta é obrigatório")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    // Sem @ManyToOne
}

```

**Justificativa:**
- **Performance:** Evita lazy loading e N+1 queries
- **Simplicidade:** DTOs já trazem dados do cliente quando necessário
- **Flexibilidade:** Permite queries customizadas sem carregar relacionamentos desnecessários
- **Padrão REST:** Relacionamentos são resolvidos via links HATEOAS, não via eager loading

---

## 7. VALIDAÇÕES: BEAN VALIDATION

### Decisão: Validações em DTOs (Request) e Entidades (Entity)

**Seu Projeto (ada-bank-api):**

```java

// Em Customer (Entity)
@NotBlank(message = "O nome do cliente é obrigatório")
@Size(min = 3, max = 100, message = "O nome do cliente deve conter entre 3 e 100 caracteres")
@Column(name = "name", nullable = false, length = 100)
private String name;

@NotBlank(message = "O CPF é obrigatório")
@Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos numéricos")
@Column(name = "cpf", nullable = false, unique = true, length = 11)
private String cpf;

@Email(message = "O email informado é inválido")
@Column(name = "email", nullable = false, unique = true, length = 255)
private String email;

```

**Justificativa:**
- **Validação em duas camadas:** DTOs validam entrada, Entidades garantem integridade no banco
- **Mensagens em português:** Erros são compreensíveis para usuários finais
- **CPF com 11 dígitos:** Padrão brasileiro (sem formatação)
- **Email único:** Previne duplicação de usuários
- **Senha com mínimo 6 caracteres:** Segurança básica

---

## 8. SEGURANÇA: ROLES E AUTORIZAÇÃO

### Decisão: Dois papéis (MANAGER e CUSTOMER) em vez de (USER e ADMIN)

**Template (meeting-room):**

```java

public enum UserRole {
    USER,
    ADMIN
}
```

**Seu Projeto (ada-bank-api):**

```java

public enum UserRole {
    MANAGER("GERENTE"),    // Gerente do banco
    CUSTOMER("CLIENTE")    // Cliente do banco
}

```

**Justificativa:**
- **Domínio bancário:** Papéis refletem a realidade (gerente vs cliente)
- **Permissões específicas:**
  - `MANAGER`: Pode criar clientes, criar contas, ver qualquer transação
  - `CUSTOMER`: Acessa apenas sua própria conta e transações
- **Segurança:** Cliente não pode ver dados de outro cliente

---

## 9. RECORD: LoggedUser

### Decisão: Um único método `isManager()` em vez de dois métodos

**Template (meeting-room):**

```java

public record LoggedUser(Long id, String username, String name, String role) {
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
```

**Seu Projeto (ada-bank-api):**

```java

public record LoggedUser(Long id, String username, String name, String role) {
    public boolean isManager() {
        return "GERENTE".equals(role);
    }

    public boolean isCustomer() {
        return "CLIENTE".equals(role);
    }
}

```

**Justificativa:**
- **Praticidade:** Dois métodos facilitam verificações nos endpoints
- **Simetria:** Ambos os papéis têm método de verificação
- **Legibilidade:** `isManager()` e `isCustomer()` são mais claros que `!isManager()`
- **Sem impacto:** Ainda é uma classe simples (Record)

---

## 10. RECORD: PageResult

### Decisão: Manter idêntico ao template

**Template (meeting-room):**

```java

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
    public int totalPages() {
        return size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }
}
```
	
**Seu Projeto (ada-bank-api):**

```java

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
    public int totalPages() {
        return size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }
}
```

**Justificativa:**
- **Padrão universal:** Paginação é igual em qualquer domínio
- **Sem customização necessária:** Funciona perfeitamente para clientes, contas e transações
- **Reutilizável:** Genérico `<T>` permite usar com qualquer tipo

---

## 11. DOCUMENTAÇÃO: JAVADOC

### Decisão: Adicionar JavaDoc completo em português

**Seu Projeto (ada-bank-api):**

``` java

/**
 * Representa uma conta bancária dentro do sistema da ADA.
 * <p>
 * Esta classe vincula um número de conta e um tipo específico a um cliente,
 * garantindo que as regras de integridade para identificação bancária sejam respeitadas.
 * </p>
 *
 * @author Marcelo
 * @version 1.0
 */
public class Account extends PanacheEntityBase {
    // ...
}

```

**Justificativa:**
- **Não é obrigatório:** Mas melhora significativamente a qualidade do projeto
- **Facilita manutenção:** Futuro desenvolvedor entende rapidamente cada classe
- **Profissionalismo:** Demonstra cuidado com a documentação
- **Apresentação:** Facilita explicação das decisões de projeto

---

## 12. IDIOMA: CÓDIGO EM INGLÊS, DOCUMENTAÇÃO EM PORTUGUÊS

### Decisão: Separação clara entre código e apresentação

**Código (Inglês):**

```java

public class Customer { }
public class Account { }
public enum AccountType { CORRENTE, POUPANCA, ELETRONICA }
public Long customerId;
public String accountNumber;

```


**Documentação e Mensagens (Português):**

```java

@NotBlank(message = "O nome do cliente é obrigatório")
@Size(min = 3, max = 100, message = "O nome do cliente deve conter entre 3 e 100 caracteres")

/**
 * Representa um cliente dentro do sistema bancário da ADA.
 */

public String getDescription() {
    return switch (this) {
        case CORRENTE -> "Conta Corrente";
        case POUPANCA -> "Conta Poupança";
    };
}

```


**Justificativa:**
- **Padrão internacional:** Código em inglês é padrão em desenvolvimento profissional
- **Usuário final:** Mensagens em português são compreensíveis para usuários brasileiros
- **Consistência:** Todas as classes, métodos e variáveis em inglês
- **Clareza:** Não há ambiguidade entre código e apresentação

---

	
## 13. TRANSAÇÕES: IMUTABILIDADE

### Decisão: Sem `@Version` em `Transaction`

**Seu Projeto (ada-bank-api):**

``` java

@Entity
public class Transaction extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sem @Version
    // Transações são imutáveis
}

```

**Justificativa:**
- **Evento histórico:** Transações representam eventos que ocorreram, não podem ser alteradas
- **Auditoria:** Cada transação é um registro permanente
- **Integridade:** Modificar uma transação quebraria a integridade do saldo
- **Padrão bancário:** Transações são imutáveis por lei em muitos países

---

## 14. TIPOS DE CONTA: RESTRIÇÕES

### Decisão: Conta ELETRONICA só permite transferências

**Requisito (projeto_final.md):**
Conta ELETRONICA: só permite transferências. Operações de saque e depósito devem ser rejeitadas com 400.

**Seu Projeto (ada-bank-api):**

```java

public enum AccountType {
    CORRENTE("CORRENTE"),      // Permite: depósito, saque, transferência
    POUPANCA("POUPANCA"),      // Permite: depósito, saque, transferência
    ELETRONICA("ELETRONICA")   // Permite: apenas transferência
}
```



**Justificativa:**
- **Requisito explícito:** Especificado no documento de projeto
- **Regra de negócio:** Contas eletrônicas são para transações digitais apenas
- **Validação na camada de serviço:** `AccountService` valida tipo de conta antes de processar operação

---

## 15. AUTENTICAÇÃO: JWT

### Decisão: Usar JWT (JSON Web Token) para autenticação

**Seu Projeto (ada-bank-api):**
POST /auth/login Request: { username, password } Response: { token: "eyJhbGciOiJSUzI1NiJ9…" }

**Justificativa:**
- **Padrão REST:** JWT é stateless, ideal para APIs
- **Escalabilidade:** Não requer sessão no servidor
- **Segurança:** Token assinado com chave privada
- **Autorização:** Token contém `LoggedUser` com papéis do usuário

---

## RESUMO EXECUTIVO

| Decisão | Template | Seu Projeto | Justificativa |
|---|---|---|---|
| DTOs em pacote | Separado (`dto/`) | Junto (`resource/`) | Coesão: Resource + DTOs juntos |
| Valores de Enum | Inglês | Português | Domínio bancário brasileiro |
| Métodos em Enum | Nenhum | `getValue()`, `getDescription()` | Serialização e exibição |
| Versioning em Customer | Não | Sim (`@Version`) | Controle de concorrência |
| Saldo em Account | Persistido | Calculado (`@Formula`) | Consistência garantida |
| Relacionamentos | `@ManyToOne` | `Long customerId` | Performance e simplicidade |
| Papéis | USER, ADMIN | MANAGER, CUSTOMER | Domínio bancário |
| LoggedUser métodos | Um (`isAdmin()`) | Dois (`isManager()`, `isCustomer()`) | Praticidade |
| Documentação | Não exigida | JavaDoc completo | Qualidade e profissionalismo |
| Idioma | Inglês | Inglês código + Português docs | Padrão internacional + usuário final |
| Transações versioning | N/A | Sem `@Version` | Imutabilidade de eventos |

---

## CONCLUSÃO

Todas as decisões foram tomadas com base em:

1. **Padrão do Professor:** Espelhando a estrutura do projeto `meeting-room`
2. **Domínio Bancário:** Adaptações específicas para o contexto brasileiro
3. **Boas Práticas:** Padrões internacionais de desenvolvimento
4. **Requisitos do Projeto:** Conformidade com `projeto_final.md`
5. **Qualidade:** Código profissional, legível e mantível

Cada mudança é **justificada e consciente**, não arbitrária.

---

**Documento gerado em:** Abril 2026  
**Versão:** 1.0  
**Status:** Aprovado para apresentação
