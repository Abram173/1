INSERT INTO customers (id, first_name, last_name, email)
VALUES (1, 'Ivan', 'Ivanov', 'ivan@example.com')
ON CONFLICT DO NOTHING;

INSERT INTO customers (id, first_name, last_name, email)
VALUES (2, 'Petr', 'Petrov', 'petr@example.com')
ON CONFLICT DO NOTHING;

INSERT INTO accounts (id, number, balance, customer_id)
VALUES (1, 'ACC-1001', 1000.00, 1)
ON CONFLICT DO NOTHING;

INSERT INTO accounts (id, number, balance, customer_id)
VALUES (2, 'ACC-1002', 500.00, 2)
ON CONFLICT DO NOTHING;

INSERT INTO cards (id, number, blocked, account_id)
VALUES (1, 'CARD-1111-2222', FALSE, 1)
ON CONFLICT DO NOTHING;

