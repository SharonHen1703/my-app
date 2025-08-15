import React from "react";
import PlaceBidDialog from "../PlaceBidDialog";
import type { AuctionListItem } from "../../utils/types";
import { fetchAuctions } from "../../api";
import styles from "./index.module.css";
import AuctionCard from "../AuctionCard";

function AuctionsList() {
  const [allItems, setAllItems] = React.useState<AuctionListItem[]>([]);
  const [error, setError] = React.useState<string | null>(null);
  const [loading, setLoading] = React.useState(false);
  const [hasMore, setHasMore] = React.useState(true);

  // Dialog state
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [selectedAuction, setSelectedAuction] =
    React.useState<AuctionListItem | null>(null);

  // Use refs to avoid dependency issues
  const loadingRef = React.useRef(false);
  const hasMoreRef = React.useRef(true);
  const currentPageRef = React.useRef(0);

  const fetchAuctionsData = React.useCallback(
    async (page: number, isFirstLoad = false) => {
      if (loadingRef.current || (!hasMoreRef.current && !isFirstLoad)) return;

      loadingRef.current = true;
      setLoading(true);

      try {
        const data = await fetchAuctions(page, 12);

        if (isFirstLoad) {
          setAllItems(data.items);
        } else {
          setAllItems((prev) => [...prev, ...data.items]);
        }

        currentPageRef.current = page;

        hasMoreRef.current = page < data.totalPages - 1;
        setHasMore(hasMoreRef.current);
      } catch (e) {
        setError(e instanceof Error ? e.message : String(e));
      } finally {
        loadingRef.current = false;
        setLoading(false);
      }
    },
    []
  );

  const handlePlaceBid = React.useCallback((auction: AuctionListItem) => {
    setSelectedAuction(auction);
    setDialogOpen(true);
  }, []);

  const handleBidPlaced = React.useCallback(() => {
    // Refresh the list after a successful bid
    fetchAuctionsData(0, true);
  }, [fetchAuctionsData]);

  // Load more items when user scrolls to bottom
  const handleScroll = React.useCallback(() => {
    if (loadingRef.current || !hasMoreRef.current) return;

    const scrollHeight = document.documentElement.scrollHeight;
    const scrollTop = document.documentElement.scrollTop;
    const clientHeight = document.documentElement.clientHeight;

    // Load more when user is 200px from bottom
    if (scrollHeight - scrollTop - clientHeight < 200) {
      fetchAuctionsData(currentPageRef.current + 1);
    }
  }, [fetchAuctionsData]);

  React.useEffect(() => {
    fetchAuctionsData(0, true);
  }, [fetchAuctionsData]);

  React.useEffect(() => {
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [handleScroll]);

  if (error) return <div className={styles.errorMessage}>שגיאה: {error}</div>;
  if (allItems.length === 0 && !loading)
    return <div className={styles.emptyMessage}>אין מכרזים פעילים כרגע.</div>;

  return (
    <>
      <div className={styles.container}>
        {allItems.map((item) => (
          <AuctionCard key={item.id} item={item} onPlaceBid={handlePlaceBid} />
        ))}
      </div>

      {loading && (
        <div className={styles.loadingMessage}>
          <div className={styles.spinner}></div>
          <span>טוען עוד מכרזים...</span>
        </div>
      )}

      {!hasMore && allItems.length > 0 && (
        <div className={styles.endMessage}>
          הוצגו כל המכרזים ({allItems.length} סה״כ)
        </div>
      )}

      <PlaceBidDialog
        open={dialogOpen}
        auction={selectedAuction}
        onClose={() => setDialogOpen(false)}
        onPlaced={handleBidPlaced}
      />
    </>
  );
}

export default AuctionsList;
