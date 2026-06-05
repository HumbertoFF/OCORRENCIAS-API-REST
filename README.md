# Ocorrências API

API REST desenvolvida em Java 17 + Spring Boot  

## Desenvolvedor Humberto Freitas da Silva Filho

## Para que serve?
## Este sistema foi desenvolvido para atender setores que dependem de documentação e rastreamento de ocorrências, como:

### 1. Órgãos de Segurança Pública

Registro de ocorrências policiais e boletins de ocorrência (BO)
Documentação de cenas de crime com fotos e localização
Rastreabilidade completa: quem registrou, quando, onde e status

### 2. Empresas de Seguros

Abertura de sinistros com foto de dano/evidência
Vinculação automática dos dados do cliente ao registro
Acompanhamento do processo até finalização

### 3. Órgãos Ambientais e Fiscalização

Registro de denúncias ambientais ou infrações
Localização geográfica (CEP/cidade) para análise
Documentação visual para investigação posterior

### 4. Serviços de Atendimento ao Cliente

Gerenciamento de reclamações e solicitações
Rastreamento do status (aberta → resolvida)
Base de dados de clientes com histórico completo

### 5. Órgãos Municipais e Prefeituras

Denúncias de problemas urbanos (buraco na rua, iluminação, etc.)
Mapeamento geográfico das ocorrências pela cidade
Priorização de resoluções



## Fluxo de Negócio
## Caso de Uso Principal

### 1. AUTENTICAÇÃO
   ↓
   Usuário faz login com email e senha
   → Recebe token JWT válido por 30 minutos
   → Token necessário para todos os próximos passos

### 2. REGISTRO DE OCORRÊNCIA
   ↓
   Usuário fornece:
   • Dados do cliente (nome, CPF, data de nascimento)
   • Endereço onde ocorreu o fato (rua, bairro, CEP, cidade)
   • Fotos/documentos de evidência (opcional no cadastro)

   → Sistema valida CPF e CEP
   → Cria novo cliente ou reutiliza existente
   → Cria novo endereço ou reutiliza existente
   → Gera ID único da ocorrência
   → Armazena fotos em bucket MinIO
   → Retorna links públicos para as fotos

### 3. GESTÃO DE FOTOS (Opcional)
   ↓
   Mesmo após cadastro, é possível:
   • Adicionar mais fotos à ocorrência
   • Gerar evidência visual cronológica
   → Bloqueado se ocorrência for finalizada

### 4. FINALIZAÇÃO
   ↓
   Quando caso é resolvido:
   • Marca ocorrência como FINALIZADA
   • Torna imutável → não permite mais alterações
   → Preserva integridade histórica do registro

### 5. CONSULTA E RELATÓRIOS
   ↓
   Sistema permite:
   • Buscar ocorrências por cliente/CPF
   • Filtrar por data de registro
   • Localizar por cidade
   • Ordenar por data ou localidade
   → Suporta paginação para grandes volumes


## Diagrama do Banco de dados
![](/home/humbertoff/Downloads/ocorrencias_database_diagram.svg)

---

## Tecnologias

- Java 17 + Spring Boot 3.5.13
- PostgreSQL 16
- MinIO 8.5.9
- Flyway Migrations
- Spring Security + JWT
- Docker + Docker Compose

---

## Como executar

### Pré-requisitos

- Docker e Docker Compose instalados

### Subir tudo com um único comando dentro da pasta do projeto

```bash
docker compose --env-file .env.development up -d
```
> obs: .env.development arquivo para guardar as variáveis de ambiente de desenvolvimento. Para ambiente em produção utilizar .env seguindo o mesmo padrão do .env.development

Aguarde os containers ficarem healthy. A aplicação estará disponível em:

| Serviço       | URL                              |
|---------------|----------------------------------|
| API           | http://localhost:8080            |
| Swagger UI    | http://localhost:8080/swagger-ui.html |
| MinIO Console | http://localhost:9001            |
| PostgreSQL    | localhost:5432                   |
| pgAdmin       | http://localhost:5050/           |


**O projeto dispõe de uma ferramenta chamada Swagger para testes e verificação dos Endpoints disponibilizado pela API.**

