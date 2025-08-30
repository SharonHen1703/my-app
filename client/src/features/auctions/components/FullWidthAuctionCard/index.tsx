import type { AuctionListItem } from "../../utils/types";
import { calculateTimeRemaining, isAuctionEnded } from "../../utils/timeUtils";
import { translateCondition } from "../../utils/conditionTranslations";
import styles from "./index.module.css";

interface FullWidthAuctionCardProps {
  item: AuctionListItem;
  onPlaceBid?: () => void;
  canBid?: boolean;
  showOwnerMessage?: boolean;
}

export default function FullWidthAuctionCard({
  item,
  onPlaceBid,
  canBid = true,
  showOwnerMessage = false,
}: FullWidthAuctionCardProps) {
  const firstImage =
    item.imageUrls && item.imageUrls.length > 0 ? item.imageUrls[0] : null;
  const price = item.currentBidAmount ?? item.minPrice;

  return (
    <div className={styles.fullWidthCard}>
      {/* Image Section */}
      <div className={styles.imageSection}>
        {firstImage ? (
          <img src={firstImage} alt={item.title} className={styles.image} />
        ) : (
          <div className={styles.placeholderContainer}>
            <svg
              className={styles.placeholderIcon}
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fillRule="evenodd"
                d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
                clipRule="evenodd"
              />
            </svg>
          </div>
        )}
      </div>

      {/* Content Section */}
      <div className={styles.contentSection}>
        <div className={styles.headerSection}>
          <h2 className={styles.cardTitle}>{item.title}</h2>
          <div className={styles.priceSection}>
            <span className={styles.priceLabel}>מחיר נוכחי:</span>
            <span className={styles.priceValue}>₪{price.toLocaleString()}</span>
          </div>
        </div>

        <div className={styles.detailsSection}>
          <div className={styles.detailsGrid}>
            <div className={styles.detailItem}>
              <span className={styles.label}>תיאור:</span>
              <span className={styles.value}>{item.description}</span>
            </div>

            <div className={styles.detailItem}>
              <span className={styles.label}>מצב:</span>
              <span className={styles.value}>
                {translateCondition(item.condition)}
              </span>
            </div>

            <div className={styles.detailItem}>
              <span className={styles.label}>הצעות:</span>
              <span className={styles.value}>{item.bidsCount}</span>
            </div>

            <div className={styles.detailItem}>
              <span className={styles.label}>זמן שנותר:</span>
              <span
                className={`${styles.value} ${
                  isAuctionEnded(item.endDate) ? styles.ended : ""
                }`}
              >
                {calculateTimeRemaining(item.endDate)}
              </span>
            </div>
          </div>
        </div>

        <div className={styles.actionSection}>
          {showOwnerMessage ? (
            <div className={styles.ownerMessage}>
              לא ניתן להגיש הצעה למכרז שלך
            </div>
          ) : canBid ? (
            <button
              type="button"
              onClick={onPlaceBid}
              className={styles.placeBidButton}
              disabled={isAuctionEnded(item.endDate)}
            >
              {isAuctionEnded(item.endDate) ? "המכרז הסתיים" : "הגש הצעה"}
            </button>
          ) : null}
        </div>
      </div>
    </div>
  );
}
