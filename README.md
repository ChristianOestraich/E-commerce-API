# E-Commerce API

API REST completa para plataforma de e-commerce, desenvolvida com Java e Spring Boot.

---

## Tecnologias

- **Java 21**
- **Spring Boot 3.5**
- **Spring Security + JWT**
- **Spring Data JPA**
- **MySQL**
- **Redis (Cache)**
- **MercadoPago SDK**
- **Spring Mail (Gmail SMTP)**
- **SpringDoc OpenAPI (Swagger)**
- **JUnit 5 + Mockito (Testes)**
- **Lombok**
- **Maven**

---

## Como executar

### Pré-requisitos

- Java 21
- Maven
- MySQL rodando localmente
- Redis rodando localmente
- Conta no [MercadoPago Developers](https://www.mercadopago.com.br/developers)
- Conta Gmail com senha de app configurada

### Instalando o Redis (Mac)

```bash
brew install redis
brew services start redis
redis-cli ping
# Deve retornar: PONG
```

### Configuração do `application.properties`

```properties
spring.application.name=e-commerce

# Servidor
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=sua_senha
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT
jwt.secret=3f8a2b1c9d4e7f6a0b5c8d2e1f4a7b3c9e2d5f8a1b4c7e0d3f6a9b2c5e8d1f4
jwt.expiration=900000
jwt.refresh.expiration=604800000

# MercadoPago
mercadopago.access.token=TEST-SEU-ACCESS-TOKEN

# Upload
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
file.upload.dir=uploads/images

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seuemail@gmail.com
spring.mail.password=sua_senha_de_app
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis
spring.cache.redis.time-to-live=600000
```

### Executando o projeto

```bash
mvn clean install
mvn spring-boot:run
```

### Acessando o Swagger

```
http://localhost:8080/swagger-ui/index.html
```

---

## Autenticação

A API utiliza **JWT (JSON Web Token)** stateless com **Access Token** e **Refresh Token**.

| Campo | Valor |
|---|---|
| Access Token | Validade de 15 minutos |
| Refresh Token | Validade de 7 dias |

### Como autenticar no Swagger

1. Faça login em `POST /api/auth/login`
2. Copie o `accessToken` retornado
3. Clique em **Authorize** no Swagger
4. Cole o token e clique em **Authorize**

### Perfis de acesso

| Role | Descricao |
|---|---|
| `CUSTOMER` | Criado automaticamente no registro |
| `ADMIN` | Promovido via `PUT /api/users/{id}` ou direto no banco |

---

## Endpoints

### Autenticacao — `/api/auth`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| POST | `/api/auth/register` | Registra novo usuario | Publico |
| POST | `/api/auth/login` | Realiza login | Publico |
| POST | `/api/auth/refresh` | Gera novo access token | Publico |
| POST | `/api/auth/logout` | Revoga o refresh token | Publico |

**Exemplo de registro:**
```json
POST /api/auth/register
{
  "name": "Christian",
  "email": "christian@email.com",
  "password": "123456"
}
```

**Resposta:**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "uuid-gerado",
  "role": "CUSTOMER"
}
```

---

### Usuarios — `/api/users`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| GET | `/api/users` | Lista usuarios ativos | ADMIN |
| GET | `/api/users/all` | Lista todos os usuarios | ADMIN |
| GET | `/api/users/{id}` | Busca usuario por ID | ADMIN |
| PUT | `/api/users/{id}` | Edita nome e role | ADMIN |
| DELETE | `/api/users/{id}` | Inativa usuario (soft delete) | ADMIN |

---

### Categorias — `/api/categories`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| POST | `/api/categories` | Cria categoria | ADMIN |
| GET | `/api/categories` | Lista categorias ativas | Autenticado |
| GET | `/api/categories/all` | Lista todas as categorias | ADMIN |
| GET | `/api/categories/{id}` | Busca categoria por ID | Autenticado |
| PUT | `/api/categories/{id}` | Atualiza categoria | ADMIN |
| DELETE | `/api/categories/{id}` | Inativa categoria (soft delete) | ADMIN |

---

### Produtos — `/api/products`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| POST | `/api/products` | Cria produto | ADMIN |
| GET | `/api/products` | Lista produtos ativos | Autenticado |
| GET | `/api/products/all` | Lista todos os produtos | ADMIN |
| GET | `/api/products/{id}` | Busca produto por ID | Autenticado |
| GET | `/api/products/search?name=x` | Busca por nome | Autenticado |
| GET | `/api/products/category/{id}` | Filtra por categoria | Autenticado |
| PUT | `/api/products/{id}` | Atualiza produto | ADMIN |
| DELETE | `/api/products/{id}` | Inativa produto (soft delete) | ADMIN |
| POST | `/api/products/{id}/image` | Upload de imagem | ADMIN |
| DELETE | `/api/products/{id}/image` | Remove imagem | ADMIN |

---

### Imagens — `/api/images`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| GET | `/api/images/{filename}` | Acessa imagem pelo nome | Publico |

Formatos aceitos: `jpg`, `jpeg`, `png`, `webp`. Tamanho maximo: `5MB`.

---

### Enderecos — `/api/addresses`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| POST | `/api/addresses` | Cadastra endereco | Autenticado |
| GET | `/api/addresses` | Lista meus enderecos | Autenticado |
| GET | `/api/addresses/{id}` | Busca endereco por ID | Autenticado |
| PUT | `/api/addresses/{id}` | Atualiza endereco | Autenticado |
| DELETE | `/api/addresses/{id}` | Remove endereco | Autenticado |
| PATCH | `/api/addresses/{id}/main` | Define como endereco principal | Autenticado |

---

### Carrinho — `/api/cart`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| GET | `/api/cart` | Visualiza carrinho | Autenticado |
| POST | `/api/cart/items` | Adiciona item | Autenticado |
| PUT | `/api/cart/items/{itemId}` | Atualiza quantidade | Autenticado |
| DELETE | `/api/cart/items/{itemId}` | Remove item | Autenticado |
| DELETE | `/api/cart` | Limpa o carrinho | Autenticado |
| POST | `/api/cart/coupon` | Aplica cupom | Autenticado |
| DELETE | `/api/cart/coupon` | Remove cupom | Autenticado |

**Exemplo de adicionar item:**
```json
POST /api/cart/items
{
  "productId": 1,
  "quantity": 2
}
```

---

### Cupons — `/api/coupons`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| POST | `/api/coupons` | Cria cupom | ADMIN |
| GET | `/api/coupons` | Lista cupons ativos | Autenticado |
| GET | `/api/coupons/all` | Lista todos os cupons | ADMIN |
| DELETE | `/api/coupons/{id}` | Inativa cupom | ADMIN |

**Tipos de desconto:**

| Tipo | Descricao |
|---|---|
| `PERCENTAGE` | Percentual sobre o total (ex: 10%) |
| `FIXED` | Valor fixo de desconto (ex: R$ 20,00) |

**Exemplo de criacao:**
```json
POST /api/coupons
{
  "code": "DESCONTO10",
  "discountType": "PERCENTAGE",
  "discountValue": 10,
  "minimumOrderValue": 50.00,
  "maxUsages": 100,
  "expiresAt": "2026-12-31T23:59:59"
}
```

---

### Pedidos — `/api/orders`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| POST | `/api/orders/checkout` | Gera pedido do carrinho | Autenticado |
| POST | `/api/orders/checkout?addressId={id}` | Checkout com endereco especifico | Autenticado |
| DELETE | `/api/orders/{id}/cancel` | Cancela pedido + devolve estoque | Autenticado |
| GET | `/api/orders/my` | Lista meus pedidos | Autenticado |
| GET | `/api/orders/my/{id}` | Detalha meu pedido | Autenticado |
| GET | `/api/orders` | Lista todos os pedidos ativos | ADMIN |
| GET | `/api/orders/all` | Lista todos + cancelados | ADMIN |
| PATCH | `/api/orders/{id}/status?status=X` | Atualiza status | ADMIN |

**Status possiveis:**

| Status | Descricao |
|---|---|
| `PENDING` | Aguardando pagamento |
| `CONFIRMED` | Pagamento confirmado |
| `SHIPPED` | Pedido enviado |
| `DELIVERED` | Pedido entregue |
| `CANCELLED` | Pedido cancelado |

**Regras de estoque:**

| Acao | Efeito |
|---|---|
| Checkout | Deduz quantidade de cada produto |
| Cancelamento | Devolve quantidade de cada produto |

---

### Pagamentos — `/api/payments`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| POST | `/api/payments/order/{id}` | Inicia pagamento MercadoPago | Autenticado |
| GET | `/api/payments/success` | Callback de aprovacao | Publico |
| GET | `/api/payments/failure` | Callback de falha | Publico |
| GET | `/api/payments/pending` | Callback de pendente | Publico |
| GET | `/api/payments/order/{id}` | Consulta pagamento | Autenticado |

**Fluxo de pagamento:**
```
1. POST /api/payments/order/{orderId}
         |
         v
   Retorna initPoint (URL do checkout MercadoPago)
         |
         v
