# Users schema

# --- !Ups

CREATE TABLE User (
    Level int(11) NOT NULL,
    Code int(11) NOT NULL,
    PRIMARY KEY (Level, Code)
);

# --- !Downs

DROP TABLE User;