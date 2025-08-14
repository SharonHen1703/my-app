import React from "react";
import AuctionCard from "../AuctionCard";
import type { AuctionListItem } from "../../utils/types";
import styles from "./index.module.css";

interface ApiResponse {
  page: number;
  size: number;
  total: number;
  totalPages: number;
  items: AuctionListItem[];
}

export default function AuctionsList() {
  const [allItems, setAllItems] = React.useState<AuctionListItem[]>([]);
  const [error, setError] = React.useState<string | null>(null);
  const [currentPage, setCurrentPage] = React.useState(0);
  const [loading, setLoading] = React.useState(false);
  const [hasMore, setHasMore] = React.useState(true);

  const fetchAuctions = React.useCallback(
    async (page: number, isFirstLoad = false) => {
      if (loading || (!hasMore && !isFirstLoad)) return;

      setLoading(true);
      try {
        const response = await fetch(`/api/auctions?page=${page}&size=12`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const data = (await response.json()) as ApiResponse;

        if (isFirstLoad) {
          setAllItems(data.items);
        } else {
          setAllItems((prev) => [...prev, ...data.items]);
        }

        setCurrentPage(page);
        setHasMore(page < data.totalPages - 1);
      } catch (e) {
        setError(e instanceof Error ? e.message : String(e));
      } finally {
        setLoading(false);
      }
    },
    [loading, hasMore]
  );

  // Load more items when user scrolls to bottom
  const handleScroll = React.useCallback(() => {
    if (loading || !hasMore) return;

    const scrollHeight = document.documentElement.scrollHeight;
    const scrollTop = document.documentElement.scrollTop;
    const clientHeight = document.documentElement.clientHeight;

    // Load more when user is 200px from bottom
    if (scrollHeight - scrollTop - clientHeight < 200) {
      fetchAuctions(currentPage + 1);
    }
  }, [fetchAuctions, currentPage, loading, hasMore]);

  React.useEffect(() => {
    fetchAuctions(0, true);
  }, [fetchAuctions]);

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
          <AuctionCard key={item.id} item={item} />
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
    </>
  );
}
