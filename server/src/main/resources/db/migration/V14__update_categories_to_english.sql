-- Update categories to use standardized English codes
-- V14__update_categories_to_english.sql

-- Update existing categories to use English codes
UPDATE auctions SET categories = 
    CASE 
        -- Electronics mapping
        WHEN categories::text LIKE '%electronics%' OR categories::text LIKE '%אלקטרוניקה%' OR categories::text ~ '.*ε.*σ.*÷.*°.*Θ.*' THEN 
            CASE 
                WHEN categories::text LIKE '%audio%' OR categories::text LIKE '%אודיו%' THEN '["electronics"]'
                WHEN categories::text LIKE '%computers%' OR categories::text LIKE '%מחשבים%' THEN '["electronics"]'
                WHEN categories::text LIKE '%tablets%' OR categories::text LIKE '%טאבלטים%' THEN '["electronics"]'
                WHEN categories::text LIKE '%tv%' OR categories::text LIKE '%טלוויזיה%' THEN '["electronics"]'
                WHEN categories::text LIKE '%smartphones%' OR categories::text LIKE '%סמארטפונים%' THEN '["electronics"]'
                WHEN categories::text LIKE '%wearables%' OR categories::text LIKE '%לבישים%' THEN '["electronics"]'
                WHEN categories::text LIKE '%gaming%' OR categories::text LIKE '%גיימינג%' THEN '["electronics", "gaming"]'
                ELSE '["electronics"]'
            END
        -- Appliances mapping
        WHEN categories::text LIKE '%appliances%' OR categories::text LIKE '%מכשירי חשמל%' OR categories::text ~ '.*α.*λ.*≈.*Φ.*°.*σ.*≡.*Θ.*≈.*Σ.*' THEN
            CASE 
                WHEN categories::text LIKE '%kitchen%' OR categories::text LIKE '%מטבח%' THEN '["appliances", "kitchen"]'
                WHEN categories::text LIKE '%refrigerators%' OR categories::text LIKE '%מקרר%' THEN '["appliances"]'
                WHEN categories::text LIKE '%cooling%' OR categories::text LIKE '%קירור%' THEN '["appliances"]'
                WHEN categories::text LIKE '%laundry%' OR categories::text LIKE '%כביסה%' THEN '["appliances"]'
                ELSE '["appliances"]'
            END
        -- Furniture mapping  
        WHEN categories::text LIKE '%furniture%' OR categories::text LIKE '%רהיטים%' OR categories::text ~ '.*°.*Σ.*Θ.*Φ.*Θ.*φ.*' THEN
            CASE 
                WHEN categories::text LIKE '%bedroom%' OR categories::text LIKE '%חדר שינה%' THEN '["furniture"]'
                WHEN categories::text LIKE '%living_room%' OR categories::text LIKE '%סלון%' THEN '["furniture"]'
                WHEN categories::text LIKE '%office%' OR categories::text LIKE '%משרד%' THEN '["furniture"]'
                WHEN categories::text LIKE '%gaming%' OR categories::text LIKE '%גיימינג%' THEN '["furniture", "gaming"]'
                ELSE '["furniture"]'
            END
        -- Sports mapping
        WHEN categories::text LIKE '%sports%' OR categories::text LIKE '%ספורט%' OR categories::text ~ '.*ε.*π.*σ.*°.*Φ.*' THEN
            CASE 
                WHEN categories::text LIKE '%bicycles%' OR categories::text LIKE '%אופניים%' THEN '["sports"]'
                WHEN categories::text LIKE '%electric_bikes%' OR categories::text LIKE '%אופניים חשמליים%' THEN '["sports"]'
                ELSE '["sports"]'
            END
        -- Vehicles mapping
        WHEN categories::text LIKE '%vehicles%' OR categories::text LIKE '%כלי רכב%' THEN
            CASE 
                WHEN categories::text LIKE '%cars%' OR categories::text LIKE '%מכוניות%' THEN '["vehicles"]'
                ELSE '["vehicles"]'
            END
        -- Music mapping
        WHEN categories::text LIKE '%music%' OR categories::text LIKE '%מוזיקה%' THEN
            CASE 
                WHEN categories::text LIKE '%instruments%' OR categories::text LIKE '%כלי נגינה%' THEN '["music"]'
                ELSE '["music"]'
            END
        -- Gaming mapping
        WHEN categories::text LIKE '%gaming%' OR categories::text LIKE '%גיימינג%' THEN
            CASE 
                WHEN categories::text LIKE '%consoles%' OR categories::text LIKE '%קונסולות%' THEN '["gaming"]'
                ELSE '["gaming"]'
            END
        -- Photography mapping
        WHEN categories::text LIKE '%photography%' OR categories::text LIKE '%צילום%' THEN
            CASE 
                WHEN categories::text LIKE '%cameras%' OR categories::text LIKE '%מצלמות%' THEN '["photography"]'
                ELSE '["photography"]'
            END
        -- Crafts mapping
        WHEN categories::text LIKE '%crafts%' OR categories::text LIKE '%יצירה%' THEN
            CASE 
                WHEN categories::text LIKE '%sewing%' OR categories::text LIKE '%תפירה%' THEN '["crafts"]'
                ELSE '["crafts"]'
            END
        -- Kitchen mapping (standalone)
        WHEN categories::text LIKE '%kitchen%' OR categories::text LIKE '%מטבח%' THEN '["kitchen"]'
        -- Default fallback for unknown categories
        ELSE '["collectibles"]'
    END
WHERE categories IS NOT NULL AND categories != '' AND categories != '[]';

-- Add comment explaining the standardization
COMMENT ON COLUMN auctions.categories IS 'Standardized category codes in English. Valid values are defined in AuctionCategory enum.';
