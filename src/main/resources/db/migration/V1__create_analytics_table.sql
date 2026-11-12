CREATE TABLE analytics (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    amount NUMERIC(19, 2) NOT NULL
);
