SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS TICKET_SEATS;
DROP TABLE IF EXISTS TICKETS;
DROP TABLE IF EXISTS SHOW_SEATS;
DROP TABLE IF EXISTS USER_ROLES;
DROP TABLE IF EXISTS SHOWS;
DROP TABLE IF EXISTS THEATER_SEATS;
DROP TABLE IF EXISTS THEATERS;
DROP TABLE IF EXISTS MOVIES;
DROP TABLE IF EXISTS USERS;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE MOVIES (
                        id              INT AUTO_INCREMENT PRIMARY KEY,
                        movie_name      VARCHAR(255)   NOT NULL,
                        duration        INT            NOT NULL,
                        rating          DECIMAL(3, 1)  DEFAULT NULL,
                        release_date    DATE           DEFAULT NULL,
                        genre           VARCHAR(255)   NOT NULL,
                        language        VARCHAR(50)    NOT NULL,
                        description     TEXT           DEFAULT NULL,
                        movie_image     VARCHAR(255) DEFAULT NULL,
                        is_banner       BOOLEAN      DEFAULT FALSE,
                        is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
                        created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT chk_language CHECK (language IN (
                            'ENGLISH', 'HINDI', 'VIETNAMESE', 'SPANISH', 'FRENCH', 'GERMAN',
                            'MANDARIN', 'JAPANESE', 'KOREAN'
                            ))
    );

CREATE INDEX idx_movies_release_date ON MOVIES(release_date);
CREATE INDEX idx_movies_genre ON MOVIES(genre);
CREATE INDEX idx_movies_language ON MOVIES(language);

CREATE TABLE THEATERS (
                          id              INT AUTO_INCREMENT PRIMARY KEY,
                          name            VARCHAR(255) NOT NULL,
                          address         VARCHAR(500) NOT NULL,
                          city            VARCHAR(100) NOT NULL,
                          state           VARCHAR(50)  NOT NULL,
                          country         VARCHAR(100) NOT NULL,
                          postal_code     VARCHAR(20)  DEFAULT NULL,
                          phone           VARCHAR(255) DEFAULT NULL,
                          email           VARCHAR(255) DEFAULT NULL,
                          is_active       BOOLEAN      DEFAULT TRUE,
                          created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          UNIQUE KEY uq_theater_location (address, city, state, country)
);

CREATE INDEX idx_theaters_city ON THEATERS(city);

CREATE TABLE THEATER_SEATS (
                               id           INT AUTO_INCREMENT PRIMARY KEY,
                               seat_no      VARCHAR(10)  NOT NULL,
                               seat_type    VARCHAR(50)  NOT NULL,
                               theater_id   INT          NOT NULL,
                               created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               FOREIGN KEY (theater_id) REFERENCES THEATERS(id) ON DELETE CASCADE,
                               UNIQUE KEY uq_theater_seat (theater_id, seat_no),
                               CONSTRAINT chk_seat_type CHECK (seat_type IN ('STANDARD', 'PREMIUM', 'RECLINER', 'WHEELCHAIR', 'COUPLE'))
);

CREATE TABLE USERS (
                       id              INT AUTO_INCREMENT PRIMARY KEY,
                       name            VARCHAR(255) NOT NULL,
                       age             INT          DEFAULT NULL,
                       address         VARCHAR(500) DEFAULT NULL,
                       gender          VARCHAR(20)  DEFAULT NULL,
                       mobile_no       VARCHAR(20)  DEFAULT NULL,
                       email           VARCHAR(255) NOT NULL UNIQUE,
                       password        VARCHAR(255) NOT NULL,
                       is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE USER_ROLES (
                             id        INT AUTO_INCREMENT PRIMARY KEY,
                             user_id   INT          NOT NULL,
                             role      VARCHAR(50) NOT NULL,

                             FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE,
                             UNIQUE KEY uq_user_role (user_id, role),
                             CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'CUSTOMER', 'GUEST'))
);

