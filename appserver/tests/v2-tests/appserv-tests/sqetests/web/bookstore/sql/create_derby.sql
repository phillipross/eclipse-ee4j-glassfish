DROP TABLE books;

CREATE TABLE books (
    ID VARCHAR(8),
    SURNAME VARCHAR(24),
    FIRST_NAME VARCHAR(24),
    TITLE VARCHAR(96),
    PRICE FLOAT,
    YEAR1         INTEGER        NOT NULL,
        DESCRIPTION VARCHAR(30),
    INVENTORY INTEGER NOT NULL
);

