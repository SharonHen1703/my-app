import { useState, useEffect } from "react";
import type { AuctionListItem } from "../../utils/types";
import { getAuctionDetail } from "../../api";
import { translateCondition } from "../../utils/conditionTranslations";
import styles from "./index.module.css";

type Props = {
  open: boolean;
  auction: AuctionListItem | null;
  onClose: () => void;
  onPlaceBid: (auction: AuctionListItem) => void;
};

export default function AuctionDetailsDialog({
  open,
  auction,
  onClose,
  onPlaceBid,
}: Props) {
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [auctionDetail, setAuctionDetail] = useState<AuctionListItem | null>(
    null
  );
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (auction && open) {
      setCurrentImageIndex(0);
      setLoading(true);

      getAuctionDetail(auction.id)
        .then((detail) => {
          setAuctionDetail(detail);
        })
        .catch((err) => {
          console.error("Failed to load auction detail:", err);
          setAuctionDetail(auction);
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, [auction, open]);

  if (!open || !auction) return null;

  const displayAuction = auctionDetail || auction;
  const images = displayAuction.imageUrls || [];

  const nextImage = () => {
    if (images.length > 1) {
      setCurrentImageIndex((prev) => (prev + 1) % images.length);
    }
  };

  const prevImage = () => {
    if (images.length > 1) {
      setCurrentImageIndex(
        (prev) => (prev - 1 + images.length) % images.length
      );
    }
  };

  const handleOverlayClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  const handlePlaceBid = () => {
    onPlaceBid(displayAuction);
  };

  return (
    <div className={styles.overlay} onClick={handleOverlayClick}>
      <div className={styles.dialog}>
        <button
          type="button"
          onClick={onClose}
          className={styles.closeButton}
          aria-label="סגור"
        >
          ×
        </button>

        <h2 className={styles.title}>{displayAuction.title}</h2>

        {loading && <div className={styles.loading}>טוען פרטים...</div>}

        {/* Image Carousel */}
        {images.length > 0 && (
          <div className={styles.carousel}>
            {images.length > 1 && (
              <button
                type="button"
                onClick={prevImage}
                className={styles.carouselButton}
                aria-label="תמונה קודמת"
              >
                ‹
              </button>
            )}
            <div className={styles.imageContainer}>
              <img
                src={images[currentImageIndex]}
                alt={displayAuction.title}
                className={styles.carouselImage}
              />
            </div>
            {images.length > 1 && (
              <button
                type="button"
                onClick={nextImage}
                className={styles.carouselButton}
                aria-label="תמונה הבאה"
              >
                ›
              </button>
            )}
            {images.length > 1 && (
              <div className={styles.imageIndicators}>
                {images.map((_, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() => setCurrentImageIndex(index)}
                    className={`${styles.indicator} ${
                      index === currentImageIndex ? styles.active : ""
                    }`}
                    aria-label={`תמונה ${index + 1}`}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {/* Auction Details */}
        <div className={styles.detailsGrid}>
          <div className={styles.detailItem}>
            <span className={styles.detailLabel}>תיאור המוצר:</span>
            <span className={styles.detailValue}>
              {displayAuction.description}
            </span>
          </div>

          <div className={styles.detailItem}>
            <span className={styles.detailLabel}>מצב הפריט:</span>
            <span className={styles.detailValue}>
              {translateCondition(displayAuction.condition)}
            </span>
          </div>

          <div className={styles.detailItem}>
            <span className={styles.detailLabel}>הצעה נוכחית:</span>
            <span className={styles.detailValue}>
              {displayAuction.currentBidAmount
                ? `₪${displayAuction.currentBidAmount.toLocaleString()}`
                : "אין הצעות"}
            </span>
          </div>

          <div className={styles.detailItem}>
            <span className={styles.detailLabel}>מספר הצעות:</span>
            <span className={styles.detailValue}>
              {displayAuction.bidsCount}
            </span>
          </div>
        </div>

        <div className={styles.actions}>
          <button
            type="button"
            onClick={handlePlaceBid}
            className={styles.placeBidButton}
          >
            הגש הצעה
          </button>
        </div>
      </div>
    </div>
  );
}
