INSERT INTO departments (id, name, created_at, updated_at) VALUES
(1, '開発部', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '営業部', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '人事部', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- パスワード: password123 (BCrypt)
INSERT INTO employees (id, employee_code, name, email, password_hash, department_id, role, created_at, updated_at) VALUES
(1, 'MGR001', '山田太郎', 'yamada@example.com', '$2a$10$bryzzpRE0Y7RT5JM5wJd/eg6pgegG5zXHibwDwKb3AjvW6HFz/VBe', 1, 'MANAGER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'EMP001', '鈴木花子', 'suzuki@example.com', '$2a$10$bryzzpRE0Y7RT5JM5wJd/eg6pgegG5zXHibwDwKb3AjvW6HFz/VBe', 1, 'EMPLOYEE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'EMP002', '佐藤次郎', 'sato@example.com', '$2a$10$bryzzpRE0Y7RT5JM5wJd/eg6pgegG5zXHibwDwKb3AjvW6HFz/VBe', 1, 'EMPLOYEE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'MGR002', '田中三郎', 'tanaka@example.com', '$2a$10$bryzzpRE0Y7RT5JM5wJd/eg6pgegG5zXHibwDwKb3AjvW6HFz/VBe', 2, 'MANAGER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

UPDATE departments SET manager_id = 1 WHERE id = 1;
UPDATE departments SET manager_id = 4 WHERE id = 2;
