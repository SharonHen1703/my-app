-- V4__sample_auctions.sql (H2 compatible)
-- הוספת נתוני דמה עם מכרזים הכוללים כמה תמונות

-- הוספת משתמשים דמה למוכרים
INSERT INTO users (id, first_name, last_name, email, created_at, updated_at)
VALUES 
(1, 'מוכר', 'ראשון', 'seller1@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'מוכר', 'שני', 'seller2@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- הוספת מכרזים דמה עם תמונות מרובות
INSERT INTO auctions (
    title,
    description,
    condition,
    categories,
    image_urls,
    min_price,
    buy_now_price,
    bid_increment,
    current_bid_amount,
    start_date,
    end_date,
    seller_id,
    status,
    created_at,
    updated_at
) VALUES 
(
    'מחשב נייד Dell XPS 13',
    'מחשב נייד מעולה במצב כמו חדש, מסך 13 אינץ, 16GB RAM, 512GB SSD',
    'like_new',
    '["electronics", "computers"]',
    '["/images/placeholder1.jpg", "/images/placeholder2.jpg", "/images/placeholder3.jpg"]',
    2500.00,
    4000.00,
    50.00,
    2500.00,
    DATEADD('day', -1, CURRENT_TIMESTAMP),
    DATEADD('day', 5, CURRENT_TIMESTAMP),
    1,
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'אופניים הרים Trek',
    'אופני הרים מקצועיים במצב מצוין, מתאים לשטח ולעיר',
    'used',
    '["sports", "bicycles"]',
    '["/images/placeholder2.jpg", "/images/placeholder4.jpg"]',
    800.00,
    1200.00,
    25.00,
    800.00,
    DATEADD('hour', -2, CURRENT_TIMESTAMP),
    DATEADD('day', 3, CURRENT_TIMESTAMP),
    1,
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'שעון חכם Apple Watch',
    'שעון חכם Apple Watch Series 8, 45mm, GPS + Cellular',
    'like_new',
    '["electronics", "wearables"]',
    '["/images/placeholder3.jpg"]',
    1800.00,
    2500.00,
    30.00,
    1800.00,
    DATEADD('minute', -30, CURRENT_TIMESTAMP),
    DATEADD('day', 7, CURRENT_TIMESTAMP),
    2,
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'גיטרה חשמלית Fender',
    'גיטרה חשמלית Fender Stratocaster במצב מצוין, כולל מגבר',
    'used',
    '["music", "instruments"]',
    '["/images/placeholder1.jpg"]',
    1500.00,
    2200.00,
    40.00,
    1540.00,
    DATEADD('hour', -1, CURRENT_TIMESTAMP),
    DATEADD('day', 4, CURRENT_TIMESTAMP),
    2,
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'מכונת קפה דלונגי',
    'מכונת קפה אוטומטית דלונגי, מטחנת קפה מובנית',
    'new',
    '["kitchen", "appliances"]',
    '["/images/placeholder5.jpg"]',
    1200.00,
    1800.00,
    30.00,
    1200.00,
    DATEADD('hour', -4, CURRENT_TIMESTAMP),
    DATEADD('day', 6, CURRENT_TIMESTAMP),
    1,
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'טלוויזיה 65 אינץ Samsung',
    'טלוויזיה QLED 65 אינץ 4K HDR, מודל 2023',
    'like_new',
    '["electronics", "tv"]',
    '["/images/placeholder4.jpg"]',
    3000.00,
    4500.00,
    100.00,
    3000.00,
    DATEADD('hour', -6, CURRENT_TIMESTAMP),
    DATEADD('day', 2, CURRENT_TIMESTAMP),
    2,
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
