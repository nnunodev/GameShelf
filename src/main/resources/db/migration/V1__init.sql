CREATE TABLE
    games (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        title VARCHAR(255) NOT NULL UNIQUE,
        genre VARCHAR(255) NOT NULL,
        platform VARCHAR(255) NOT NULL,
        rating DOUBLE,
        release_date DATE,
        notes VARCHAR(1000)
    );

CREATE TABLE
    users (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(255) NOT NULL UNIQUE,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL
    );

CREATE TABLE
    user_roles (
        user_id BIGINT NOT NULL,
        role VARCHAR(255) NOT NULL,
        PRIMARY KEY (user_id, role),
        CONSTRAINT fk_user_roles FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
    );