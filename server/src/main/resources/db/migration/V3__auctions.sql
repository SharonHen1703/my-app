-- V3__auctions.sql

CREATE TABLE public.auctions (
  id                BIGSERIAL PRIMARY KEY,
  title             TEXT        NOT NULL,
  description       TEXT        NOT NULL,
  condition         TEXT        NOT NULL
                   CHECK (condition IN ('new','like_new','used','refurbished','damaged_or_parts')),

  categories        JSONB       NOT NULL DEFAULT '[]'::jsonb,
  image_urls        JSONB       NOT NULL DEFAULT '[]'::jsonb,

  min_price         NUMERIC(12,2) NOT NULL CHECK (min_price >= 0),
  buy_now_price     NUMERIC(12,2)      NULL CHECK (buy_now_price IS NULL OR buy_now_price >= min_price),

  bid_increment     NUMERIC(12,2) NOT NULL CHECK (bid_increment > 0),

  current_bid_amount NUMERIC(12,2) NULL,
  highest_max_bid    NUMERIC(12,2) NULL,
  highest_user_id    BIGINT        NULL REFERENCES public.users(id) ON DELETE SET NULL,
  bids_count         INTEGER       NOT NULL DEFAULT 0 CHECK (bids_count >= 0),

  start_date        TIMESTAMPTZ NOT NULL,
  end_date          TIMESTAMPTZ NOT NULL CHECK (end_date > start_date),

  seller_id         BIGINT      NOT NULL REFERENCES public.users(id) ON DELETE RESTRICT,
  status            TEXT        NOT NULL CHECK (status IN ('active','sold','unsold')),

  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),

  CHECK (current_bid_amount IS NULL OR current_bid_amount >= min_price),
  CHECK (highest_max_bid  IS NULL OR current_bid_amount IS NULL OR current_bid_amount <= highest_max_bid),
  CHECK ((highest_user_id IS NULL) = (highest_max_bid IS NULL))
);

CREATE OR REPLACE FUNCTION public.auctions_set_current_bid_on_insert()
RETURNS trigger AS $$
BEGIN
  IF NEW.current_bid_amount IS NULL THEN
    NEW.current_bid_amount := NEW.min_price;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auctions_init_current_bid
BEFORE INSERT ON public.auctions
FOR EACH ROW EXECUTE FUNCTION public.auctions_set_current_bid_on_insert();

CREATE TRIGGER trg_auctions_touch_updated_at
BEFORE UPDATE ON public.auctions
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX idx_auctions_active_ends_at
  ON public.auctions (end_date)
  WHERE status = 'active';

CREATE INDEX idx_auctions_seller_id
  ON public.auctions (seller_id);

CREATE INDEX idx_auctions_categories_gin
  ON public.auctions USING GIN (categories);
