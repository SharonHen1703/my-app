import { useState, useMemo, memo, useEffect } from "react";
import type { AuctionListItem } from "../../utils/types";
import styles from "./index.module.css";

interface AuctionCardProps {
  item: AuctionListItem;
  onPlaceBid?: (auction: AuctionListItem) => void;
}

const AuctionCard = memo(function AuctionCard({
  item,
  onPlaceBid,
}: AuctionCardProps) {
  const [imageLoaded, setImageLoaded] = useState(false);
  const [imageError, setImageError] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [currentTime, setCurrentTime] = useState(Date.now());

  // Update time every minute to avoid constant re-renders
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentTime(Date.now());
    }, 60000); // Update every minute instead of every second

    return () => clearInterval(interval);
  }, []);

  const price = item.currentBidAmount ?? item.minPrice;

  const timeLeftText = useMemo(() => {
    const end = new Date(item.endDate).getTime();
    const diff = end - currentTime;
    if (diff <= 0) return "הסתיים";
    const sec = Math.floor(diff / 1000);
    const d = Math.floor(sec / 86400);
    const h = Math.floor((sec % 86400) / 3600);
    const m = Math.floor((sec % 3600) / 60);
    return d > 0 ? `${d} ימים ${h} שעות` : `${h} שעות ${m} דק׳`;
  }, [item.endDate, currentTime]);

  // השתמש רק במערך תמונות
  const images = useMemo(
    () => (item.imageUrls?.length > 0 ? item.imageUrls : []),
    [item.imageUrls]
  );

  const hasMultipleImages = images.length > 1;
  const currentImage = images[currentImageIndex];

  const nextImage = () => {
    setCurrentImageIndex((prev) => (prev + 1) % images.length);
    setImageLoaded(false);
  };

  const prevImage = () => {
    setCurrentImageIndex((prev) => (prev - 1 + images.length) % images.length);
    setImageLoaded(false);
  };

  return (
    <div className={styles.card}>
      <div className={styles.topHalf}>
        <div className={styles.imageContainer}>
          {!imageError && currentImage && (
            <>
              <img
                src={currentImage}
                alt={`${item.title} - תמונה ${currentImageIndex + 1}`}
                loading="lazy"
                className={`${styles.image} ${
                  imageLoaded ? styles.imageLoaded : styles.imageLoading
                }`}
                onLoad={() => setImageLoaded(true)}
                onError={() => setImageError(true)}
              />

              {/* כפתורי דפדוף */}
              {hasMultipleImages && (
                <>
                  <button
                    className={`${styles.navButton} ${styles.navButtonPrev}`}
                    onClick={prevImage}
                    aria-label="תמונה קודמת"
                  >
                    ‹
                  </button>
                  <button
                    className={`${styles.navButton} ${styles.navButtonNext}`}
                    onClick={nextImage}
                    aria-label="תמונה הבאה"
                  >
                    ›
                  </button>

                  {/* אינדיקטורים */}
                  <div className={styles.imageIndicators}>
                    {images.map((_, index) => (
                      <button
                        key={index}
                        className={`${styles.indicator} ${
                          index === currentImageIndex
                            ? styles.indicatorActive
                            : ""
                        }`}
                        onClick={() => {
                          setCurrentImageIndex(index);
                          setImageLoaded(false);
                        }}
                        aria-label={`תמונה ${index + 1}`}
                      />
                    ))}
                  </div>
                </>
              )}
            </>
          )}

          {!imageLoaded && !imageError && currentImage && (
            <div className={styles.loadingContainer}>
              <div className={styles.spinner}></div>
            </div>
          )}

          {(imageError || !currentImage) && (
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
        <div className={styles.titleContainer}>
          <h3 className={styles.title}>{item.title}</h3>
        </div>
      </div>
      <div className={styles.details}>
        <div>
          מחיר נוכחי: <b>{price.toLocaleString()}</b> ₪
        </div>
        <div>הצעות: {item.bidsCount}</div>
        <div>זמן שנותר: {timeLeftText}</div>

        {onPlaceBid && (
          <button className={styles.bidButton} onClick={() => onPlaceBid(item)}>
            הגש הצעה
          </button>
        )}
      </div>
    </div>
  );
});

AuctionCard.displayName = "AuctionCard";

export default AuctionCard;
