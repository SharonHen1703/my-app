-- V5__bid_history_snapshots.sql — היסטוריית “מה הוצג בפועל”

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'bid_history_kind') THEN
    CREATE TYPE bid_history_kind AS ENUM ('USER_BID','AUTO_RAISE','TIE_AUTO');
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS public.bid_history_snapshots (
  id              BIGSERIAL PRIMARY KEY,
  auction_id      BIGINT NOT NULL REFERENCES public.auctions(id)      ON DELETE CASCADE,
  bid_id          BIGINT     REFERENCES public.bids(id)               ON DELETE CASCADE,
  actor_user_id   BIGINT NOT NULL REFERENCES public.users(id)         ON DELETE RESTRICT,
  displayed_bid   NUMERIC(12,2) NOT NULL CHECK (displayed_bid >= 0),
  kind            bid_history_kind NOT NULL,
  snapshot_time   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_bhs_auction_time ON public.bid_history_snapshots(auction_id, snapshot_time DESC);