O acesso é realizado pela url fornecida na tabela anterior, na sessão de testes será fornecida as orientações para uso da ferramenta.

Credenciais MinIO Console: `minioadmin` / `minioadmin`

Para acessar o banco de dados pelo console do pgAdmin no navegador. 
```bash
Deve-se acessar o link http://localhost:5050/
Realizar o login com as credenciais: `admin@admin.com` / `admin`
Criar a conexão com o banco, clicando com o botão direito no Servers > Register > Server.
Na aba General coloque um nome para a conexão e na aba Connection utilize as seguintes credenciais:

-Host name/address: ocorrencias-postgres
-Port: 5432
-Maintenance database: ocorrencias_db
-Username: postgres
-Password: postgres
```

---
## Como realizar os testes
### Autenticação

A API usa JWT com expiração de **30 minutos**. Um usuário administrador é criado automaticamente via migration.

#### Utilizando o Swagger:
Acesse a url da ferramenta http://localhost:8080/swagger-ui.html

### 1. **Autenticação**

Todos os endpoints (exceto /login) exigem o token JWT no header. 
Após o login, clique em Authorize(no início da página) no Swagger e cole: Bearer <seu_token>

```bash
POST	/api/v1/auth/login 
```

Clique em Try it out para autenticar o usuário e obter token JWT

Corpo da Requisição (JSON):
```json
{
  "email": "admin@admin.com.br",
  "senha": "admin123"
}
```
Clique em execute

