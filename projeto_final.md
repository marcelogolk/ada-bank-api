# Projeto Final — API Bancária com Quarkus

---

## Contexto

Você foi contratado para desenvolver o backend de um sistema bancário simplificado. O sistema deve expor uma API REST que permita o gerenciamento de **clientes**, **contas bancárias** e **transações**.

---

## Aviso Importante

> **Projeto que não compila = nota ZERO, sem exceção.**
> Antes de entregar, execute `./mvnw quarkus:dev` e confirme que a aplicação sobe sem erros.

---

## Requisitos Técnicos Obrigatórios

| Tecnologia | Versão mínima |
|---|---|
| Java | 21+ |
| Quarkus | 3.x |
| Build | Maven |
| Banco de dados | PostgreSQL |

---

## Entidades do Domínio

### `Cliente`
```
id, nome, cpf, email, senha
```

### `Conta`
```
id, numero, tipo (CORRENTE | POUPANCA | ELETRONICA), cliente_id
```

> **Conta ELETRONICA:** só permite transferências. Operações de saque e depósito devem ser rejeitadas com `400`.

### `Transacao`
```
id, tipo (DEPOSITO | SAQUE | TRANSFERENCIA), valor, dataHora, conta_origem_id, conta_destino_id
```

---

## Endpoints Obrigatórios

---

### Autenticação

#### `POST /auth/login`
**Request:**
```json
{
    "email": "alice@banco.com",
    "senha": "senha123"
}
```
**Response `200`:**
```json
{
    "token": "eyJhbGciOiJSUzI1NiJ9..."
}
```

---

### Clientes — Role: `GERENTE`

#### `POST /clientes`
**Request:**
```json
{
    "nome": "Alice Souza",
    "cpf": "123.456.789-00",
    "email": "alice@banco.com",
    "senha": "senha123"
}
```
**Response `201`:**
```json
{
    "id": 1,
    "nome": "Alice Souza",
    "email": "alice@banco.com"
}
```

#### `GET /clientes`
**Response `200`:**
```json
[
    {
        "id": 1,
        "nome": "Alice Souza",
        "email": "alice@banco.com"
    },
    {
        "id": 2,
        "nome": "Bob Lima",
        "email": "bob@banco.com"
    }
]
```

#### `GET /clientes/{id}`
**Response `200`:**
```json
{
    "id": 1,
    "nome": "Alice Souza",
    "email": "alice@banco.com"
}
```

#### `PUT /clientes/{id}`
**Request:**
```json
{
    "nome": "Alice Souza Silva",
    "email": "alice.silva@banco.com",
    "senha": "novaSenha456"
}
```
> **CPF não pode ser atualizado.** Tentativa de atualizar CPF deve ser respondida com status `400`.

**Response `200`:**
```json
{
    "id": 1,
    "nome": "Alice Souza Silva",
    "email": "alice.silva@banco.com"
}
```

---

### Contas — Role: `GERENTE` ou `CLIENTE`

#### `POST /contas` — Role: `GERENTE`
**Request:**
```json
{
    "tipo": "CORRENTE",
    "cliente": { "id": 1 }
}
```
**Response `201`:**
```json
{
    "id": 10,
    "numero": "0001-0",
    "tipo": "CORRENTE",
    "saldo": 0.00,
    "titular": {
        "id": 1,
        "nome": "Alice Souza Silva",
        "email": "alice.silva@banco.com"
    }
}
```

#### `GET /contas/{id}`
**Response `200`:**
```json
{
    "id": 10,
    "numero": "0001-0",
    "tipo": "CORRENTE",
    "saldo": 1500.00,
    "titular": {
        "id": 1,
        "nome": "Alice Souza Silva",
        "email": "alice.silva@banco.com"
    },
    "transacoes": [
        {
            "id": 5,
            "tipo": "DEPOSITO",
            "valor": 1500.00,
            "dataHora": "2026-04-07T10:30:00"
        }
    ],
    "_links": {
        "transacoes": "/transacoes?contaId=10"
    }
}
```

> O campo `transacoes` retorna apenas as transações do dia atual. Para o histórico completo, utilize o link indicado em `_links.transacoes`.

#### `POST /contas/{id}/deposito`
> Não disponível para contas do tipo `ELETRONICA`.

**Request:**
```json
{
    "valor": 500.00
}
```
**Response `200`:**
```json
{
    "id": 6,
    "tipo": "DEPOSITO",
    "valor": 500.00,
    "saldoAtual": 2000.00,
    "dataHora": "2026-04-07T11:00:00",
    "conta": {
        "id": 10,
        "numero": "0001-0",
        "titular": {
            "id": 1,
            "nome": "Alice Souza"
        }
    }
}
```
**Response `422` (conta eletrônica):**
```json
{
    "erro": "Conta do tipo ELETRONICA não permite depósitos."
}
```

#### `POST /contas/{id}/saque`
> Não disponível para contas do tipo `ELETRONICA`.

**Request:**
```json
{
    "valor": 200.00
}
```
**Response `200`:**
```json
{
    "id": 7,
    "tipo": "SAQUE",
    "valor": 200.00,
    "saldoAtual": 1800.00,
    "dataHora": "2026-04-07T11:05:00",
    "conta": {
        "id": 10,
        "numero": "0001-0",
        "titular": {
            "id": 1,
            "nome": "Alice Souza"
        }
    }
}
```
**Response `422` (saldo insuficiente):**
```json
{
    "erro": "Saldo insuficiente para realizar o saque."
}
```
**Response `422` (conta eletrônica):**
```json
{
    "erro": "Conta do tipo ELETRONICA não permite saques."
}
```

