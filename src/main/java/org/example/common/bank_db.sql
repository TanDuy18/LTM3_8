CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       sdt varchar(20) UNIQUE,
                       password VARCHAR(100),
                       balance DOUBLE DEFAULT 0,
                       is_logged_in BOOLEAN DEFAULT FALSE
);
