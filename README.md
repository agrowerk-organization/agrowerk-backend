# 🌾 AgroWerk - Backend

> Sistema de inventário agrícola robusto e seguro desenvolvido com Spring Boot

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
![Postgres](https://img.shields.io/badge/Postgres-15-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

## 📋 Sobre o Projeto

**AgroWerk** (do alemão: *Agro* = Agricultura + *Werk* = Obra/Trabalho) é uma API RESTful completa para gestão de inventário agrícola, conectando produtores rurais, fornecedores e administradores em uma plataforma integrada de controle de insumos e estoque.

### ✨ Principais Funcionalidades

- 🔐 **Sistema Multi-tenant** com três tipos de usuários (Admin do Sistema, Admin de Fornecedor, Produtor)
- 📦 **Gestão de Estoque** - controle completo de insumos agrícolas e movimentações
- 🏭 **Gerenciamento de Fornecedores** com especialidades e categorias
- 🌾 **Controle de Propriedades Rurais** e lotes de produção
- 📊 **Rastreabilidade de Lotes (Batch)** para controle de qualidade
- 🔄 **API RESTful** seguindo padrões de arquitetura limpa
- 🛡️ **Segurança robusta** com validações, filtros e interceptors
- 📝 **Documentação interativa** com Swagger/OpenAPI

## 🚀 Tecnologias Utilizadas

### Core
- **Java 21** - Linguagem base
- **Spring Boot 4.x** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL 15** - Banco de dados relacional
- **Redis 7** - Banco de dados em memória para cache

### Segurança
- **JWT (JSON Web Tokens)** - Autenticação stateless
- **BCrypt** - Hash de senhas
- **Spring Security** - Proteção de endpoints

### Documentação & Testes
- **Swagger/OpenAPI** - Documentação da API
- **JUnit 5** - Testes unitários
- **Mockito** - Mocks para testes

### Ferramentas
- **Gradle** - Gerenciamento de dependências e build
- **Lombok** - Redução de boilerplate
- **Bean Validation** - Validação de dados
- **MapStruct** - Mapeamento de DTOs

## 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- Docker e Docker Compose
- Git

## ⚙️ Instalação e Configuração

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/agrowerk-backend.git
cd agrowerk-backend
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env.local` na raíz do projeto e coloque suas variáveis de ambiente e credenciais, não suba para a plataforma de versionamento:

```env.local
# Client side
CLIENT_ENDPOINT=

# Database
POSTGRESQL_HOST=
POSTGRESQL_PORT=
POSTGRESQL_ROOT_PASSWORD=
POSTGRESQL_DATABASE=
POSTGRESQL_USER=
POSTGRESQL_PASSWORD=

# Logging
SHOW_SQL=
FORMAT_SQL=
LOG_LEVEL=
APP_LOG_LEVEL=
APP_LOG_LEVEL_HIBERNATE=

# Jwt
JWT_EXPIRATION=
JWT_REFRESH_EXPIRATION=
JWT_ISSUER=

# CORS
CORS_ORIGINS=

# Cloudinary
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
```

### 3. Execute o projeto

```docker
   docker compose --env-file .env-file up --build
```

A API estará disponível em: `http://localhost:8080`

## 📚 Documentação da API

Após iniciar a aplicação, acesse a documentação interativa:

```
http://localhost:8080/swagger-ui.html
```

### Principais Endpoints

#### Autenticação
- `POST /api/auth/register` - Registrar novo usuário
- `POST /api/auth/login` - Realizar login
- `POST /api/auth/refresh` - Renovar token

#### Usuários
- `GET /api/users` - Listar usuários (requer permissão)
- `GET /api/users/{id}` - Buscar usuário por ID
- `PUT /api/users/{id}` - Atualizar dados do usuário

#### Insumos (Inputs)
- `GET /api/inputs` - Listar todos os insumos
- `POST /api/inputs` - Cadastrar novo insumo
- `GET /api/inputs/{id}` - Buscar insumo por ID
- `PUT /api/inputs/{id}` - Atualizar insumo
- `GET /api/inputs/category/{categoryId}` - Listar por categoria

#### Estoque (Stock)
- `GET /api/stock` - Listar itens em estoque
- `POST /api/stock` - Adicionar item ao estoque
- `PUT /api/stock/{id}` - Atualizar quantidade em estoque
- `GET /api/stock/property/{propertyId}` - Estoque por propriedade

#### Gestão de Estoque (Stock Management)
- `POST /api/stock-management` - Registrar movimentação
- `GET /api/stock-management/history` - Histórico de movimentações
- `GET /api/stock-management/batch/{batchId}` - Movimentações por lote

#### Fornecedores (Suppliers)
- `GET /api/suppliers` - Listar fornecedores
- `POST /api/suppliers` - Cadastrar fornecedor
- `GET /api/suppliers/{id}` - Buscar fornecedor
- `GET /api/suppliers/specialty/{specialtyId}` - Fornecedores por especialidade

#### Propriedades (Properties)
- `GET /api/properties` - Listar propriedades rurais
- `POST /api/properties` - Cadastrar propriedade
- `GET /api/properties/{id}` - Buscar propriedade por ID

#### Lotes (Batches)
- `GET /api/batches` - Listar lotes de produção
- `POST /api/batches` - Criar novo lote
- `GET /api/batches/{id}` - Buscar lote por ID
- `GET /api/batches/property/{propertyId}` - Lotes por propriedade

## 🏗️ Arquitetura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── tech/
│   │       └── agrowerk/
│   │           ├── application/           # Camada de aplicação
│   │           │   ├── controller/        # Controllers REST
│   │           │   ├── dto/               # Data Transfer Objects
│   │           │   └── initializer/       # Inicializadores
│   │           ├── business/              # Camada de negócio
│   │           │   ├── filter/            # Filtros de requisição
│   │           │   ├── interceptors/      # Interceptors
│   │           │   ├── mapper/            # Mapeadores DTO ↔ Entity
│   │           │   ├── service/           # Lógica de negócio
│   │           │   ├── utils/             # Utilitários
│   │           │   └── validators/        # Validadores customizados
│   │           └── infrastructure/        # Camada de infraestrutura
│   │               ├── config/            # Configurações (Security, Swagger)
│   │               ├── enums/             # Enumerações
│   │               ├── exception/         # Tratamento de exceções
│   │               ├── model/             # Entidades JPA
│   │               ├── repository/        # Repositories Spring Data
│   │               └── security/          # Configurações de segurança
│   └── resources/
│       ├── application.properties         # Configurações da aplicação
│       └── db.migrations                  # Scripts SQL iniciais
└── test/                                  # Testes automatizados
```

### 🗂️ Modelo de Dados

#### Entidades Principais

**User** - Usuários do sistema
- `SYSTEM_ADMIN` - Administrador do sistema
- `SUPPLIER_ADMIN` - Administrador de fornecedor
- `PRODUCER` - Produtor rural

**Property** - Propriedades rurais cadastradas

**Supplier** - Fornecedores de insumos
- Relacionamento com `SupplierSpecialty` via `SupplierSpecialtyLink`

**Input** - Insumos agrícolas
- Relacionamento com `InputCategory` para categorização

**Stock** - Estoque atual de insumos por propriedade

**StockManagement** - Histórico de movimentações de estoque
- Vinculado a lotes (`Batch`) para rastreabilidade

**Batch** - Lotes de produção para controle de qualidade

## 🔒 Segurança

O sistema implementa múltiplas camadas de segurança:

- ✅ Autenticação via JWT com refresh tokens
- ✅ Autorização baseada em roles (SYSTEM_ADMIN, SUPPLIER_ADMIN, PRODUCER)
- ✅ Filtros customizados para validação de requisições
- ✅ Interceptors para logging e auditoria
- ✅ Validação de dados com Bean Validation
- ✅ Proteção contra SQL Injection via JPA
- ✅ Criptografia de senhas com BCrypt
- ✅ CORS configurado para ambientes específicos
- ✅ Validadores personalizados para regras de negócio

## 🧪 Testes

Execute os testes unitários e de integração:

```bash
./gradlew test
```

Gerar relatório de cobertura:

```bash
./gradlew jacocoTestReport
```

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👨‍💻 Autor

**Douglas Holanda**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](www.linkedin.com/in/douglas-holanda-113519269)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Doug16Yanc)

## 📞 Contato

Para dúvidas ou sugestões, entre em contato:

- 📧 Email: douglasholanda3195@gmail.com
- 💼 LinkedIn: [Douglas Holanda](www.linkedin.com/in/douglas-holanda-113519269)

---

<div align="center">
  Desenvolvido com ☕ e 💚 por Douglas Holanda
</div>
