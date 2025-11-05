CREATE SCHEMA IF NOT EXISTS book_recommendation_system;

SET search_path TO book_recommendation_system;

-- Drop old tables
DROP TABLE IF EXISTS bookmarks CASCADE;
DROP TABLE IF EXISTS favorites CASCADE;
DROP TABLE IF EXISTS reading_history CASCADE;
DROP TABLE IF EXISTS ratings CASCADE;
DROP TABLE IF EXISTS book_genres CASCADE;
DROP TABLE IF EXISTS book_authors CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS authors CASCADE;
DROP TABLE IF EXISTS book_formats CASCADE;
DROP TABLE IF EXISTS book_types CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- User roles
CREATE TABLE roles
(
    role_id   BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(20) UNIQUE NOT NULL
);

-- Users
CREATE TABLE users
(
    user_id                         BIGSERIAL PRIMARY KEY,
    role_id                         BIGINT              NOT NULL,
    username                        VARCHAR(50) UNIQUE  NOT NULL,
    password                        VARCHAR(255)        NOT NULL,
    email                           VARCHAR(100) UNIQUE NOT NULL,
    phone_number                    VARCHAR(15) UNIQUE,
    avatar_url                      VARCHAR(512),
    is_activate                     BOOLEAN             NOT NULL DEFAULT false,
    email_verification_token        VARCHAR(255),
    email_verification_token_expiry TIMESTAMP,
    reset_password_token            VARCHAR(255),
    reset_password_token_expiry     TIMESTAMP,
    full_name                       VARCHAR(100),
    is_ban                          BOOLEAN             NOT NULL DEFAULT false,
    created_at                      TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMP
);

-- Books
CREATE TABLE books
(
    book_id          BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    description      TEXT         NOT NULL,
    cover_image_url  VARCHAR(255) NOT NULL,
    publication_year INT,
    publisher        VARCHAR(100),
    is_deleted       BOOLEAN      NOT NULL DEFAULT false,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP
);

-- Book type lookup (ebook, audiobook, etc.)
CREATE TABLE book_types
(
    type_id   BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(30) UNIQUE NOT NULL
);

-- Stored formats per book
CREATE TABLE book_formats
(
    format_id    BIGSERIAL PRIMARY KEY,
    book_id      BIGINT       NOT NULL,
    type_id      BIGINT       NOT NULL,
    content_url  VARCHAR(255) NOT NULL,
    total_pages  INT,
    file_size_kb INT,
    UNIQUE (book_id, type_id)
);

-- Authors master data
CREATE TABLE authors
(
    author_id   BIGSERIAL PRIMARY KEY,
    author_name VARCHAR(100) NOT NULL,
    biography   TEXT
);

-- Genre master data
CREATE TABLE genres
(
    genre_id    BIGSERIAL PRIMARY KEY,
    genre_name  VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- Book-author link
CREATE TABLE book_authors
(
    book_id   BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, author_id)
);

-- Book-genre link
CREATE TABLE book_genres
(
    book_id  BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, genre_id)
);

-- Book ratings
CREATE TABLE ratings
(
    rating_id    BIGSERIAL PRIMARY KEY,
    user_id      BIGINT    NOT NULL,
    book_id      BIGINT    NOT NULL,
    rating_value INT       NOT NULL CHECK (rating_value BETWEEN 1 AND 5),
    comment      TEXT,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, book_id)
);

-- Reading progress
CREATE TABLE reading_history
(
    history_id   BIGSERIAL PRIMARY KEY,
    user_id      BIGINT    NOT NULL,
    book_id      BIGINT    NOT NULL,
    last_read_at TIMESTAMP NOT NULL DEFAULT now(),
    progress     DOUBLE PRECISION,
    UNIQUE (user_id, book_id)
);

-- Favorite books
CREATE TABLE favorites
(
    favorite_id BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL,
    book_id     BIGINT    NOT NULL,
    added_at    TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, book_id)
);