CREATE TABLE SHOWS (
                       show_id         INT AUTO_INCREMENT PRIMARY KEY,
                       movie_id        INT          DEFAULT NULL,
                       theater_id      INT          DEFAULT NULL,
                       screen_number   INT          DEFAULT NULL,
                       show_date       DATE         DEFAULT NULL,
                       show_time       TIME         DEFAULT NULL,
                       status          VARCHAR(255) DEFAULT 'ACTIVE',
                       is_deleted      BOOLEAN      DEFAULT FALSE,
                       created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (movie_id) REFERENCES MOVIES (id),
                       FOREIGN KEY (theater_id) REFERENCES THEATERS (id),
                       UNIQUE KEY uq_show_slot (theater_id, screen_number, show_date, show_time),
                       CONSTRAINT chk_show_status CHECK (status IN ('ACTIVE', 'CANCELLED', 'POSTPONED', 'COMPLETED'))
);

CREATE INDEX idx_shows_date_time ON SHOWS(show_date, show_time);

CREATE TABLE SHOW_SEATS (
                             id                INT AUTO_INCREMENT PRIMARY KEY,
                             show_id           INT            NOT NULL,
                             theater_seat_id   INT            NOT NULL,
                             price             DECIMAL(10, 2) NOT NULL,
                             is_food_included BOOLEAN        NOT NULL DEFAULT FALSE,

                             FOREIGN KEY (show_id)          REFERENCES SHOWS(show_id) ON DELETE CASCADE,
                             FOREIGN KEY (theater_seat_id) REFERENCES THEATER_SEATS(id) ON DELETE CASCADE,
                             UNIQUE KEY uq_show_seat (show_id, theater_seat_id),
                             CONSTRAINT chk_price CHECK (price > 0)
);

CREATE TABLE TICKETS (
                         ticket_id              INT AUTO_INCREMENT PRIMARY KEY,
                         show_id                INT            NOT NULL,
                         user_id                INT            NOT NULL,
                         total_tickets_price    DECIMAL(10, 2) NOT NULL,
                         status                 VARCHAR(50)    NOT NULL DEFAULT 'CONFIRMED',
                         confirmation_number    VARCHAR(50)    NOT NULL UNIQUE,
                         booked_at              TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         rating                 INT            DEFAULT NULL,

                         FOREIGN KEY (show_id) REFERENCES SHOWS(show_id),
                         FOREIGN KEY (user_id) REFERENCES USERS(id),
                         CONSTRAINT chk_ticket_status CHECK (status IN ('CONFIRMED', 'PENDING', 'CANCELLED', 'REFUNDED', 'EXPIRED'))
);

CREATE TABLE TICKET_SEATS (
                              id           INT AUTO_INCREMENT PRIMARY KEY,
                              ticket_id    INT NOT NULL,
                              show_seat_id INT NOT NULL,

                              FOREIGN KEY (ticket_id)    REFERENCES TICKETS(ticket_id) ON DELETE CASCADE,
                              FOREIGN KEY (show_seat_id) REFERENCES SHOW_SEATS(id) ON DELETE CASCADE,
                              UNIQUE KEY uq_show_seat_booked (show_seat_id)
);

DROP VIEW IF EXISTS AVAILABLE_SHOW_SEATS;
DROP VIEW IF EXISTS SHOW_SEAT_STATUS;

CREATE VIEW AVAILABLE_SHOW_SEATS AS
SELECT ss.*, ts.seat_no, ts.seat_type
FROM SHOW_SEATS ss
         JOIN THEATER_SEATS ts ON ss.theater_seat_id = ts.id
WHERE NOT EXISTS (SELECT 1 FROM TICKET_SEATS tks WHERE tks.show_seat_id = ss.id);

CREATE VIEW SHOW_SEAT_STATUS AS
SELECT
    ss.id AS show_seat_id, ss.show_id, ts.seat_no, ss.price,
    CASE WHEN tks.id IS NOT NULL THEN 'BOOKED' ELSE 'AVAILABLE' END AS seat_status
FROM SHOW_SEATS ss
         JOIN THEATER_SEATS ts ON ss.theater_seat_id = ts.id
          LEFT JOIN TICKET_SEATS tks ON ss.id = tks.show_seat_id;

CREATE TABLE IF NOT EXISTS PASSWORD_RESET_TOKENS (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    token           VARCHAR(6)      NOT NULL,
    user_id         INT             NOT NULL,
    expiry_date     DATETIME        NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE
);