2. Usuario acessa o initPoint no navegador
         |
         v
3. Usuario paga com cartao no sandbox
         |
         v
4. MercadoPago redireciona para /api/payments/success
         |
         v
   Pedido atualizado para CONFIRMED
```

**Cartao de teste sandbox:**

| Campo | Valor |
|---|---|
| Numero | 5031 4332 1540 6351 |
| Validade | 11/25 |
| CVV | 123 |
| Nome | APRO |

---

### Avaliacoes — `/api/products`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| GET | `/api/products/{id}/reviews` | Lista avaliacoes | Publico |
| GET | `/api/products/{id}/rating` | Rating medio | Publico |
| POST | `/api/products/{id}/reviews` | Cria avaliacao | Autenticado |
| PUT | `/api/products/reviews/{id}` | Edita avaliacao | Autenticado |
| DELETE | `/api/products/reviews/{id}` | Remove propria avaliacao | Autenticado |
| DELETE | `/api/products/reviews/{id}/admin` | Remove qualquer avaliacao | ADMIN |

**Regras:**
- So pode avaliar produtos de pedidos com status `DELIVERED`
- Apenas uma avaliacao por produto por usuario
- Rating de 1 a 5 estrelas

---

### Relatorios — `/api/admin/reports`

| Metodo | Endpoint | Descricao | Parametros |
|---|---|---|---|
| GET | `/api/admin/reports/summary` | Resumo geral do dashboard | — |
| GET | `/api/admin/reports/top-products` | Produtos mais vendidos | `limit` (default 10) |
| GET | `/api/admin/reports/sales-by-period` | Vendas por periodo | `start`, `end` |
| GET | `/api/admin/reports/revenue` | Receita por periodo | `start`, `end` |
| GET | `/api/admin/reports/top-coupons` | Cupons mais usados | `limit` (default 5) |
| GET | `/api/admin/reports/low-stock` | Produtos com estoque baixo | `threshold` (default 10) |

**Exemplo de uso com datas:**
```
GET /api/admin/reports/sales-by-period
    ?start=2026-01-01T00:00:00
    &end=2026-03-28T23:59:59
