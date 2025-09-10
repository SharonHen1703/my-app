import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import type { AuctionListItem } from "../../utils/types";
import { getAuctionDetail } from "../../api";
import { translateCondition } from "../../utils/conditionTranslations";
import { calculateTimeRemaining, isAuctionEnded } from "../../utils/timeUtils";
import BidSubmissionDialog from "../../components/PlaceBidDialog/BidSubmissionDialog";
import { UserMenu } from "../../../../components/common";
import { useAuth } from "../../../auth/useAuth";
import styles from "./index.module.css";

export default function AuctionDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [auction, setAuction] = useState<AuctionListItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [bidDialogOpen, setBidDialogOpen] = useState(false);
  const { user } = useAuth();

  // Check if the current user is the seller of this auction
  const isOwner = user && auction && user.id === auction.sellerId;

  useEffect(() => {
    // גלול לראש הדף כשהקומפוננטה נטענת
    window.scrollTo(0, 0);

    if (!id) {
      setError("מזהה מכרז לא תקין");
      setLoading(false);
      return;
    }

    const loadAuction = async () => {
      try {
        setLoading(true);
        const auctionData = await getAuctionDetail(Number(id));
        setAuction(auctionData);
      } catch (err) {
        console.error("Failed to load auction:", err);
        setError("שגיאה בטעינת פרטי המכרז");
      } finally {
        setLoading(false);
      }
    };

    loadAuction();
  }, [id]);

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>
          <div className={styles.spinner}></div>
          <p>טוען פרטי מכרז...</p>
        </div>
      </div>
    );
  }

  if (error || !auction) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>
          <h2>שגיאה</h2>
          <p>{error || "מכרז לא נמצא"}</p>
        </div>
      </div>
    );
  }

  const images = auction.imageUrls || [];

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

  const handleBidPlaced = () => {
    // Refresh auction data
    if (id) {
      getAuctionDetail(Number(id)).then(setAuction).catch(console.error);
    }
  };

  return (
    <div className={styles.container}>
      {/* User menu */}
      <UserMenu />

      {/* Main content */}
      <div className={styles.content}>
        <h1 className={styles.title}>{auction.title}</h1>

        <div className={styles.mainContent}>
          {/* Image carousel - Left side */}
          <div className={styles.carouselSection}>
            {images.length > 0 && (
              <div className={styles.carousel}>
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
                <div className={styles.imageContainer}>
                  <img
                    src={images[currentImageIndex]}
                    alt={auction.title}
                    className={styles.carouselImage}
                  />
                </div>
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
          </div>

          {/* Details and bid section - Right side */}
          <div className={styles.detailsSection}>
            <div className={styles.details}>
              <div className={styles.detailsGrid}>
                <div className={styles.detailItem}>
                  <span className={styles.detailLabel}>תיאור המוצר:</span>
                  <span className={styles.detailValue}>
                    {auction.description}
                  </span>
                </div>

                <div className={styles.detailItem}>
                  <span className={styles.detailLabel}>מצב הפריט:</span>
                  <span className={styles.detailValue}>
                    {translateCondition(auction.condition)}
                  </span>
                </div>

                <div className={styles.detailItem}>
                  <span className={styles.detailLabel}>הצעה נוכחית:</span>
                  <span className={styles.detailValue}>
                    {auction.currentBidAmount
                      ? `₪${auction.currentBidAmount.toLocaleString()}`
                      : "אין הצעות"}
                  </span>
                </div>

                <div className={styles.detailItem}>
                  <span className={styles.detailLabel}>מספר הצעות:</span>
                  <span
                    className={`${styles.detailValue} ${styles.clickableBidsCount}`}
                    onClick={() => window.open(`/auction/${id}/bids`, "_blank")}
                  >
                    {auction.bidsCount}
                  </span>
                </div>

                <div className={styles.detailItem}>
                  <span className={styles.detailLabel}>זמן שנותר:</span>
                  <span className={styles.detailValue}>
                    {calculateTimeRemaining(auction.endDate)}
                  </span>
                </div>
              </div>
            </div>

            {/* Bid action - only show if user is not the owner */}
            {!isOwner && (
              <div className={styles.bidSection}>
                <button
                  type="button"
                  onClick={() => setBidDialogOpen(true)}
                  className={styles.placeBidButton}
                  disabled={isAuctionEnded(auction.endDate)}
                >
                  {isAuctionEnded(auction.endDate)
                    ? "המכרז הסתיים"
                    : "הגש הצעה"}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Bid submission dialog - only show if user is not the owner */}
      {!isOwner && (
        <BidSubmissionDialog
          open={bidDialogOpen}
          auction={auction}
          onClose={() => setBidDialogOpen(false)}
          onBidPlaced={handleBidPlaced}
        />
      )}
    </div>
  );
}
