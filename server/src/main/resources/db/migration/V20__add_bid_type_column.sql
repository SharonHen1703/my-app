-- Add bid type column to distinguish between manual and automatic bids
-- This column indicates whether the bid was placed manually by user or automatically by the system

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'bid_type') THEN
    CREATE TYPE bid_type AS ENUM ('ידני', 'אוטומטי');
  END IF;
END$$;

-- Add the new column
ALTER TABLE public.bid_history_snapshots
  ADD COLUMN IF NOT EXISTS bid_type bid_type;

-- Set default values based on existing kind values
-- USER_BID = ידני (manual), AUTO_RAISE and TIE_AUTO = אוטומטי (automatic)
UPDATE public.bid_history_snapshots 
SET bid_type = CASE 
  WHEN kind = 'USER_BID' THEN 'ידני'::bid_type
  WHEN kind IN ('AUTO_RAISE', 'TIE_AUTO') THEN 'אוטומטי'::bid_type
  ELSE 'ידני'::bid_type  -- Default fallback
END
WHERE bid_type IS NULL;

-- Make the column NOT NULL now that all values are set
ALTER TABLE public.bid_history_snapshots
  ALTER COLUMN bid_type SET NOT NULL;

-- Set default for future inserts
ALTER TABLE public.bid_history_snapshots
  ALTER COLUMN bid_type SET DEFAULT 'ידני'::bid_type;