```

---

### Lista de Desejos — `/api/wishlist`

| Metodo | Endpoint | Descricao | Acesso |
|---|---|---|---|
| GET | `/api/wishlist` | Lista todos os itens | Autenticado |
| POST | `/api/wishlist/{productId}` | Adiciona produto | Autenticado |
| DELETE | `/api/wishlist/{productId}` | Remove produto | Autenticado |
| GET | `/api/wishlist/{productId}/check` | Verifica se esta na wishlist | Autenticado |
| POST | `/api/wishlist/{productId}/move-to-cart?quantity=1` | Move para o carrinho | Autenticado |
| DELETE | `/api/wishlist` | Limpa toda a wishlist | Autenticado |

**Regras:**
- Um produto nao pode ser adicionado duas vezes na mesma wishlist
- Ao mover para o carrinho, o item e removido automaticamente da wishlist
- Quando o ADMIN reduz o preco de um produto, todos os usuarios que tem ele na wishlist recebem um email de notificacao

---

## Notificacoes de Email

Emails sao enviados automaticamente e de forma assincrona nos seguintes eventos:

| Evento | Email enviado |
|---|---|
| Registro | Boas vindas |
| Checkout | Pedido recebido com total |
| Pagamento aprovado | Pagamento confirmado |
| Cancelamento | Pedido cancelado |
| Status atualizado | Novo status do pedido |
| Preco reduzido | Notificacao de queda de preco para usuarios da wishlist |

---

## Estrutura do projeto

```
src/main/java/project/ecommerce/
├── config/
│   ├── MercadoPagoConfiguration.java
│   ├── RedisConfig.java
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller/
│   ├── AuthController.java
│   ├── CartController.java
│   ├── CategoryController.java
│   ├── CouponController.java
│   ├── ImageController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   ├── ProductController.java
│   ├── ProductImageController.java
│   ├── ProductReviewController.java
│   ├── ReportController.java
│   ├── UserController.java
│   └── WishlistController.java
├── dto/
│   ├── report/
│   │   ├── DashboardSummaryResponse.java
│   │   ├── LowStockResponse.java
│   │   ├── SalesByPeriodResponse.java
│   │   ├── TopCouponResponse.java
│   │   └── TopProductResponse.java
│   ├── AddressRequest.java
│   ├── AddressResponse.java
│   ├── ApplyCouponRequest.java
│   ├── AuthResponse.java
│   ├── CartItemRequest.java
│   ├── CartItemResponse.java
│   ├── CartResponse.java
│   ├── CategoryRequest.java
│   ├── CategoryResponse.java
│   ├── CouponRequest.java
│   ├── CouponResponse.java
│   ├── LoginRequest.java
│   ├── OrderItemResponse.java
│   ├── OrderResponse.java
│   ├── PaymentResponse.java
│   ├── ProductRatingResponse.java
│   ├── ProductRequest.java
│   ├── ProductResponse.java
│   ├── RefreshTokenRequest.java
│   ├── RegisterRequest.java
│   ├── ReviewRequest.java
│   ├── ReviewResponse.java
│   ├── UserResponse.java
│   ├── WishlistItemResponse.java
│   └── WishlistResponse.java
├── entity/
│   ├── enums/
│   │   ├── DiscountType.java
│   │   ├── OrderStatus.java
│   │   ├── PaymentStatus.java
│   │   └── Role.java
│   ├── Address.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Category.java
│   ├── Coupon.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Payment.java
│   ├── Product.java
│   ├── ProductReview.java
│   ├── RefreshToken.java
│   ├── User.java
│   └── WishlistItem.java
├── exception/
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── AddressRepository.java
│   ├── CartItemRepository.java
│   ├── CartRepository.java
│   ├── CategoryRepository.java
│   ├── CouponRepository.java
│   ├── OrderItemRepository.java
│   ├── OrderRepository.java
│   ├── PaymentRepository.java
│   ├── ProductRepository.java
│   ├── ProductReviewRepository.java
│   ├── RefreshTokenRepository.java
│   ├── UserRepository.java
│   └── WishlistRepository.java
├── security/
│   ├── JwtAuthFilter.java
│   └── JwtUtil.java
└── service/
    ├── AddressService.java
    ├── AuthService.java
    ├── CartService.java
    ├── CouponService.java
    ├── EmailService.java
    ├── FileStorageService.java
    ├── MercadoPagoService.java
    ├── OrderService.java
    ├── ProductReviewService.java
    ├── ProductService.java
    ├── ReportService.java
    ├── RefreshTokenService.java
    ├── UserService.java
    └── WishlistService.java

