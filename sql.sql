-- ============================================================
-- AgroVet - Script de populacao do banco de dados
-- Execute no DBeaver conectado ao banco ecommerce_db
-- ============================================================

USE ecommerce_db;

SET FOREIGN_KEY_CHECKS = 0;

-- Limpa todas as tabelas
TRUNCATE TABLE wishlist_items;
TRUNCATE TABLE product_reviews;
TRUNCATE TABLE order_items;
TRUNCATE TABLE payments;
TRUNCATE TABLE orders;
TRUNCATE TABLE cart_items;
TRUNCATE TABLE carts;
TRUNCATE TABLE addresses;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE coupons;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- USUARIOS
-- ============================================================
INSERT INTO users (id, name, email, password, role, active) VALUES
                                                                (1, 'Admin AgroVet',    'admin@agrovet.com',  '$2a$10$v9m5LaR9uL50FY6he8h/4O6SGn9NP/dVIKSQfOyUQX8fRoaYc/RDO', 'ADMIN',    1),
                                                                (2, 'Joao Silva',       'joao@email.com',     '$2a$10$0y6GIZ3RvpcbO2db6z5AH.ylP5fURujaGYLt/4XVa8ll.9vYOAtY.', 'CUSTOMER', 1),
                                                                (3, 'Maria Santos',     'maria@email.com',    '$2a$10$0y6GIZ3RvpcbO2db6z5AH.ylP5fURujaGYLt/4XVa8ll.9vYOAtY.', 'CUSTOMER', 1),
                                                                (4, 'Pedro Costa',      'pedro@email.com',    '$2a$10$0y6GIZ3RvpcbO2db6z5AH.ylP5fURujaGYLt/4XVa8ll.9vYOAtY.', 'CUSTOMER', 1),
                                                                (5, 'Ana Oliveira',     'ana@email.com',      '$2a$10$0y6GIZ3RvpcbO2db6z5AH.ylP5fURujaGYLt/4XVa8ll.9vYOAtY.', 'CUSTOMER', 1),
                                                                (6, 'Carlos Mendes',   'carlos@email.com',   '$2a$10$0y6GIZ3RvpcbO2db6z5AH.ylP5fURujaGYLt/4XVa8ll.9vYOAtY.', 'CUSTOMER', 1);

-- Senhas: admin123 para admin, 123456 para os demais

-- ============================================================
-- CATEGORIAS
-- ============================================================
INSERT INTO categories (id, name, description, active) VALUES
                                                           (1, 'Medicamentos Veterinarios', 'Medicamentos e vacinas para animais',         1),
                                                           (2, 'Racoes e Suplementos',      'Racoes premium e suplementos nutricionais',   1),
                                                           (3, 'Equipamentos Agricolas',    'Equipamentos e ferramentas para o campo',     1),
                                                           (4, 'Acessorios para Animais',   'Coleiras, bebedouros e acessorios',           1),
                                                           (5, 'Sementes e Fertilizantes',  'Sementes selecionadas e fertilizantes',       1),
                                                           (6, 'Higiene e Limpeza Animal',  'Shampoos, desinfetantes e produtos de higiene',1);

