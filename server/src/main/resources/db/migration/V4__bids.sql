-- V4__bids.sql — טבלת ההצעות הפעילות (Max Bid לכל משתמש/מכרז)
-- שורה אחת לכל משתמש במכרז (ה־Max Bid האחרון שלו)

CREATE TABLE IF NOT EXISTS public.bids (
  id              BIGSERIAL PRIMARY KEY,
  auction_id      BIGINT NOT NULL REFERENCES public.auctions(id) ON DELETE CASCADE,
  bidder_user_id  BIGINT NOT NULL REFERENCES public.users(id)    ON DELETE RESTRICT,
  max_bid         NUMERIC(12,2) NOT NULL CHECK (max_bid >= 0),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_auction_bidder UNIQUE (auction_id, bidder_user_id)
);

-- עדכון updated_at אוטומטי (מניחים שהפונקציה set_updated_at() קיימת)
CREATE TRIGGER trg_bids_touch_updated_at
BEFORE UPDATE ON public.bids
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_bids_auction_id ON public.bids(auction_id);
