-- This file allow to write SQL commands that will be emitted in test and dev.
-- Quarkus ORM (Hibernate) will handle Gift table creation and data
-- Sample data for Gift table (handled by Quarkus ORM)
INSERT INTO gifts (id, name, description, price, category) VALUES (1, 'Wireless Headphones', 'High-quality bluetooth headphones', 89.99, 'Electronics');
INSERT INTO gifts (id, name, description, price, category) VALUES (2, 'Coffee Mug', 'Ceramic coffee mug with custom design', 15.99, 'Home');
INSERT INTO gifts (id, name, description, price, category) VALUES (3, 'Book Set', 'Collection of classic literature', 45.00, 'Books');
ALTER SEQUENCE gifts_seq RESTART WITH 4;