#### `POST /contas/{id}/transferencia`
**Request:**
```json
{
    "contaDestino": { "id": 11 },
    "valor": 300.00
}
```
**Response `200`:**
```json
{
    "id": 8,
    "tipo": "TRANSFERENCIA",
    "valor": 300.00,
    "saldoAtual": 1500.00,
    "dataHora": "2026-04-07T11:10:00",
    "conta": {
        "id": 10,
        "numero": "0001-0",
        "tipo": "CORRENTE",
        "titular": {
            "id": 1,
            "nome": "Alice Souza"
        }
    },
    "contaDestino": {
        "id": 11,
        "numero": "0002-1",
        "tipo": "ELETRONICA",
        "titular": {
            "id": 2,
            "nome": "Bob Lima"
        }
    }
}
```

---

### Transações — Role: `GERENTE` ou `CLIENTE`

#### `GET /transacoes/{id}`
**Response `200`:**
```json
{
    "id": 8,
    "tipo": "TRANSFERENCIA",
    "valor": 300.00,
    "dataHora": "2026-04-07T11:10:00",
    "conta": {
        "id": 10,
        "numero": "0001-0",
        "tipo": "CORRENTE",
        "titular": {
            "id": 1,
            "nome": "Alice Souza",
            "email": "alice@banco.com"
        }
    },
    "contaDestino": {
        "id": 11,
        "numero": "0002-1",
        "tipo": "ELETRONICA",
        "titular": {
            "id": 2,
            "nome": "Bob Lima",
            "email": "bob@banco.com"
        }
    }
}
```

#### `GET /transacoes?contaId={contaId}`
> Retorna o histórico completo de transações da conta. Referenciado via HATEOAS em `GET /contas/{id}`.

**Response `200`:**
```json
[
    {
        "id": 5,
        "tipo": "DEPOSITO",
        "valor": 1500.00,
        "dataHora": "2026-03-10T09:00:00",
        "conta": {
            "id": 10,
            "numero": "0001-0",
            "titular": {
                "id": 1,
                "nome": "Alice Souza",
                "email": "alice@banco.com"
            }
        }
    },
    {
        "id": 8,
        "tipo": "TRANSFERENCIA",
        "valor": 300.00,
        "dataHora": "2026-04-07T11:10:00",
        "conta": {
            "id": 10,
            "numero": "0001-0",
            "titular": {
                "id": 1,
                "nome": "Alice Souza",
                "email": "alice@banco.com"
            }
        },
        "contaDestino": {
            "id": 11,
            "numero": "0002-1",
            "titular": {
                "id": 2,
                "nome": "Bob Lima",
                "email": "bob@banco.com"
            }
        }
    }
]
```

---

## Critérios de Avaliação

### 1. Projeto compila e sobe (pré-requisito - Zera o projeto caso não funcione)
- `./mvnw quarkus:dev` sem erros
- Endpoints respondem

---

### 2. Verbos e Semântica HTTP `(10 pts)`
- Uso correto de `GET`, `POST`, `PUT`, `DELETE`
- Status codes corretos: `200`, `201`, `204`, `400`, `401`, `403`, `404`
- Exemplo: saque com saldo insuficiente deve retornar `400`, não `500`

---

### 3. Hibernate + Panache `(20 pts)`
- Entidades mapeadas com anotações JPA (`@Entity`, `@Id`, `@ManyToOne`, etc.)
- Uso de `PanacheEntity` ou `PanacheRepository`
- Relacionamentos corretos entre `Cliente`, `Conta` e `Transacao`
- Consultas customizadas (ex: buscar extrato paginado por conta)

---

### 4. Bean Validation `(20 pts)`
- `@NotBlank`, `@NotNull`, `@Email`, `@Size`, `@Min` aplicados nas entidades ou DTOs
- Validação ativa nos endpoints (`@Valid`)
- Retorno de mensagem de erro descritiva quando validação falha (`400`)

---

### 5. Segurança e Controle de Acesso `(20 pts)`
- Endpoint `/auth/login` funcional gerando JWT
- Roles definidas: `GERENTE` e `CLIENTE`
- Endpoints protegidos com `@RolesAllowed`
- Acesso negado retorna `401` (não autenticado) ou `403` (sem permissão)
- Cliente não pode ver nem transacionar em conta de outro cliente

---

### 6. Qualidade Geral `(20 pts)`
- Código organizado em camadas: `resource`, `service`, `repository` (ou `entity`)
- Sem lógica de negócio dentro do Resource/Controller
- Regra de saldo negativo tratada na camada de serviço
- `application.properties` configurado corretamente

---

### 7. Qualidade de Código `(10 pts)`
- Nomes de variáveis, métodos e classes claros e em inglês ou português consistente
- Sem código comentado ou métodos vazios sem justificativa
- Métodos curtos e com responsabilidade única
- Sem duplicação de lógica (ex: validação de saldo repetida em vários lugares)

---

## Resumo da Pontuação

| Critério | Pontos |
|---|---|
| Verbos e semântica HTTP | 10 |
| Hibernate + Panache | 20 |
| Bean Validation | 20 |
| Segurança e autorização | 20 |
| Qualidade geral | 20 |
| Qualidade de Código | 10 |
| **Total** | **100** |

---

## Entrega

- Repositório Git (GitHub, GitLab ou zip)
- `README.md` com instruções de como rodar o projeto
