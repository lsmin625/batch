DROP TABLE IF EXISTS user;

CREATE TABLE user (
    user_id VARCHAR(128) NOT NULL PRIMARY KEY,
    user_name VARCHAR(128),
    transaction_date TIMESTAMP,
    transaction_amount INTEGER,
    updated_date TIMESTAMP
);