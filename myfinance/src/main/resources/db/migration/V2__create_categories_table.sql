CREATE TABLE categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    user_id BIGINT,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id)
);