-- ============================================================
-- PRODUTOS
-- ============================================================
INSERT INTO products (id, name, description, price, stock_quantity, active, image_url, category_id) VALUES
-- Medicamentos
(1,  'Ivermectina 1% Injectable 500ml',   'Antiparasitario de amplo espectro para bovinos e suinos',        89.90,  150, 1, NULL, 1),
(2,  'Vacina Aftosa Dose 10ml',           'Vacina contra febre aftosa dose unitaria',                       12.50,  500, 1, NULL, 1),
(3,  'Antibiotico Pen-Strep 100ml',       'Associacao de penicilina e estreptomicina injetavel',            45.00,  200, 1, NULL, 1),
(4,  'Carrapaticida Spray 500ml',         'Solucao carrapaticida e moscicida de rapida acao',               34.90,  300, 1, NULL, 1),
(5,  'Vermifugo Equinos 30g',             'Pasta oral para controle de helmintos em equinos',               28.00,  180, 1, NULL, 1),
-- Racoes
(6,  'Racao Premium Bovinos 30kg',        'Racao concentrada de alto desempenho para bovinos em engorda',  149.90,   80, 1, NULL, 2),
(7,  'Sal Mineral Completo 30kg',         'Mistura mineral completa para bovinos e ovinos',                 89.00,  120, 1, NULL, 2),
(8,  'Racao Suinos Crescimento 20kg',     'Racao balanceada fase crescimento para suinos',                  98.50,   60, 1, NULL, 2),
(9,  'Suplemento Proteico Aves 10kg',     'Nucleo proteico para aves de postura e corte',                   67.90,   90, 1, NULL, 2),
(10, 'Racao Equinos Performance 25kg',    'Formulacao especial para equinos de alta performance',          189.00,   40, 1, NULL, 2),
-- Equipamentos
(11, 'Pulverizador Costal 20L',           'Pulverizador manual com bomba de pressao constante',            245.00,   35, 1, NULL, 3),
(12, 'Balanca Digital Bovina 2000kg',     'Balanca eletronica com plataforma para pesagem de bovinos',   3890.00,    8, 1, NULL, 3),
(13, 'Ordenhadeira Portatil 2 Teteiras',  'Ordenhadeira a vacuo portatil para pequenos produtores',      1290.00,   12, 1, NULL, 3),
(14, 'Enxada Agricola Cabo Longo',        'Enxada de aco forjado com cabo de madeira 1.5m',                45.90,  200, 1, NULL, 3),
(15, 'Bebedouro Automatico Bovinos',      'Bebedouro flutuante automatico em inox para pasto',            389.00,   25, 1, NULL, 3),
-- Acessorios
(16, 'Coleira Antipulga Caes Grande',     'Coleira repelente duracao 8 meses para caes acima 25kg',        39.90,  250, 1, NULL, 4),
(17, 'Brinco Identificacao Bovinos 50un', 'Brincos numerados para identificacao do rebanho',               89.00,  100, 1, NULL, 4),
(18, 'Cabresto para Equinos Couro',       'Cabresto artesanal em couro legitimo com argolas inox',         75.00,   45, 1, NULL, 4),
(19, 'Protetor Casco Bezerros',           'Protetor de casco em borracha resistente para bezerros',        28.50,   80, 1, NULL, 4),
-- Sementes
(20, 'Semente Soja Certificada 40kg',     'Semente de soja de alta produtividade tratada fungicida',      320.00,   50, 1, NULL, 5),
(21, 'Semente Milho Hibrido 5kg',         'Hibrido de milho adaptado regiao sul resistente a seca',       189.00,   70, 1, NULL, 5),
(22, 'Fertilizante NPK 10-10-10 25kg',    'Fertilizante granulado para pastagens e culturas',             145.00,   90, 1, NULL, 5),
(23, 'Semente Brachiaria Brizantha 5kg',  'Sementes de braquiaria para formacao de pastagens',             98.00,  110, 1, NULL, 5),
-- Higiene
(24, 'Shampoo Veterinario Antipulgas',    'Shampoo com permetrina para caes e gatos 500ml',                24.90,  300, 1, NULL, 6),
(25, 'Desinfetante Ambiental 5L',         'Desinfetante de amplo espectro para instalacoes rurais',        67.00,  150, 1, NULL, 6),
(26, 'Cal Virgem Hidratada 20kg',         'Cal para higienizacao de instalacoes e solo',                   35.00,  200, 1, NULL, 6),
(27, 'Escova para Bovinos Parede',        'Escova automatica giratoria para higiene e conforto bovino',   890.00,    6, 1, NULL, 6);

-- ============================================================
-- CUPONS
-- ============================================================
INSERT INTO coupons (id, code, discount_type, discount_value, minimum_order_value, max_usages, current_usages, expires_at, active) VALUES
                                                                                                                                       (1, 'BEMVINDO10', 'PERCENTAGE', 10.00, 100.00, 1000, 247, DATE_ADD(NOW(), INTERVAL 6 MONTH),  1),
                                                                                                                                       (2, 'FRETE30',    'FIXED',      30.00, 200.00,  500, 189, DATE_ADD(NOW(), INTERVAL 3 MONTH),  1),
                                                                                                                                       (3, 'PRODUTOR20', 'PERCENTAGE', 20.00, 300.00,  200,  98, DATE_ADD(NOW(), INTERVAL 2 MONTH),  1),
                                                                                                                                       (4, 'NATAL50',    'FIXED',      50.00, 500.00,  100, 100, DATE_SUB(NOW(), INTERVAL 10 DAY),   0);