Resposta de Sucesso:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiracaoMs": 1800000
}
```
>Códigos de Status:
> - 200	Token gerado com sucesso
> - 401	Email ou senha inválidos
> - 400	Campos obrigatórios ausentes ou formato de email inválido 


### CRUD de Clientes e Endereços

### 2. Clientes

```bash
POST	/api/v1/clientes	Criar novo cliente
```

Corpo da Requisição (JSON):
```json
{
  "nmeCliente": "João Silva",
  "dtaNascimento": "1990-05-15",
  "nroCpf": "123.456.789-09"
}
```
Resposta de Sucesso:
```json
{
  "codCliente": 1,
  "nmeCliente": "João Silva",
  "dtaNascimento": "1990-05-15",
  "nroCpf": "123.456.789-09",
  "dtaCriacao": "2026-04-09T10:00:00"
}
```

>Códigos de Status:
> - 201	Cliente criado — header Location aponta para o novo recurso 
> - 400	CPF inválido, campo obrigatório ausente ou CPF duplicado 
> - 401	Token ausente ou expirado

```bash
GET	/api/v1/clientes?page=0&size=10	Listar clientes paginado
```

Ordenação padrão: nmeCliente. Para alterar: ?sort=dtaCriacao,desc

Resposta de Sucesso:
```json
{
  "content": [
    { "codCliente": 1, "nmeCliente": "João Silva", ...}
  ],
  "pageable": { "pageNumber": 0, "pageSize": 10 },
  "totalElements": 1,
  "totalPages": 1
}
```
> Códigos de Status:
> - 200	Lista retornada com sucesso 
> - 401	Token ausente ou expirado

```bash
GET	/api/v1/clientes/{id}	Buscar cliente pelo ID
```

Resposta de Sucesso:
```json
{
  "codCliente": 1,
  "nmeCliente": "João Silva",
  "dtaNascimento": "1990-05-15",
  "nroCpf": "123.456.789-09",
  "dtaCriacao": "2026-04-09T10:00:00"
}
```
>Códigos de Status:
> - 200	Cliente encontrado 
> - 404	Cliente não encontrado para o ID informado 
> - 401	Token ausente ou expirado

```bash
PUT	/api/v1/clientes/{id}	Atualizar cliente existente
```

Corpo da Requisição (JSON):
```json
{
  "nmeCliente": "João Silva Atualizado",
  "dtaNascimento": "1990-05-15",
  "nroCpf": "123.456.789-09"
}
```

Resposta de Sucesso:
```json
{
  "codCliente": 1,
  "nmeCliente": "João Silva Atualizado",
  "dtaNascimento": "1990-05-15",
  "nroCpf": "123.456.789-09",
  "dtaCriacao": "2026-04-09T10:00:00"
}
```

>Códigos de Status:
> - 200	Cliente atualizado com sucesso 
> - 404	Cliente não encontrado 
> - 400	CPF inválido ou campos obrigatórios ausentes  
> - 401	Token ausente ou expirado

```bash
DELETE	/api/v1/clientes/{id}	Remover cliente pelo ID
```

> Códigos de Status: 
> - 204	Cliente removido — sem corpo na resposta 
> - 404	Cliente não encontrado  
> - 401	Token ausente ou expirado

### 3. Endereços

```bash
POST	/api/v1/enderecos	Criar novo endereço
```

CEP deve conter exatamente 8 dígitos numéricos sem hífen. Exemplo: 01310100

Corpo da Requisição (JSON):
```json
{
  "nmeLogradouro": "Rua das Flores, 123",
  "nmeBairro": "Centro",
  "nroCep": "01310100",
  "nmeCidade": "São Paulo",
  "nmeEstado": "SP"
}
```
Resposta de Sucesso:
```json
{
  "codEndereco": 1,
  "nmeLogradouro": "Rua das Flores, 123",
  "nmeBairro": "Centro",
  "nroCep": "01310100",
  "nmeCidade": "São Paulo",
  "nmeEstado": "SP"
}
```

>Códigos de Status:
> - 201	Endereço criado — header Location aponta para o novo recurso 
> - 400	CEP com tamanho diferente de 8 dígitos ou campo obrigatório ausente 
> - 401	Token ausente ou expirado

```bash
GET	/api/v1/enderecos?page=0&size=10	Listar endereços paginado
```

Ordenação padrão: nmeCidade. Para alterar: ?sort=nmeEstado,asc

Resposta de Sucesso:
```json
{
  "content": [
    { "codEndereco": 1, "nmeLogradouro": "Rua das Flores, 123", ... }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 10 },
  "totalElements": 1,
  "totalPages": 1
}
```
>Códigos de Status:
> - 200	Lista retornada com sucesso 
> - 401	Token ausente ou expirado

```bash
GET	/api/v1/enderecos/{id}	Buscar endereço pelo ID
```

Resposta de Sucesso:
```json
{
  "codEndereco": 1,
  "nmeLogradouro": "Rua das Flores, 123",
  "nmeBairro": "Centro",
  "nroCep": "01310100",
  "nmeCidade": "São Paulo",
  "nmeEstado": "SP"
}
```

>Códigos de Status:
> - 200	Endereço encontrado
> - 404	Endereço não encontrado para o ID informado  
> - 401	Token ausente ou expirado

```bash
PUT	/api/v1/enderecos/{id}	Atualizar endereço existente
```

Corpo da Requisição (JSON):
```json
{
  "nmeLogradouro": "Av. Paulista, 1000",
  "nmeBairro": "Bela Vista",
  "nroCep": "01310900",
  "nmeCidade": "São Paulo",
  "nmeEstado": "SP"
}
```
Resposta de Sucesso:
```json
{
  "codEndereco": 1,
  "nmeLogradouro": "Av. Paulista, 1000",
  "nmeBairro": "Bela Vista",
  "nroCep": "01310900",
  "nmeCidade": "São Paulo",
  "nmeEstado": "SP"
}
```

>Códigos de Status:
> - 200	Endereço atualizado com sucesso 
> - 404	Endereço não encontrado 
> - 400	CEP inválido ou campos obrigatórios ausentes 
> - 401	Token ausente ou expirado

```bash
DELETE	/api/v1/enderecos/{id}	Remover endereço pelo ID
```

>Códigos de Status:
> - 204	Endereço removido — sem corpo na resposta 
> - 404	Endereço não encontrado 
> - 401	Token ausente ou expirado


### 4. Ocorrências 

Este é o fluxo principal da API. 
O cadastro de ocorrência usa multipart/form-data — no Swagger, o campo 'dados' recebe o JSON e 'fotos' recebe os arquivos.

**4.1 Cadastrar Ocorrência**

```bash
POST	/api/v1/ocorrencias	Cadastrar ocorrência — multipart/form-data
```

Content-Type: multipart/form-data  |  Campo 'dados' (JSON) + campo 'imagens' (arquivos, opcional)

Parte 'dados' (JSON):
```json
{
  "cliente": {
    "nmeCliente": "Maria Santos",
    "dtaNascimento": "1985-08-22",
    "nroCpf": "987.654.321-00"
  },
  "endereco": {
    "nmeLogradouro": "Rua XV de Novembro, 200",
    "nmeBairro": "Jardim América",
    "nroCep": "80020310",
    "nmeCidade": "Curitiba",
    "nmeEstado": "PR"
  }
}
```
Parte 'fotos': arquivo(s) de imagem — opcional no cadastro
Ou outros tipos de arquivos.

Resposta de Sucesso (201):
```json
{
  "codOcorrencia": 1,
  "dtaOcorrencia": "2026-04-09T10:30:00",
  "staOcorrencia": "ATIVA",
  "cliente": { "codCliente": 2, "nmeCliente": "Maria Santos", ... },
  "endereco": { "codEndereco": 2, "nmeCidade": "Curitiba", ... },
  "fotos": [
      {
        "codFotoOcorrencia": 1,
        "urlAcesso": "http://localhost:9000/ocorrencias/ocorrencias/1/...",
        "dtaCriacao": "2026-04-09T10:30:00"
      }
    ]
}
```
  
>Códigos de Status:
> - 201	Ocorrência cadastrada com sucesso 
> - 400	Dados inválidos (CPF, CEP, campos obrigatórios ausentes)
> - 401	Token ausente ou expirado


**4.2 Listar Ocorrências com Filtros**
```bash
GET	/api/v1/ocorrencias	Listar com filtros, paginação e ordenação
```

>Parâmetros de Query disponíveis:
> Parâmetro	Tipo	Exemplo 
> - nmeCliente	String (parcial)	?nmeCliente=Maria 
> - nroCpf	String (CPF)	?nroCpf=987.654.321-00 
> - dtaOcorrencia	Date (yyyy-MM-dd)	?dtaOcorrencia=2026-04-09 
> - nmeCidade	String (parcial)	?nmeCidade=Curitiba 
> - sort	Campo,direção	?sort=dtaOcorrencia,desc

Campos ordenáveis: dtaOcorrencia e endereco.nmeCidade — direções: asc ou desc

>Exemplos de URL completos:
> - GET /api/v1/ocorrencias?nmeCliente=Maria&page=0&size=10 
> - GET /api/v1/ocorrencias?nmeCidade=Curitiba&sort=dtaOcorrencia,desc&page=0&size=5 
> - GET /api/v1/ocorrencias?dtaOcorrencia=2026-04-09&sort=endereco.nmeCidade,asc 

>Códigos de Status:
> - 200	Lista retornada com paginação e links de imagem nas fotos 
> - 401	Token ausente ou expirado

```bash
GET	/api/v1/ocorrencias/{id}	Buscar ocorrência por ID
```

Resposta de Sucesso:
```json
{
  "codOcorrencia": 1,
  "dtaOcorrencia": "2026-04-09T10:30:00",
  "staOcorrencia": "ATIVA",
  "cliente": { "codCliente": 2, "nmeCliente": "Maria Santos", ... },
  "endereco": { "codEndereco": 2, "nmeCidade": "Curitiba", ... },
  "fotos": [{ "urlAcesso": "http://localhost:9000/...", ... }]
}
```
>Códigos de Status:
> - 200	Ocorrência encontrada com dados do cliente, endereço e links das fotos 
> - 404	Ocorrência não encontrada 
> - 401	Token ausente ou expirado

**4.3 Finalizar Ocorrência**

```bash
PATCH	/api/v1/ocorrencias/{id}/finalizar	Finalizar ocorrência — sem corpo na requisição
```

REGRA DE NEGÓCIO: Uma vez FINALIZADA, a ocorrência não pode ser alterada. Qualquer tentativa de edição ou novo upload retorna erro.

Fluxo de teste recomendado:
1. Faça PATCH /finalizar em uma ocorrência ATIVA → deve retornar 200 com staOcorrencia: 'FINALIZADA'
2. Tente PATCH /finalizar novamente na mesma ocorrência → deve retornar erro (já finalizada)
3. Tente POST /fotos na ocorrência finalizada → deve retornar erro

>Códigos de Status:
> - 200	Ocorrência finalizada — staOcorrencia agora é 'FINALIZADA' 
> - 409 / 422	Ocorrência já está finalizada 
> - 404	Ocorrência não encontrada 
> - 401	Token ausente ou expirado


**4.4 Adicionar Fotos a uma Ocorrência**

```bash
POST	/api/v1/ocorrencias/{id}/fotos	Upload de imagens — multipart/form-data
```

Campo 'fotos': um ou mais arquivos de imagem (PNG, JPG, etc.). Enviados ao MinIO e link gerado automaticamente.

Resposta de Sucesso (200):
```json
[
  {
    "codFotoOcorrencia": 2,
    "urlAcesso": "http://localhost:9000/ocorrencias/ocorrencias/1/foto2.png?...",
    "dtaCriacao": "2026-04-09T11:00:00"
  },
  {
    "codFotoOcorrencia": 3,
    "urlAcesso": "http://localhost:9000/ocorrencias/ocorrencias/1/foto3.jpg?...",
    "dtaCriacao": "2026-04-09T11:00:00"
  }
]
```

> Códigos de Status:
> - 200	Fotos enviadas ao MinIO e URLs de acesso retornadas 
> - 409 / 422	Ocorrência já finalizada — upload bloqueado 
> - 404	Ocorrência não encontrada 
> - 401	Token ausente ou expirado

```bash
DELETE	/api/v1/ocorrencias/{id}	Remover ocorrência — bloqueado se finalizada
```

Ocorrências com status FINALIZADA não podem ser removidas.

> Códigos de Status:
> - 204	Ocorrência removida — sem corpo na resposta 
> - 409 / 422	Ocorrência finalizada não pode ser removida 
> - 404	Ocorrência não encontrada 
> - 401	Token ausente ou expirado


### 5. Casos de Erro — Validação e Segurança
   Estes cenários validam o comportamento da API fora do fluxo feliz. 
   Todos devem retornar mensagens de erro claras e códigos HTTP adequados.

| Cenário  | HTTP Esperado | Como testar  |
|---|---|---|
| Acessar qualquer endpoint sem token  | 401 | Remova o header Authorization e faça qualquer GET |
| Token expirado (após 30 min)  |  401 |  Aguarde o token expirar e tente qualquer chamada |
| ID inexistente em qualquer recurso  |  404 | GET /api/v1/clientes/99999  |
| CPF inválido ao criar cliente  |  400 | Use nroCpf: '000.000.000-00'  |
| CPF com formato incorreto  | 400 | Use nroCpf: '12345678900' (sem pontuação esperada) |
| CEP com tamanho diferente de 8 dígitos  | 400 | Use nroCep: '0131010' (7 dígitos)  |
| Campo obrigatório ausente (nmeCliente)  | 	400 | Omita o campo nmeCliente no POST /clientes |
| Email inválido no login  | 400 | Use email: 'nao-e-um-email' |
| Finalizar ocorrência já finalizada | 4xx  | PATCH /finalizar duas vezes no mesmo ID |
| Upload de foto em ocorrência finalizada | 4xx	 | POST /fotos após finalizar a ocorrência  |
| Deletar ocorrência finalizada  | 4xx  |  	DELETE em ocorrência com status FINALIZADA |

Ocorrências API  |  Testes via Swagger UI: http://localhost:8080/swagger-ui.html
---

## Parar os containers

```bash
docker compose down

# Remover volumes (apaga dados)
docker compose down -v
```

---

## O que foi implementado

- [x] Autenticação JWT com expiração de 30 minutos
- [x] CRUD completo de Cliente, Endereço e Ocorrência
- [x] Cadastro de ocorrência com cliente + endereço + imagens em uma única requisição
- [x] Listagem com filtros por nome, CPF, data e cidade
- [x] Ordenação por `dtaOcorrencia` e `endereco.nmeCidade` (asc/desc)
- [x] Paginação em todos os endpoints de listagem
- [x] Upload de imagens para MinIO com URL assinada de retorno
- [x] Endpoint de finalização com regra de imutabilidade
- [x] Flyway migrations para criação e população das tabelas
- [x] Docker Compose orquestrando API + PostgreSQL + MinIO
