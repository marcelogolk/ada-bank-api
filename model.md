# Model - Camada de Domínio

## Visão Geral

A camada `model` representa as entidades principais do sistema bancário.  
Ela define a estrutura dos dados e estabelece as bases para as regras de negócio.

As entidades modeladas são:

- Customer (Cliente)
- Account (Conta)
- Transaction (Transação)

Além disso, foram definidos enums para padronização:

- AccountType
- TransactionType

---

## Decisões de Projeto

### 1. Linguagem do Código

- Código (classes, atributos, métodos): **Inglês**
- Mensagens e documentação: **Português**

**Justificativa:**
- Inglês é padrão de mercado para código
- Português facilita entendimento do domínio no contexto acadêmico

---

### 2. Estrutura das Entidades

As classes seguem o padrão:

- atributos privados
- construtor vazio
- construtor completo
- getters e setters
- `equals`, `hashCode` e `toString`

**Justificativa:**
- Simplicidade e clareza
- Compatível com frameworks (Jackson)
- Facilita evolução futura (JPA)

---

### 3. Validação com Bean Validation

Foram utilizadas anotações como:

- `@NotBlank`
- `@NotNull`
- `@Size`
- `@Pattern`
- `@Email`
- `@DecimalMin`

**Objetivo:**
Garantir integridade dos dados já na entrada da aplicação.

---

### 4. Padronização de Mensagens

As mensagens seguem o padrão:

- "O campo é obrigatório"
- "O campo deve conter..."
- "O campo informado é inválido"

---

## Entidades

### Customer

Representa o cliente do sistema.

#### Campos:
- id
- name
- cpf
- email
- password

#### Decisões importantes:
- CPF é obrigatório e único
- Email é obrigatório e único
- Password possui validação mínima de tamanho

#### Segurança:
- Campo `password` usa `@JsonIgnore`
- Não é exposto em respostas da API

---

### Account

Representa uma conta bancária.

#### Campos:
- id
- accountNumber
- type (AccountType)
- customerId
- balance

#### Decisões importantes:

##### Saldo (balance)
- Tipo: `BigDecimal`
- Inicializado com `BigDecimal.ZERO`

**Justificativa:**
- Precisão financeira
- Evita erros de ponto flutuante
- Conta sempre inicia com saldo zero

##### Validação:
- `@NotNull` no saldo para evitar estado inválido

---

### Transaction

Representa uma movimentação financeira.

#### Campos:
- id
- type (TransactionType)
- amount
- dateTime
- sourceAccountId
- destinationAccountId

#### Decisões importantes:
- `BigDecimal` para valores monetários
- `LocalDateTime` para registro temporal
- `destinationAccountId` é opcional dependendo da operação

---

## Enums

### AccountType

Valores:
- CORRENTE
- POUPANCA
- ELETRONICA

**Decisão:**
- Sem acentos
- Alinhado com o contrato da API

---

### TransactionType

Valores:
- DEPOSITO
- SAQUE
- TRANSFERENCIA

**Decisão:**
- Sem acentos
- Uso de `@JsonValue` para controle do JSON

---

## Conclusão

A camada `model` foi construída com foco em:

- clareza
- aderência ao domínio bancário
- validação de dados
- preparação para evolução futura

Ela serve como base sólida para a implementação da camada de serviço.
