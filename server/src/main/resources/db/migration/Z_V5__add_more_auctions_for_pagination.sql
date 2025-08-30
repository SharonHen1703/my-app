-- Add more auctions for pagination testing
INSERT INTO auctions (
    title, description, condition, categories, image_urls,
    min_price, buy_now_price, bid_increment, current_bid_amount, highest_max_bid,
    highest_user_id, bids_count, start_date, end_date, seller_id, status,
    created_at, updated_at
) VALUES
-- Auctions 7-18 (12 additional auctions)
('מכונת כביסה בוש 8 ק"ג', 'מכונת כביסה בוש 8 ק"ג, בשימוש שנתיים, במצב מעולה', 'like_new', '["מוצרי חשמל", "בית ומשק"]', '["https://picsum.photos/400/300?random=7"]', 800.00, 1200.00, 50.00, 850.00, NULL, NULL, 3, NOW() - INTERVAL '1 days', NOW() + INTERVAL '4 days', 2, 'active', NOW(), NOW()),

('מחשב נייד לנובו', 'מחשב נייד לנובו ThinkPad, Intel i7, 16GB RAM, SSD 512GB', 'used', '["מחשבים", "אלקטרוניקה"]', '["https://picsum.photos/400/300?random=8"]', 1500.00, 2200.00, 100.00, 1600.00, NULL, NULL, 5, NOW() - INTERVAL '2 days', NOW() + INTERVAL '3 days', 1, 'active', NOW(), NOW()),

('שולחן עבודה עץ מלא', 'שולחן עבודה מעץ מלא, ברוחב 1.5 מטר, במצב חדש', 'new', '["רהיטים", "משרד"]', '["https://picsum.photos/400/300?random=9"]', 400.00, 700.00, 25.00, 425.00, NULL, NULL, 2, NOW() - INTERVAL '3 hours', NOW() + INTERVAL '5 days', 1, 'active', NOW(), NOW()),

('אופניים חשמליים', 'אופניים חשמליים עם טווח של 50 ק"מ, משופצים לאחרונה', 'refurbished', '["ספורט", "תחבורה"]', '["https://picsum.photos/400/300?random=10"]', 2000.00, 3500.00, 150.00, NULL, NULL, NULL, 0, NOW() - INTERVAL '1 hours', NOW() + INTERVAL '6 days', 2, 'active', NOW(), NOW()),

('מיקסר קיטצן איד', 'מיקסר קיטצן איד מקצועי, בצבע אדום, כולל אביזרים', 'like_new', '["מטבח", "מוצרי חשמל"]', '["https://picsum.photos/400/300?random=11"]', 800.00, 1100.00, 50.00, 850.00, NULL, NULL, 4, NOW() - INTERVAL '6 hours', NOW() + INTERVAL '2 days', 2, 'active', NOW(), NOW()),

('ספה תלת מושבית', 'ספה תלת מושבית בצבע אפור, נוחה ומעוצבת', 'used', '["רהיטים", "סלון"]', '["https://picsum.photos/400/300?random=12"]', 600.00, 900.00, 40.00, 640.00, NULL, NULL, 1, NOW() - INTERVAL '2 hours', NOW() + INTERVAL '7 days', 1, 'active', NOW(), NOW()),

('מצלמה קנון DSLR', 'מצלמה קנון EOS 90D עם עדשה 18-135mm, במצב מצוין', 'like_new', '["צילום", "אלקטרוניקה"]', '["https://picsum.photos/400/300?random=13"]', 2500.00, 3800.00, 200.00, NULL, NULL, NULL, 0, NOW() - INTERVAL '4 hours', NOW() + INTERVAL '8 days', 1, 'active', NOW(), NOW()),

('מקרר סמסונג 2 דלתות', 'מקרר סמסונג 500 ליטר, צבע נירוסטה, חסכוני באנרגיה', 'used', '["מוצרי חשמל", "מטבח"]', '["https://picsum.photos/400/300?random=14"]', 1200.00, 1800.00, 80.00, 1280.00, NULL, NULL, 6, NOW() - INTERVAL '12 hours', NOW() + INTERVAL '1 days', 2, 'active', NOW(), NOW()),

('כסא גיימינג', 'כסא גיימינג ארגונומי עם תמיכה מלאה, בצבע שחור-אדום', 'new', '["רהיטים", "גיימינג"]', '["https://picsum.photos/400/300?random=15"]', 500.00, 750.00, 30.00, 530.00, NULL, NULL, 3, NOW() - INTERVAL '8 hours', NOW() + INTERVAL '4 days', 2, 'active', NOW(), NOW()),

('טלוויזיה 55 אינץ', 'טלוויזיה חכמה 55 אינץ 4K, מבית LG, במצב מעולה', 'like_new', '["אלקטרוניקה", "בידור"]', '["https://picsum.photos/400/300?random=16"]', 1800.00, 2500.00, 120.00, 1920.00, NULL, NULL, 7, NOW() - INTERVAL '5 hours', NOW() + INTERVAL '3 days', 1, 'active', NOW(), NOW()),

('אוזניות בוס', 'אוזניות אלחוטיות בוס עם ביטול רעשים פעיל', 'used', '["אלקטרוניקה", "אודיו"]', '["https://picsum.photos/400/300?random=17"]', 300.00, 450.00, 20.00, 320.00, NULL, NULL, 2, NOW() - INTERVAL '3 hours', NOW() + INTERVAL '6 days', 1, 'active', NOW(), NOW()),

('שואב אבק רובוטי', 'שואב אבק רובוטי עם מיפוי חכם וחזרה אוטומטית לעמדת הטעינה', 'refurbished', '["מוצרי חשמל", "ניקיון"]', '["https://picsum.photos/400/300?random=18"]', 600.00, 900.00, 40.00, NULL, NULL, NULL, 0, NOW() - INTERVAL '1 hours', NOW() + INTERVAL '5 days', 2, 'active', NOW(), NOW());
