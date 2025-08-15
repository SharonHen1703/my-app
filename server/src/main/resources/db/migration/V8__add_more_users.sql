-- V8__add_more_users.sql
-- הוספת משתמשים נוספים לבדיקת מערכת ההצעות

INSERT INTO users (id, first_name, last_name, email, created_at, updated_at)
VALUES 
(3, 'קונה', 'ראשון', 'buyer1@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'קונה', 'שני', 'buyer2@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'קונה', 'שלישי', 'buyer3@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'קונה', 'רביעי', 'buyer4@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
