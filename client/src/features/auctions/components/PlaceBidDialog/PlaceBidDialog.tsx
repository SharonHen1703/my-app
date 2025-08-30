import { useState } from "react";
import type { AuctionListItem, PlaceBidRequest } from "../../utils/types";
import { placeBid } from "../../api";
import styles from "./PlaceBidDialog.module.css";

type Props = {
  open: boolean;
  auction: AuctionListItem | null;
  onClose: () => void;
  onPlaced: () => void;
};

export default function PlaceBidDialog({
  open,
  auction,
  onClose,
  onPlaced,
}: Props) {
  const [maxBid, setMaxBid] = useState<number>(auction?.minBidToPlace ?? 0);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>("");

  // פונקציה לפורמט מספר עם פסיקים
  const formatNumber = (num: number): string => {
    return num.toLocaleString("he-IL");
  };

  if (!open || !auction) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError("");

    try {
      const body: PlaceBidRequest = { maxBid };
      await placeBid(auction.id, body);
      onPlaced();
      onClose();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "שגיאה בהגשת ההצעה");
    } finally {
      setSubmitting(false);
    }
  };

  const handleOverlayClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div className={styles.overlay} onClick={handleOverlayClick}>
      <div className={styles.dialog}>
        <div className={styles.header}>
          <h2 className={styles.title}>הגשת הצעה</h2>
          <button
            type="button"
            onClick={onClose}
            className={styles.closeButton}
            aria-label="סגור"
          >
            ×
          </button>
        </div>

        <div className={styles.content}>
          <div className={styles.auctionInfo}>
            <h3 className={styles.auctionTitle}>{auction.title}</h3>
            <p className={styles.minBidInfo}>
              הצעה מינימלית מותרת:{" "}
              <strong>₪{formatNumber(auction.minBidToPlace)}</strong>
            </p>
          </div>

          <form onSubmit={handleSubmit} className={styles.form}>
            {/* מזהה משתמש הוסר - הזדהות נעשית דרך עוגייה/סשן */}

            <div className={styles.field}>
              <label htmlFor="max-bid" className={styles.label}>
                הצעת המחיר המירבית שלך:
              </label>
              <div className={styles.inputGroup}>
                <input
                  id="max-bid"
                  type="number"
                  min={auction.minBidToPlace}
                  step={auction.bidIncrement}
                  value={maxBid}
                  onChange={(e) => setMaxBid(Number(e.target.value))}
                  className={styles.bidInput}
                  required
                />
                <span className={styles.currency}>₪</span>
              </div>
              <div className={styles.helpText}>
                הכנס ₪{formatNumber(auction.minBidToPlace)} או יותר
                {auction.bidIncrement > 0 &&
                  ` | צעד הצעה: ₪${formatNumber(auction.bidIncrement)}`}
              </div>
            </div>

            {error && <div className={styles.error}>{error}</div>}

            <div className={styles.actions}>
              <button
                type="button"
                onClick={onClose}
                className={styles.cancelButton}
                disabled={submitting}
              >
                ביטול
              </button>
              <button
                type="submit"
                className={styles.submitButton}
                disabled={submitting || maxBid < auction.minBidToPlace}
              >
                {submitting ? "שולח..." : "הגש הצעה"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
