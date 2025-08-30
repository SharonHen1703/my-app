import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import type { AuctionListItem } from "../../utils/types";
import {
  getAuctionDetail,
  getBidHistory,
  type BidHistoryItem,
} from "../../api";
import FullWidthAuctionCard from "../../components/FullWidthAuctionCard";
import BidSubmissionDialog from "../../components/PlaceBidDialog/BidSubmissionDialog";
import { useAuth } from "../../../auth/useAuth";
import styles from "./index.module.css";

export default function BidHistoryPage() {
  const { id } = useParams<{ id: string }>();
  const [auction, setAuction] = useState<AuctionListItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [bidDialogOpen, setBidDialogOpen] = useState(false);
  const [history, setHistory] = useState<BidHistoryItem[]>([]);
  const { user } = useAuth();

  // Compute canBid based on user authentication, seller status, and auction status
  const canBid = Boolean(
    user &&
      auction &&
      user.id !== auction.sellerId &&
      auction.status === "active"
  );

  const showOwnerMessage = Boolean(
    user && auction && user.id === auction.sellerId
  );

  useEffect(() => {
    window.scrollTo(0, 0);

    if (!id) {
      setError("מזהה מכרז לא תקין");
      setLoading(false);
      return;
    }

    const loadData = async () => {
      try {
        setLoading(true);
        const [auctionData, historyData] = await Promise.all([
          getAuctionDetail(Number(id)),
          getBidHistory(Number(id)),
        ]);
        setAuction(auctionData);
        setHistory(historyData);
      } catch (err) {
        console.error("Failed to load data:", err);
        setError("שגיאה בטעינת נתונים");
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [id]);

  const handleBidPlaced = () => {
    if (id) {
      getAuctionDetail(Number(id)).then(setAuction).catch(console.error);
      getBidHistory(Number(id)).then(setHistory).catch(console.error);
    }
  };

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>
          <div className={styles.spinner}></div>
          <p>טוען נתונים...</p>
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
          <button
            type="button"
            onClick={() => (window.location.href = "/")}
            className={styles.backButton}
          >
            חזור למכרזים
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>היסטוריית הצעות</h1>
      </div>

      {/* Auction Card - Full Width */}
      <div className={styles.auctionCardWrapper}>
        <FullWidthAuctionCard
          item={auction}
          onPlaceBid={() => setBidDialogOpen(true)}
          canBid={canBid}
          showOwnerMessage={showOwnerMessage}
        />
      </div>

      {/* Bid History Table */}
      <div className={styles.historySection}>
        {history.length === 0 ? (
          <div className={styles.historyEmpty}>אין היסטוריית הצעות להצגה</div>
        ) : (
          <div className={styles.historyTableWrapper}>
            <table className={styles.historyTable}>
              <thead>
                <tr>
                  <th>מציע</th>
                  <th>סכום מוצג</th>
                  <th>זמן</th>
                </tr>
              </thead>
              <tbody>
                {history
                  .sort((a, b) => {
                    const timeA = new Date(a.snapshotTime).getTime();
                    const timeB = new Date(b.snapshotTime).getTime();

                    // First sort by time (newest first)
                    if (timeA !== timeB) {
                      return timeB - timeA;
                    }

                    // If same timestamp, ensure USER_BID comes before AUTO_RAISE/TIE_AUTO
                    const kindOrder = {
                      USER_BID: 0,
                      AUTO_RAISE: 1,
                      TIE_AUTO: 1,
                    };
                    const orderA =
                      kindOrder[a.kind as keyof typeof kindOrder] ?? 2;
                    const orderB =
                      kindOrder[b.kind as keyof typeof kindOrder] ?? 2;

                    return orderA - orderB;
                  })
                  .map((h) => (
                    <tr key={h.snapshotId}>
                      <td>#{h.bidderId}</td>
                      <td>₪{h.displayedBid.toLocaleString()}</td>
                      <td>{new Date(h.snapshotTime).toLocaleString()}</td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Bid submission dialog - only show if user can bid */}
      {canBid && (
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