-- Bookmarks
CREATE TABLE bookmarks
(
    bookmark_id      BIGSERIAL PRIMARY KEY,
    user_id          BIGINT    NOT NULL,
    book_id          BIGINT    NOT NULL,
    page_number      INT,
    location_in_book VARCHAR(255),
    note             VARCHAR(255),
    created_at       TIMESTAMP NOT NULL DEFAULT now()
);

-- Foreign keys
ALTER TABLE users
    ADD CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles (role_id);

ALTER TABLE book_formats
    ADD CONSTRAINT fk_book_formats_book
        FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE;

ALTER TABLE book_formats
    ADD CONSTRAINT fk_book_formats_type
        FOREIGN KEY (type_id) REFERENCES book_types (type_id);

ALTER TABLE book_authors
    ADD CONSTRAINT fk_book_authors_book
        FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE;

ALTER TABLE book_authors
    ADD CONSTRAINT fk_book_authors_author
        FOREIGN KEY (author_id) REFERENCES authors (author_id) ON DELETE CASCADE;

ALTER TABLE book_genres
    ADD CONSTRAINT fk_book_genres_book
        FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE;

ALTER TABLE book_genres
    ADD CONSTRAINT fk_book_genres_genre
        FOREIGN KEY (genre_id) REFERENCES genres (genre_id) ON DELETE CASCADE;

ALTER TABLE ratings
    ADD CONSTRAINT fk_ratings_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE ratings
    ADD CONSTRAINT fk_ratings_book
        FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE;

ALTER TABLE reading_history
    ADD CONSTRAINT fk_reading_history_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE reading_history
    ADD CONSTRAINT fk_reading_history_book
        FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE;

ALTER TABLE favorites
    ADD CONSTRAINT fk_favorites_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE favorites
    ADD CONSTRAINT fk_favorites_book
        FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE;

ALTER TABLE bookmarks
    ADD CONSTRAINT fk_bookmarks_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE bookmarks
    ADD CONSTRAINT fk_bookmarks_book
        FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE;

-- Seed data
INSERT INTO roles (role_name)
VALUES ('ADMIN'),
       ('USER');
INSERT INTO book_types (type_name)
VALUES ('PDF'),
       ('EPUB');

INSERT INTO genres (genre_name, description)
VALUES ('Chưa phân loại', 'Thể loại sách chưa phân loại');
INSERT INTO genres (genre_name, description)
VALUES ('Công nghệ thông tin', 'Thể loại sách công nghệ thông tin');
INSERT INTO genres (genre_name, description)
VALUES ('Khoa học', 'Thể loại sách khoa học');
INSERT INTO genres (genre_name, description)
VALUES ('Kinh dị', 'Thể loại sách kinh dị');
INSERT INTO genres (genre_name, description)
VALUES ('Kỹ năng sống', 'Thể loại sách kỹ năng sống');
INSERT INTO genres (genre_name, description)
VALUES ('Lịch sử', 'Thể loại sách lịch sử');
INSERT INTO genres (genre_name, description)
VALUES ('Thiếu nhi', 'Thể loại sách thiếu nhi');
INSERT INTO genres (genre_name, description)
VALUES ('Tiểu thuyết', 'Thể loại sách tiểu thuyết');
INSERT INTO genres (genre_name, description)
VALUES ('Trinh thám', 'Thể loại sách trinh thám');
INSERT INTO genres (genre_name, description)
VALUES ('Tài chính', 'Thể loại sách tài chính');
INSERT INTO genres (genre_name, description)
VALUES ('Tâm Lý', 'Thể loại sách tâm lý');
INSERT INTO genres (genre_name, description)
VALUES ('Tâm linh', 'Thể loại sách tâm linh');

-- Seed 20 test accounts
INSERT INTO users (role_id, username, password, email, is_activate, full_name, created_at)
VALUES (1, 'adminPRVL', '$2a$14$vaEn.tgctyvKU6jYR.cEmO1d6dO7slXnVmWF0H6HzFUIfT6sFqJju', 'admin@gmail.com', true,
        'Account 1', now());