-- ============================================================
-- PEDIDOS
-- ============================================================
INSERT INTO orders (id, user_id, status, total, discount, created_at, active, address_id, coupon_id) VALUES
                                                                                                         (1,  2, 'DELIVERED', 239.80,  0.00, DATE_SUB(NOW(), INTERVAL 30 DAY), 1, NULL, NULL),
                                                                                                         (2,  3, 'DELIVERED', 238.90,  0.00, DATE_SUB(NOW(), INTERVAL 25 DAY), 1, NULL, NULL),
                                                                                                         (3,  4, 'CONFIRMED', 434.00,  0.00, DATE_SUB(NOW(), INTERVAL 15 DAY), 1, NULL, NULL),
                                                                                                         (4,  2, 'SHIPPED',    79.90,  0.00, DATE_SUB(NOW(), INTERVAL 10 DAY), 1, NULL, NULL),
                                                                                                         (5,  3, 'DELIVERED', 355.40,  0.00, DATE_SUB(NOW(), INTERVAL 18 DAY), 1, NULL, NULL),
                                                                                                         (6,  4, 'PENDING',   318.90,  0.00, DATE_SUB(NOW(), INTERVAL 5 DAY),  1, NULL, NULL),
                                                                                                         (7,  2, 'CANCELLED', 323.00,  0.00, DATE_SUB(NOW(), INTERVAL 3 DAY),  0, NULL, NULL),
                                                                                                         (8,  5, 'DELIVERED', 246.40,  0.00, DATE_SUB(NOW(), INTERVAL 20 DAY), 1, NULL, NULL),
                                                                                                         (9,  6, 'DELIVERED', 214.90, 21.49, DATE_SUB(NOW(), INTERVAL 12 DAY), 1, NULL, 1),
                                                                                                         (10, 5, 'CONFIRMED', 191.20, 38.24, DATE_SUB(NOW(), INTERVAL 8 DAY),  1, NULL, 3),
                                                                                                         (11, 3, 'DELIVERED', 170.00,  0.00, DATE_SUB(NOW(), INTERVAL 22 DAY), 1, NULL, NULL),
                                                                                                         (12, 6, 'SHIPPED',   268.90,  0.00, DATE_SUB(NOW(), INTERVAL 7 DAY),  1, NULL, NULL);

-- ============================================================
-- ITENS DOS PEDIDOS
-- ============================================================
INSERT INTO order_items (id, order_id, product_id, quantity, unit_price, subtotal) VALUES
-- Pedido 1
(1,  1,  1, 1,  89.90,  89.90),
(2,  1,  4, 2,  34.90,  69.80),
(3,  1,  2, 4,  12.50,  50.00),
(4,  1, 24, 1,  24.90,  24.90),
-- Pedido 2
(5,  2,  6, 1, 149.90, 149.90),
(6,  2, 24, 2,  24.90,  49.80),
(7,  2, 16, 1,  39.90,  39.90),
-- Pedido 3
(8,  3, 11, 1, 245.00, 245.00),
(9,  3, 14, 2,  45.90,  91.80),
(10, 3, 24, 4,  24.90,  99.60),
-- Pedido 4
(11, 4,  4, 1,  34.90,  34.90),
(12, 4,  2, 2,  12.50,  25.00),
(13, 4, 24, 1,  24.90,  24.90),
-- Pedido 5
(14, 5,  9, 1,  67.90,  67.90),
(15, 5,  8, 1,  98.50,  98.50),
(16, 5, 22, 1, 145.00, 145.00),
(17, 5, 24, 2,  24.90,  49.80),
-- Pedido 6
(18, 6, 20, 1, 320.00, 320.00),
(19, 6, 16, 1,  39.90,  39.90),
-- Pedido 7
(20, 7, 13, 1,1290.00,1290.00),
-- Pedido 8
(21, 8,  1, 1,  89.90,  89.90),
(22, 8,  5, 2,  28.00,  56.00),
(23, 8, 22, 1, 145.00, 145.00),
-- Pedido 9
(24, 9,  3, 2,  45.00,  90.00),
(25, 9,  7, 1,  89.00,  89.00),
(26, 9, 16, 1,  39.90,  39.90),
-- Pedido 10
(27,10, 21, 1, 189.00, 189.00),
(28,10, 23, 1,  98.00,  98.00),
(29,10, 16, 1,  39.90,  39.90),
-- Pedido 11
(30,11, 14, 2,  45.90,  91.80),
(31,11, 25, 1,  67.00,  67.00),
(32,11, 24, 1,  24.90,  24.90),
-- Pedido 12
(33,12,  6, 1, 149.90, 149.90),
(34,12, 22, 1, 145.00, 145.00);

