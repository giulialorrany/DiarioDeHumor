CREATE DATABASE diario_de_humor;
USE diario_de_humor;

CREATE TABLE humor_dia (
	day_date DATE PRIMARY KEY,
    mood CHAR(9) NOT NULL,
    note TEXT
);