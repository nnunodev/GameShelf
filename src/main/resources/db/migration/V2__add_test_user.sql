DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE username = 'testuser');
DELETE FROM users WHERE username = 'testuser';

-- Password: 'Testpassword123' - encoded with strength 10
INSERT INTO users (username, email, password) 
VALUES ('testuser', 'test@example.com', '$2a$10$gXzBO3a7ahZP2UuRcPgPieZ9zfau4n3/0ytSsPgv54pk5tQb23nbu');

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER'
FROM users 
WHERE username = 'testuser';