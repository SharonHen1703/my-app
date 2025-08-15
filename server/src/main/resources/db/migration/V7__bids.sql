-- היסטוריית הצעות
CREATE TABLE public.bids (
  id          BIGSERIAL PRIMARY KEY,
  auction_id  BIGINT NOT NULL REFERENCES public.auctions(id) ON DELETE CASCADE,
  bidder_id   BIGINT NOT NULL REFERENCES public.users(id)     ON DELETE RESTRICT,
  max_bid     NUMERIC(12,2) NOT NULL CHECK (max_bid > 0),
  created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bids_auction_id ON public.bids(auction_id);
CREATE INDEX idx_bids_auction_bidder ON public.bids(auction_id, bidder_id);

-- אופציונלי: לאכוף שלאותו משתמש לא תהיה שורה מיותרת אם נשמור רק את המקסימום שלו.
-- במימוש הנוכחי אנחנו מאפשרים כמה שורות לאותו משתמש, אבל תמיד מחשבים את המקסימום שלו.
