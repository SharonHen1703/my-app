import { useState, memo } from "react";
import type { AuctionListItem } from "../../utils/types";
import { calculateTimeRemaining } from "../../utils/timeUtils";
import styles from "./index.module.css";

interface AuctionCardProps {
  item: AuctionListItem;
  onClick?: (auction: AuctionListItem) => void;
}

const AuctionCard = memo(function AuctionCard({
  item,
  onClick,
}: AuctionCardProps) {
  const [imageLoaded, setImageLoaded] = useState(false);
  const [imageError, setImageError] = useState(false);

  // Filter out invalid image URLs (old file paths that no longer work)
  const validImageUrls =
    item.imageUrls?.filter(
      (url) => url && (url.startsWith("data:") || url.startsWith("http"))
    ) || [];

  const firstImage = validImageUrls.length > 0 ? validImageUrls[0] : null;

  const price = item.currentBidAmount ?? item.minPrice;

  return (
    <div className={styles.card}>
      <div className={styles.topHalf}>
        <div className={styles.imageContainer}>
          {firstImage && !imageError ? (
            <img
              src={firstImage}
              alt={item.title}
              className={styles.image}
              style={{
                display: imageLoaded ? "block" : "none",
                cursor: onClick ? "pointer" : "default",
              }}
              onLoad={() => setImageLoaded(true)}
              onError={() => setImageError(true)}
              onClick={() => onClick?.(item)}
            />
          ) : (
            <div
              className={styles.placeholderContainer}
              style={{ cursor: onClick ? "pointer" : "default" }}
              onClick={() => onClick?.(item)}
            >
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
              <span className={styles.placeholderText}>אין תמונה</span>
            </div>
          )}
          {!imageLoaded && !imageError && firstImage && (
            <div className={styles.loadingContainer}>
              <div className={styles.spinner}></div>
            </div>
          )}
        </div>
        <div className={styles.titleContainer}>
          <h3
            className={styles.title}
            onClick={() => onClick?.(item)}
            style={{ cursor: onClick ? "pointer" : "default" }}
          >
            {item.title}
          </h3>
          <div className={styles.details}>
            <div>
              מחיר נוכחי: <b>₪{price.toLocaleString()}</b>
            </div>
            <div>הצעות: {item.bidsCount}</div>
            <div>זמן שנותר: {calculateTimeRemaining(item.endDate)}</div>
          </div>
        </div>
      </div>
    </div>
  );
});

AuctionCard.displayName = "AuctionCard";

export default AuctionCard;
