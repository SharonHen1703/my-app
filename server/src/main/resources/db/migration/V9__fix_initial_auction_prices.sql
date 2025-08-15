-- V9__fix_initial_auction_prices.sql
-- תיקון מחירים ראשוניים - כל מכירה צריכה להתחיל עם current_bid_amount = min_price

UPDATE auctions 
SET current_bid_amount = min_price,
    bids_count = 0,
    highest_user_id = NULL,
    highest_max_bid = NULL
WHERE current_bid_amount != min_price;
