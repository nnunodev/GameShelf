DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE username = 'testuser');
DELETE FROM users WHERE username = 'testuser';

-- Password: 'Testpassword123' - encoded with strength 10
INSERT INTO users (username, email, password) 
VALUES ('testuser', 'test@example.com', '$2a$10$lvPJRCQLJRhLHyXqB3IkB.S5bVwpZvLmAEb5jGmT5yFWVROtDSzIe');

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER'
FROM users 
WHERE username = 'testuser';