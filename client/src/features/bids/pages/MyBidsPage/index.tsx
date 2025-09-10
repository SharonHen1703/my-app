import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getUserBidsSummary } from "../../../auctions/api";
import type { UserBidSummaryItem } from "../../utils/types";
import {
  calculateTimeRemaining,
  isAuctionUrgent,
  parseTimeRemaining,
} from "../../../auctions/utils/timeUtils";
import { formatCurrency } from "../../utils/formatters";
import { HomeIcon, BidIcon } from "../../components/Icons";
import styles from "./index.module.css";
import SmartTable, {
  type ColumnDef,
  type FilterOption,
  type SortDirection,
} from "../../../shared/SmartTable";
import { useAuth } from "../../../auth/useAuth";
import { UserMenu } from "../../../../components/common";

export default function MyBidsPage() {
  const [bids, setBids] = useState<UserBidSummaryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const { user } = useAuth();

  const getBidStatusText = (bid: UserBidSummaryItem): string => {
    if (bid.status === "active") {
      return bid.leading ? "מוביל" : "הוצע סכום נמוך מדי";
    } else {
      // auction has ended
      return bid.leading ? "זכית" : "הפסדת";
    }
  };

  const getBidStatusClass = (bid: UserBidSummaryItem): string => {
    if (bid.status === "active") {
      return bid.leading ? styles.leadingBadge : styles.notLeadingBadge;
    } else {
      return bid.leading ? styles.wonBadge : styles.lostBadge;
    }
  };

  const handleActionClick = (bid: UserBidSummaryItem) => {
    if (bid.status === "active") {
      // מכרז פעיל - פתח פרטי המכרז בעמוד חדש להגשת הצעות נוספות
      window.open(`/auction/${bid.auctionId}`, "_blank");
    } else {
      // מכרז הסתיים - עבור לדף הבית
      navigate("/");
    }
  };

  useEffect(() => {
    const loadBids = async () => {
      try {
        setLoading(true);
        const data = await getUserBidsSummary();
        setBids(data);
      } catch (err) {
        console.error("Failed to load user bids:", err);
        setError("שגיאה בטעינת ההצעות שלך");
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      loadBids();
    }
  }, [user]);

  const getTimeRemainingClass = (endDate: string, status?: string): string => {
    if (status === "ended") {
      return styles.timeRemaining;
    }
    return isAuctionUrgent(endDate)
      ? `${styles.timeRemaining} ${styles.urgent}`
      : styles.timeRemaining;
  };

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>
          <div className={styles.spinner}></div>
          <p>טוען את ההצעות שלך...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>
          <h2>שגיאה</h2>
          <p>{error}</p>
          <button
            type="button"
            onClick={() => window.location.reload()}
            className={styles.backButton}
          >
            נסה שוב
          </button>
        </div>
      </div>
    );
  }

  if (bids.length === 0) {
    return (
      <div className={styles.container}>
        <UserMenu />
        <div className={styles.headerSection}>
          <h1 className={styles.title}>ההצעות שלי</h1>
        </div>
        <div className={styles.emptyState}>
          <h3>אין לך הצעות פעילות כרגע</h3>
          <p>כשתגיש הצעות למכרזים, הן יופיעו כאן</p>
        </div>
      </div>
    );
  }

  // SmartTable setup
  // Status mapping (internal values)
  type BidStatusFilter = "outbid_low" | "leading" | "lost" | "won";

  const getStatusValue = (bid: UserBidSummaryItem): BidStatusFilter => {
    if (bid.status === "active") {
      return bid.leading ? "leading" : "outbid_low";
    }
    return bid.leading ? "won" : "lost";
  };

  // Grouped default sort: active first by time remaining asc; non-active by endDate desc; tie-break title asc
  const groupedSort = (
    a: UserBidSummaryItem,
    b: UserBidSummaryItem
  ): number => {
    const statusA = a.status === "active" ? 0 : 1;
    const statusB = b.status === "active" ? 0 : 1;
    if (statusA !== statusB) return statusA - statusB;
    if (statusA === 0) {
      // both active: time remaining asc
      const cmp = parseTimeRemaining(a.endDate) - parseTimeRemaining(b.endDate);
      if (cmp !== 0) return cmp;
    } else {
      // both ended: end date desc
      const ea = new Date(a.endDate).getTime();
      const eb = new Date(b.endDate).getTime();
      const cmp = eb - ea;
      if (cmp !== 0) return cmp;
    }
    // tie-break by title asc (he)
    return a.auctionTitle.localeCompare(b.auctionTitle, "he");
  };

  const columns: ColumnDef<UserBidSummaryItem>[] = [
    {
      key: "title",
      header: "מכרז",
      accessor: (bid: UserBidSummaryItem) => (
        <a
          href={`/auction/${bid.auctionId}`}
          target="_blank"
          rel="noopener noreferrer"
          className={styles.auctionTitle}
        >
          {bid.auctionTitle}
        </a>
      ),
      comparator: (a: UserBidSummaryItem, b: UserBidSummaryItem) =>
        a.auctionTitle.localeCompare(b.auctionTitle, "he"),
      customSort: (rows: UserBidSummaryItem[], direction: SortDirection) => {
        const active: UserBidSummaryItem[] = [];
        const ended: UserBidSummaryItem[] = [];
        rows.forEach((r: UserBidSummaryItem) =>
          r.status === "active" ? active.push(r) : ended.push(r)
        );
        const cmp = (a: UserBidSummaryItem, b: UserBidSummaryItem) =>
          a.auctionTitle.localeCompare(b.auctionTitle, "he");
        const apply = (arr: UserBidSummaryItem[]) =>
          arr.sort((a, b) => (direction === "desc" ? -cmp(a, b) : cmp(a, b)));
        apply(active);
        apply(ended);
        return [...active, ...ended];
      },
      sortable: true,
      className: styles.tableCell,
    },
    {
      key: "currentPrice",
      header: "מחיר נוכחי",
      accessor: (bid: UserBidSummaryItem) => (
        <span className={styles.currentPrice}>
          {formatCurrency(bid.currentPrice)}
        </span>
      ),
      comparator: (a: UserBidSummaryItem, b: UserBidSummaryItem) =>
        (a.currentPrice || 0) - (b.currentPrice || 0),
      customSort: (rows: UserBidSummaryItem[], direction: SortDirection) => {
        const active: UserBidSummaryItem[] = [];
        const ended: UserBidSummaryItem[] = [];
        rows.forEach((r: UserBidSummaryItem) =>
          r.status === "active" ? active.push(r) : ended.push(r)
        );
        const cmp = (a: UserBidSummaryItem, b: UserBidSummaryItem) =>
          (a.currentPrice || 0) - (b.currentPrice || 0);
        const apply = (arr: UserBidSummaryItem[]) =>
          arr.sort((a, b) => {
            const primary = direction === "desc" ? -cmp(a, b) : cmp(a, b);
            if (primary !== 0) return primary;
            return a.auctionTitle.localeCompare(b.auctionTitle, "he");
          });
        apply(active);
        apply(ended);
        return [...active, ...ended];
      },
      sortable: true,
      className: styles.tableCell,
    },
    {
      key: "yourMax",
      header: "הסכום המרבי שלך",
      accessor: (bid: UserBidSummaryItem) => (
        <span className={styles.yourMaxBid}>{formatCurrency(bid.yourMax)}</span>
      ),
      comparator: (a: UserBidSummaryItem, b: UserBidSummaryItem) =>
        (a.yourMax || 0) - (b.yourMax || 0),
      customSort: (rows: UserBidSummaryItem[], direction: SortDirection) => {
        const active: UserBidSummaryItem[] = [];
        const ended: UserBidSummaryItem[] = [];
        rows.forEach((r: UserBidSummaryItem) =>
          r.status === "active" ? active.push(r) : ended.push(r)
        );
        const cmp = (a: UserBidSummaryItem, b: UserBidSummaryItem) =>
          (a.yourMax || 0) - (b.yourMax || 0);
        const apply = (arr: UserBidSummaryItem[]) =>
          arr.sort((a, b) => {
            const primary = direction === "desc" ? -cmp(a, b) : cmp(a, b);
            if (primary !== 0) return primary;
            return a.auctionTitle.localeCompare(b.auctionTitle, "he");
          });
        apply(active);
        apply(ended);
        return [...active, ...ended];
      },
      sortable: true,
      className: styles.tableCell,
    },
    {
      key: "timeRemaining",
      header: "זמן שנותר",
      accessor: (bid: UserBidSummaryItem) => (
        <span className={getTimeRemainingClass(bid.endDate, bid.status)}>
          {bid.status === "ended"
            ? "הסתיים"
            : calculateTimeRemaining(bid.endDate)}
        </span>
      ),
      comparator: (a: UserBidSummaryItem, b: UserBidSummaryItem) =>
        parseTimeRemaining(a.endDate) - parseTimeRemaining(b.endDate),
      customSort: (rows: UserBidSummaryItem[], direction: SortDirection) => {
        const active: UserBidSummaryItem[] = [];
        const ended: UserBidSummaryItem[] = [];
        rows.forEach((r: UserBidSummaryItem) =>
          r.status === "active" ? active.push(r) : ended.push(r)
        );
        active.sort(
          (a: UserBidSummaryItem, b: UserBidSummaryItem) =>
            parseTimeRemaining(a.endDate) - parseTimeRemaining(b.endDate)
        );
        if (direction === "desc") active.reverse();
        // ended stay by end_date desc
        ended.sort(
          (a: UserBidSummaryItem, b: UserBidSummaryItem) =>
            new Date(b.endDate).getTime() - new Date(a.endDate).getTime()
        );
        // tie-breaker inside groups by title asc
        active.sort((a: UserBidSummaryItem, b: UserBidSummaryItem) => {
          const t =
            parseTimeRemaining(a.endDate) - parseTimeRemaining(b.endDate);
          if ((direction === "desc" ? -t : t) !== 0)
            return direction === "desc" ? -t : t;
          return a.auctionTitle.localeCompare(b.auctionTitle, "he");
        });
        ended.sort((a: UserBidSummaryItem, b: UserBidSummaryItem) => {
          const t =
            new Date(b.endDate).getTime() - new Date(a.endDate).getTime();
          if (t !== 0) return t;
          return a.auctionTitle.localeCompare(b.auctionTitle, "he");
        });
        return [...active, ...ended];
      },
      sortable: true,
      className: styles.tableCell,
    },
    {
      key: "bidStatus",
      header: "סטטוס הצעה",
      accessor: (bid: UserBidSummaryItem) => (
        <span className={getBidStatusClass(bid)}>{getBidStatusText(bid)}</span>
      ),
      sortable: false,
      className: styles.tableCell,
    },
    {
      key: "action",
      header: "פעולה",
      accessor: (bid: UserBidSummaryItem) => (
        <button
          onClick={() => handleActionClick(bid)}
          className={styles.actionButton}
          aria-label={bid.status === "active" ? "פתח פרטי המכרז" : "לדף הבית"}
          title={bid.status === "active" ? "פתח פרטי המכרז" : "לדף הבית"}
        >
          {bid.status === "active" ? (
            <BidIcon className={styles.actionIcon} />
          ) : (
            <HomeIcon className={styles.actionIcon} />
          )}
        </button>
      ),
      sortable: false,
      className: styles.tableCell,
    },
  ];

  const defaultSort = "grouped" as const;

  const filterOptionsMapped: FilterOption[] = [
    { label: "הוצע סכום נמוך מידי", value: "outbid_low" },
    { label: "מוביל", value: "leading" },
    { label: "הפסדת", value: "lost" },
    { label: "זכית", value: "won" },
  ];

  return (
    <div className={styles.container}>
      {/* User menu */}
      <UserMenu />

      <div className={styles.headerSection}>
        <h1 className={styles.title}>ההצעות שלי</h1>
      </div>

      <SmartTable<UserBidSummaryItem>
        rows={bids}
        columns={columns}
        defaultSort={defaultSort}
        groupedSort={groupedSort}
        filterOptions={filterOptionsMapped}
        filterInColumnKey="bidStatus"
        getRowStatusValue={(b) => getStatusValue(b)}
      />
    </div>
  );
}
