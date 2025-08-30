-- Align bid_history_snapshots schema with extended fields used by the app
-- Safe, idempotent migration: adds missing columns, backfills, and enforces constraints.

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'bid_history_kind') THEN
    CREATE TYPE bid_history_kind AS ENUM ('USER_BID','AUTO_RAISE','TIE_AUTO');
  END IF;
END$$;

-- Add missing columns if the table came from the older V10 definition
ALTER TABLE public.bid_history_snapshots
  ADD COLUMN IF NOT EXISTS auction_id    BIGINT,
  ADD COLUMN IF NOT EXISTS actor_user_id BIGINT,
  ADD COLUMN IF NOT EXISTS kind          bid_history_kind;

-- Backfill auction_id and actor_user_id from bids when available
UPDATE public.bid_history_snapshots s
SET auction_id = b.auction_id
FROM public.bids b
WHERE s.auction_id IS NULL AND s.bid_id = b.id;

-- Backfill actor_user_id using whichever bidder column exists on bids
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'bids' AND column_name = 'bidder_user_id'
  ) THEN
    EXECUTE $b1$
      UPDATE public.bid_history_snapshots s
      SET actor_user_id = b.bidder_user_id
      FROM public.bids b
      WHERE s.actor_user_id IS NULL AND s.bid_id = b.id
    $b1$;
  ELSIF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'bids' AND column_name = 'bidder_id'
  ) THEN
    EXECUTE $b2$
      UPDATE public.bid_history_snapshots s
      SET actor_user_id = b.bidder_id
      FROM public.bids b
      WHERE s.actor_user_id IS NULL AND s.bid_id = b.id
    $b2$;
  ELSIF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'bids' AND column_name = 'user_id'
  ) THEN
    EXECUTE $b3$
      UPDATE public.bid_history_snapshots s
      SET actor_user_id = b.user_id
      FROM public.bids b
      WHERE s.actor_user_id IS NULL AND s.bid_id = b.id
    $b3$;
  END IF;
END$$;

-- Default missing kinds to USER_BID
UPDATE public.bid_history_snapshots s
SET kind = 'USER_BID'
WHERE s.kind IS NULL;

-- Enforce NOT NULL now that values are present
ALTER TABLE public.bid_history_snapshots
  ALTER COLUMN auction_id SET NOT NULL,
  ALTER COLUMN actor_user_id SET NOT NULL,
  ALTER COLUMN kind SET NOT NULL;

-- Add foreign keys if not present (guarded by constraint name checks)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_bhs_auction_id'
  ) THEN
    ALTER TABLE public.bid_history_snapshots
      ADD CONSTRAINT fk_bhs_auction_id
      FOREIGN KEY (auction_id) REFERENCES public.auctions(id) ON DELETE CASCADE;
  END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_bhs_actor_user_id'
  ) THEN
    ALTER TABLE public.bid_history_snapshots
      ADD CONSTRAINT fk_bhs_actor_user_id
      FOREIGN KEY (actor_user_id) REFERENCES public.users(id) ON DELETE RESTRICT;
  END IF;
END$$;

-- Helpful index for history lookups
CREATE INDEX IF NOT EXISTS idx_bhs_auction_time
  ON public.bid_history_snapshots(auction_id, snapshot_time DESC);