-- ============================================================
-- PAGAMENTOS
-- ============================================================
INSERT INTO payments (id, order_id, status, amount, preference_id, mercado_pago_payment_id, created_at, paid_at) VALUES
                                                                                                                     (1,  1, 'APPROVED', 239.80, UUID(), UUID(), DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
                                                                                                                     (2,  2, 'APPROVED', 238.90, UUID(), UUID(), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
                                                                                                                     (3,  3, 'APPROVED', 434.00, UUID(), UUID(), DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
                                                                                                                     (4,  4, 'APPROVED',  79.90, UUID(), UUID(), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
                                                                                                                     (5,  5, 'APPROVED', 355.40, UUID(), UUID(), DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),
                                                                                                                     (6,  6, 'PENDING',  318.90, UUID(), NULL,   DATE_SUB(NOW(), INTERVAL 5 DAY),  NULL),
                                                                                                                     (7,  7, 'FAILED',   323.00, UUID(), NULL,   DATE_SUB(NOW(), INTERVAL 3 DAY),  NULL),
                                                                                                                     (8,  8, 'APPROVED', 246.40, UUID(), UUID(), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
                                                                                                                     (9,  9, 'APPROVED', 214.90, UUID(), UUID(), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
                                                                                                                     (10,10, 'APPROVED', 191.20, UUID(), UUID(), DATE_SUB(NOW(), INTERVAL 8 DAY),  DATE_SUB(NOW(), INTERVAL 8 DAY)),
                                                                                                                     (11,11, 'APPROVED', 170.00, UUID(), UUID(), DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY)),
                                                                                                                     (12,12, 'APPROVED', 268.90, UUID(), UUID(), DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_SUB(NOW(), INTERVAL 7 DAY));

-- ============================================================
-- AVALIACOES
-- ============================================================
INSERT INTO product_reviews (id, user_id, product_id, rating, comment, created_at, active) VALUES
                                                                                               (1, 2,  1, 5, 'Produto excelente, funcionou muito bem no meu rebanho!',          DATE_SUB(NOW(), INTERVAL 28 DAY), 1),
                                                                                               (2, 3,  6, 5, 'Racao de otima qualidade, meus bovinos adoraram!',               DATE_SUB(NOW(), INTERVAL 23 DAY), 1),
                                                                                               (3, 4, 11, 4, 'Pulverizador muito bom, apenas um pouco pesado.',                DATE_SUB(NOW(), INTERVAL 13 DAY), 1),
                                                                                               (4, 2,  4, 5, 'Acabou com os carrapatos rapidamente. Recomendo!',               DATE_SUB(NOW(), INTERVAL 28 DAY), 1),
                                                                                               (5, 3,  9, 4, 'Bom suplemento, percebi melhora nas aves em 2 semanas.',         DATE_SUB(NOW(), INTERVAL 16 DAY), 1),
                                                                                               (6, 5,  1, 5, 'Produto original e eficaz. Chegou bem embalado.',                DATE_SUB(NOW(), INTERVAL 18 DAY), 1),
                                                                                               (7, 6,  3, 5, 'Otimo antibiotico, resolveu a infeccao do meu gado rapidamente.',DATE_SUB(NOW(), INTERVAL 10 DAY), 1),
                                                                                               (8, 5, 22, 4, 'Fertilizante de qualidade, boa relacao custo beneficio.',        DATE_SUB(NOW(), INTERVAL 6 DAY),  1);

SELECT 'Banco populado com sucesso!' AS resultado;
SELECT COUNT(*) AS total_usuarios   FROM users;
SELECT COUNT(*) AS total_categorias FROM categories;
SELECT COUNT(*) AS total_produtos   FROM products;
SELECT COUNT(*) AS total_pedidos    FROM orders;
SELECT COUNT(*) AS total_pagamentos FROM payments;
SELECT COUNT(*) AS total_avaliacoes FROM product_reviews;
SELECT COUNT(*) AS total_cupons     FROM coupons;