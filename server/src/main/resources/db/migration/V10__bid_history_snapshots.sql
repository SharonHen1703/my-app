CREATE TABLE IF NOT EXISTS public.bid_history_snapshots (
  id             BIGSERIAL PRIMARY KEY,
  bid_id         BIGINT NOT NULL REFERENCES public.bids(id) ON DELETE CASCADE,
  displayed_bid  NUMERIC(12,2) NOT NULL CHECK (displayed_bid >= 0),
  snapshot_time  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_bhs_bid_id ON public.bid_history_snapshots(bid_id);
CREATE INDEX IF NOT EXISTS idx_bhs_snapshot_time ON public.bid_history_snapshots(snapshot_time);