src/test/java/project/ecommerce/service/
    ├── AuthServiceTest.java
    ├── CartServiceTest.java
    ├── CouponServiceTest.java
    ├── OrderServiceTest.java
    ├── ProductServiceTest.java
    └── ReportServiceTest.java
```

---

## Boas praticas aplicadas

- **Soft Delete** em todas as entidades — nenhum dado e removido fisicamente
- **JWT stateless** com refresh token para renovacao automatica
- **@PreAuthorize** para controle de acesso por role em cada endpoint
- **@Transactional** nos metodos que alteram multiplas entidades
- **@Async** nos emails para nao bloquear a resposta da API
- **GlobalExceptionHandler** para respostas de erro padronizadas
- **Paginacao** nos endpoints de listagem com `Pageable`
- **Validacoes** com `@Valid`, `@NotBlank`, `@Min`, `@Max` nos DTOs
- **Cache com Redis** em listagens de produtos, categorias, cupons e relatorios
- **Cache Eviction** automatico ao criar, editar ou desativar entidades
- **Testes automatizados** com JUnit 5 e Mockito cobrindo os principais services

---

## Cache Redis

| Cache | TTL | Invalidado quando |
|---|---|---|
| `products` | 10 min | Criar, editar, desativar produto |
| `product` | 10 min | Editar, desativar produto |
| `categories` | 15 min | Criar, editar, desativar categoria |
| `category` | 15 min | Editar, desativar categoria |
| `coupons` | 5 min | Criar, desativar cupom |
| `dashboard` | 5 min | Checkout, cancelamento de pedido |
| `topProducts` | 5 min | Checkout, cancelamento de pedido |
| `lowStock` | 3 min | Expira automaticamente |

```bash
# Comandos uteis no Redis CLI
redis-cli ping          # Verifica conexao
redis-cli keys *        # Lista todas as chaves em cache
redis-cli flushall      # Limpa todo o cache
```

---

## Testes automatizados

```bash
# Rodar todos os testes
mvn test

