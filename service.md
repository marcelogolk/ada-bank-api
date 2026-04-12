# Service - Camada de Regras de Negócio

## Visão Geral

A camada `service` é responsável por:

- centralizar regras de negócio
- controlar fluxo de operações
- validar regras complexas
- manipular dados em memória

Ela atua entre:

- Resource (entrada HTTP)
- Model (estrutura de dados)

---

## Decisões de Projeto

### 1. Armazenamento em Memória

Cada service utiliza:

- `Map<Long, Entity>`
- `AtomicLong` para geração de IDs

**Justificativa:**
- Simplicidade
- Independência de banco de dados

---

### 2. Separação por Domínio

Foram definidas quatro services:

```
service/
├── CustomerService
├── AccountService
├── TransactionService
└── AuthService
```

---

## Services

### CustomerService

Responsável pela gestão de clientes.

#### Responsabilidades:
- cadastrar cliente
- listar clientes
- buscar por ID
- atualizar cliente
- validar CPF único
- validar email único
- buscar cliente por email

#### Regras:
- CPF não pode duplicar
- Email não pode duplicar
- CPF não deve ser alterado após criação

---

### AccountService

Responsável pela gestão de contas e operações financeiras.

#### Responsabilidades:
- criar conta
- buscar conta
- validar cliente existente
- controlar saldo
- executar operações financeiras

#### Operações:
- depósito
- saque
- transferência

#### Regras:

- Conta inicia com saldo zero
- Conta ELETRONICA:
  - não permite saque
  - não permite depósito
- Saque:
  - exige saldo suficiente
- Transferência:
  - origem e destino válidos
  - saldo suficiente
  - contas diferentes

---

### TransactionService

Responsável pelo registro e consulta de transações.

#### Responsabilidades:
- registrar transações
- buscar por ID
- listar por conta
- listar transações do dia
- ordenar por data/hora

#### Papel no sistema:
- manter histórico financeiro
- não controla saldo diretamente

---

### AuthService

Responsável pela autenticação.

#### Responsabilidades:
- validar email e senha
- buscar cliente por email
- autenticar usuário

#### Regras:
- email deve existir
- senha deve corresponder

---

## Fluxo de Operações

### Cadastro de Cliente
Resource → CustomerService

### Criação de Conta
Resource → AccountService → CustomerService

### Depósito
Resource → AccountService → TransactionService

### Saque
Resource → AccountService → TransactionService

### Transferência
Resource → AccountService → TransactionService

### Consulta de Transações
Resource → TransactionService

### Login
Resource → AuthService → CustomerService

---

## Organização Interna das Services

Cada service deve conter:

### Métodos públicos
- operações principais do sistema

### Métodos privados
- validações
- busca obrigatória
- lógica auxiliar

---

## Regras de Projeto Importantes

- Regras de negócio ficam no service, não no model
- Services podem depender entre si
- Resource não deve conter lógica
- Model não deve conter regras complexas

---

## Evolução Futura

Esta estrutura permite evoluir facilmente para:

- persistência com banco de dados
- uso de JPA / Hibernate
- autenticação com JWT

---

## Conclusão

A camada `service` foi projetada para:

- centralizar regras de negócio
- manter código organizado
- facilitar testes
- preparar o sistema para crescimento

Ela é o núcleo lógico da aplicação.
