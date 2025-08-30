-- הפיכת המיגרציה לאידמפוטנטית כדי לא להתנגש עם V4__bids.sql
CREATE TABLE IF NOT EXISTS public.bids (
  id              BIGSERIAL PRIMARY KEY,
  auction_id      BIGINT NOT NULL REFERENCES public.auctions(id) ON DELETE CASCADE,
  bidder_user_id  BIGINT NOT NULL REFERENCES public.users(id)     ON DELETE RESTRICT,
  max_bid         NUMERIC(12,2) NOT NULL CHECK (max_bid >= 0),
  created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- אינדקסים (אם לא קיימים)
CREATE INDEX IF NOT EXISTS idx_bids_auction_id ON public.bids(auction_id);
CREATE INDEX IF NOT EXISTS idx_bids_auction_bidder ON public.bids(auction_id, bidder_user_id);