# Rodar um arquivo especifico
mvn test -Dtest=AuthServiceTest

# Rodar com relatorio detalhado
mvn test -Dsurefire.useFile=false
```

| Classe | Testes | Cenarios cobertos |
|---|---|---|
| `AuthServiceTest` | 5 | Registro, login, usuario inativo, senha errada, email duplicado |
| `ProductServiceTest` | 5 | Criar, listar, buscar, desativar, categoria inexistente |
| `CartServiceTest` | 4 | Ver carrinho, adicionar item, estoque insuficiente, limpar |
| `OrderServiceTest` | 5 | Checkout, carrinho vazio, estoque, cancelar, pedido entregue |
| `CouponServiceTest` | 8 | Criar, validar, expirado, minimo, % desconto, fixo, nulo |
| `ReportServiceTest` | 5 | Dashboard, top produtos, estoque baixo, periodo invalido, receita |
| **Total** | **32** | — |

---

## Dependencias principais (`pom.xml`)

```xml
<!-- Spring Boot Starters -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation
spring-boot-starter-mail
spring-boot-starter-data-redis

<!-- Banco de dados -->
mysql-connector-j

<!-- JWT -->
jjwt-api (0.12.6)
jjwt-impl (0.12.6)
jjwt-jackson (0.12.6)

<!-- Documentacao -->
springdoc-openapi-starter-webmvc-ui (2.8.6)

<!-- Pagamento -->
mercadopago sdk-java (2.1.24)

<!-- Testes -->
spring-boot-starter-test (JUnit 5 + Mockito)

<!-- Utilitarios -->
lombok
spring-boot-devtools
```