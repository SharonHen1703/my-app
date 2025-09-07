import { useState, useEffect } from "react";
import type { AuctionListItem } from "../../utils/types";
import { placeBid, getUserBidsSummary } from "../../api";
import { isAuctionEnded } from "../../utils/timeUtils";
import styles from "./BidSubmissionDialog.module.css";

const formatNumber = (num: number) => {
  return new Intl.NumberFormat("he-IL").format(num);
};

type Props = {
  open: boolean;
  auction: AuctionListItem | null;
  onClose: () => void;
  onBidPlaced: () => void;
};

export default function BidSubmissionDialog({
  open,
  auction,
  onClose,
  onBidPlaced,
}: Props) {
  const [maxBid, setMaxBid] = useState(auction?.minBidToPlace || 0);
  const [requiredMin, setRequiredMin] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [userBidInfo, setUserBidInfo] = useState<{
    yourMax: number | null;
    isLeading: boolean;
  } | null>(null);

  // עדכון הערך כאשר האוקציה משתנה + חישוב מינימום נדרש לפי כללים (Leader/Non-leader)
  useEffect(() => {
    let cancelled = false;
    const computeRequiredMin = async () => {
      if (!auction) return;

      console.log(
        "🔍 Computing required min for auction:",
        auction.id,
        auction.title
      );

      // השרת כבר מחשב את minBidToPlace נכון:
      // אם אין הצעות: minPrice
      // אם יש הצעות: currentBidAmount + bidIncrement
      const baseMin = auction.minBidToPlace;
      console.log("� Server-calculated minBidToPlace:", baseMin);

      let computedMin = baseMin;
      let userBidItem = null;

      // Try refine using user bid summary to handle leader rule: incomingMax > previousMax
      try {
        const summary = await getUserBidsSummary();
        if (cancelled) return;
        console.log("👤 User bid summary:", summary);
        console.log(
          "🔍 Looking for auctionId:",
          auction.id,
          "type:",
          typeof auction.id
        );

        // Make sure we're comparing correctly - convert both to numbers
        const targetId = Number(auction.id);
        console.log("🔍 Converted targetId:", targetId);

        const item = summary.find((s) => {
          const summaryId = Number(s.auctionId);
          console.log(
            "  Checking summaryId:",
            summaryId,
            "vs targetId:",
            targetId,
            "equals:",
            summaryId === targetId
          );
          console.log("  Item details:", {
            auctionTitle: s.auctionTitle,
            yourMax: s.yourMax,
            leading: s.leading,
            currentPrice: s.currentPrice,
          });
          return summaryId === targetId;
        });
        console.log("🎯 Found user bid for this auction:", item);
        if (item) {
          userBidItem = item; // Save for later use
          console.log("📊 Found bid item:", {
            leading: item.leading,
            yourMax: item.yourMax,
            currentPrice: item.currentPrice,
            baseMin: baseMin,
          });

          if (item.leading) {
            // Leader case: Must bid more than their current maxBid
            // For leader: minimum is yourMax + 1 (or some small increment)
            const leaderMinimum = (item.yourMax || 0) + 1;
            console.log(
              "👑 LEADER case - yourMax:",
              item.yourMax,
              "leaderMinimum (yourMax + 1):",
              leaderMinimum,
              "baseMin (general minimum):",
              baseMin
            );
            // Leader should use the higher of their minimum or the general minimum
            computedMin = Math.max(leaderMinimum, baseMin);
            console.log("👑 LEADER final computedMin:", computedMin);
          } else {
            console.log(
              "👤 Non-leader case - using server minBidToPlace:",
              baseMin
            );
          }
        } else {
          console.log(
            "❌ No bid found for this auction - using server minBidToPlace:",
            baseMin
          );
        }
      } catch {
        console.log(
          "⚠️ Error getting user bid summary - using server minBidToPlace:",
          baseMin
        );
        console.log("⚠️ This usually means user is not authenticated");
        // If unauthenticated or error, fall back to computedMin without user data
        // no-op
      }

      console.log("🎯 Final computedMin:", computedMin);

      if (!cancelled) {
        setRequiredMin(computedMin);
        setMaxBid(computedMin);
        // Save user bid info for display
        if (userBidItem) {
          setUserBidInfo({
            yourMax: userBidItem.yourMax,
            isLeading: userBidItem.leading,
          });
        } else {
          setUserBidInfo(null);
        }
      }
    };

    computeRequiredMin();
    return () => {
      cancelled = true;
    };
  }, [auction]);

  if (!open || !auction) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    // בדוק אם המכרז הסתיים
    if (isAuctionEnded(auction.endDate)) {
      setError("המכרז הסתיים ולא ניתן יותר להגיש הצעות");
      return;
    }

    const effectiveMin = requiredMin ?? auction.minBidToPlace;
    if (maxBid < effectiveMin) {
      setError(`הצעה מינימלית מותרת: ₪${formatNumber(effectiveMin)}`);
      return;
    }

    setSubmitting(true);

    try {
      await placeBid(auction.id, {
        maxBid: maxBid,
      });

      onBidPlaced();
      onClose();
    } catch (error) {
      console.error("Failed to place bid:", error);

      // Parse error message from server
      let errorMessage = "שגיאה בהגשת ההצעה. אנא נסה שוב.";
      try {
        const errorText = error instanceof Error ? error.message : "";
        const parsed = JSON.parse(errorText);
        if (parsed.message) {
          errorMessage = parsed.message;
        }
      } catch {
        // Keep default message
      }

      setError(errorMessage);
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
            {userBidInfo && (
              <>
                {userBidInfo.isLeading && (
                  <div className={styles.leadingStatus}>
                    🏆 אתה מוביל כעת במכרז!
                  </div>
                )}
              </>
            )}
          </div>

          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.field}>
              <label htmlFor="max-bid" className={styles.label}>
                הצעת המחיר המירבית שלך:
              </label>
              <div className={styles.inputGroup}>
                <input
                  id="max-bid"
                  type="number"
                  min={(requiredMin ?? auction.minBidToPlace) || 0}
                  step="any"
                  value={maxBid || (requiredMin ?? auction.minBidToPlace)}
                  onChange={(e) => {
                    const value = Number(e.target.value);
                    const minVal = requiredMin ?? auction.minBidToPlace;
                    setMaxBid(value || minVal);
                  }}
                  className={styles.bidInput}
                  placeholder={
                    userBidInfo?.isLeading && userBidInfo.yourMax
                      ? `כמוביל, עליך להציע יותר מההצעה הנוכחית שלך שהיא ₪${formatNumber(
                          userBidInfo.yourMax
                        )}`
                      : `${formatNumber(requiredMin ?? auction.minBidToPlace)}`
                  }
                  required
                />
                <span className={styles.currency}>₪</span>
              </div>
              <div className={styles.helpText}>
                {userBidInfo?.isLeading && userBidInfo.yourMax ? (
                  <>
                    מינימום לצורך הגשת הצעה: ₪
                    {formatNumber(requiredMin ?? auction.minBidToPlace)}
                    (יותר מההצעה הנוכחית שלך ₪
                    {formatNumber(userBidInfo.yourMax)})
                  </>
                ) : (
                  <>
                    הכנס ₪{formatNumber(requiredMin ?? auction.minBidToPlace)}{" "}
                    או יותר
                  </>
                )}
              </div>
            </div>

            {error && <div className={styles.error}>{error}</div>}

            <div className={styles.actions}>
              <button
                type="submit"
                className={styles.submitButton}
                disabled={
                  submitting ||
                  maxBid < (requiredMin ?? auction.minBidToPlace) ||
                  isAuctionEnded(auction.endDate)
                }
              >
                {submitting
                  ? "שולח..."
                  : isAuctionEnded(auction.endDate)
                  ? "המכרז הסתיים"
                  : "הגש הצעה"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
