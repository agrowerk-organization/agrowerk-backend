CREATE TABLE users (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,

       name VARCHAR(255) NOT NULL,
       email VARCHAR(255) NOT NULL UNIQUE,
       password VARCHAR(255) NOT NULL,

       telephone VARCHAR(15) UNIQUE,
       cpf VARCHAR(14) UNIQUE,

       role_id BIGINT NOT NULL,

       created_at DATETIME NOT NULL,
       updated_at DATETIME NOT NULL,
       last_login DATETIME NOT NULL,

       is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

       CONSTRAINT fk_users_role
           FOREIGN KEY (role_id) REFERENCES roles(id)
);
