import { useState, useEffect } from "react";
import type { AuctionListItem, PlaceBidRequest } from "../../utils/types";
import { placeBid } from "../../api";
import styles from "./index.module.css";

type Props = {
  open: boolean;
  auction: AuctionListItem | null;
  onClose: () => void;
  onPlaced: () => void; // נקרא אחרי הצלחה כדי לרענן רשימה
};

export default function PlaceBidDialog({
  open,
  auction,
  onClose,
  onPlaced,
}: Props) {
  const [bidderId, setBidderId] = useState<number>(1); // שונה מ-2 כדי למנוע בעיות עם המוכר
  const [maxBid, setMaxBid] = useState<number>(auction?.minBidToPlace ?? 0);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>("");

  // עדכון ערך ברגע שהמודל נפתח עם מכרז אחר
  useEffect(() => {
    if (auction) {
      setMaxBid(auction.minBidToPlace);
      setError("");
    }
  }, [auction]);

  if (!open || !auction) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      const body: PlaceBidRequest = { bidderId, maxBid };
      await placeBid(auction.id, body);
      onPlaced();
      onClose();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Bid failed");
    } finally {
      setSubmitting(false);
    }
  };

  const handleOverlayClick = (e: React.MouseEvent) => {
    // Close dialog only if clicking on the overlay (not on the dialog content)
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div className={styles.overlay} onClick={handleOverlayClick}>
      <div className={styles.dialog}>
        <h2 className={styles.title}>הגש הצעה – {auction.title}</h2>

        <form onSubmit={handleSubmit} className={styles.form}>
          <label className={styles.field}>
            <span className={styles.label}>
              User ID (זמני - אל תשתמש ב-1 אם אתה המוכר)
            </span>
            <input
              type="number"
              min={1}
              value={bidderId}
              onChange={(e) => setBidderId(Number(e.target.value))}
              className={styles.input}
              required
            />
          </label>

          <label className={styles.field}>
            <span className={styles.label}>
              Maximum Bid (מינימום: {auction.minBidToPlace.toFixed(2)})
            </span>
            <input
              type="number"
              step="0.01"
              min={auction.minBidToPlace}
              value={maxBid}
              onChange={(e) => setMaxBid(Number(e.target.value))}
              className={styles.input}
              required
            />
          </label>

          {error && <div className={styles.error}>{error}</div>}

          <div className={styles.buttons}>
            <button
              type="button"
              onClick={onClose}
              className={styles.cancelButton}
            >
              ביטול
            </button>
            <button
              type="submit"
              disabled={submitting}
              className={styles.submitButton}
            >
              {submitting ? "שולח..." : "הגש הצעה"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
