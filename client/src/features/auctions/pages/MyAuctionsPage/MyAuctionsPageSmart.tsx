import { useState, useEffect } from "react";
import { getUserAuctions } from "../../api";
import type { UserAuctionItem } from "../../utils/types";
import { formatCurrency } from "../../../bids/utils/formatters";
import {
  calculateTimeRemaining,
  parseTimeRemaining,
  isAuctionUrgent,
} from "../../utils/timeUtils";
import AddItemModal from "../../components/AddItemModal";
import SmartTable, {
  type ColumnDef,
  type FilterOption,
  type SortDirection,
} from "../../../shared/SmartTable";
import styles from "./index.module.css";
import { useAuth } from "../../../auth/useAuth";

type AuctionStatus = "פעיל" | "הסתיים בהצלחה" | "הסתיים ללא זכייה";

// Status priority for sorting
const STATUS_PRIORITY = {
  פעיל: 1,
  "הסתיים בהצלחה": 2,
  "הסתיים ללא זכייה": 3,
} as const;

export default function MyAuctionsPageSmart() {
  const [auctions, setAuctions] = useState<UserAuctionItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const { user } = useAuth();

  useEffect(() => {
    const loadAuctions = async () => {
      if (!user) return;

      try {
        setLoading(true);
        const data = await getUserAuctions();
        setAuctions(data);
      } catch (err) {
        console.error("Failed to load user auctions:", err);
        setError("שגיאה בטעינת המכרזים שלך");
      } finally {
        setLoading(false);
      }
    };

    loadAuctions();
  }, [user]);

  const getAuctionStatusClass = (status: AuctionStatus): string => {
    switch (status) {
      case "פעיל":
        return styles.statusActive;
      case "הסתיים בהצלחה":
        return styles.statusSold;
      case "הסתיים ללא זכייה":
        return styles.statusUnsold;
      default:
        return "";
    }
  };

  const getTimeRemainingClass = (
    endDate: string,
    status: AuctionStatus
  ): string => {
    if (status !== "פעיל") {
      return styles.timeRemaining;
    }
    return isAuctionUrgent(endDate)
      ? `${styles.timeRemaining} ${styles.urgent}`
      : styles.timeRemaining;
  };

  // SmartTable setup
  type StatusFilter = "active" | "sold" | "unsold";

  const getStatusValue = (auction: UserAuctionItem): StatusFilter => {
    switch (auction.auctionStatus) {
      case "פעיל":
        return "active";
      case "הסתיים בהצלחה":
        return "sold";
      case "הסתיים ללא זכייה":
        return "unsold";
      default:
        return "active";
    }
  };

  // Grouped default sort: active first by time remaining asc; non-active by endDate desc; tie-break title asc
  const groupedSort = (a: UserAuctionItem, b: UserAuctionItem): number => {
    const statusA = a.auctionStatus as AuctionStatus;
    const statusB = b.auctionStatus as AuctionStatus;

    // First by status priority
    const statusComparison =
      STATUS_PRIORITY[statusA] - STATUS_PRIORITY[statusB];
    if (statusComparison !== 0) return statusComparison;

    // Within same status group
    if (statusA === "פעיל") {
      // Active: sort by time remaining ascending (ends soonest first)
      const timeComparison =
        parseTimeRemaining(a.endDate) - parseTimeRemaining(b.endDate);
      if (timeComparison !== 0) return timeComparison;
    } else {
      // Non-active: sort by end date descending (most recently ended first)
      const endDateA = new Date(a.endDate).getTime();
      const endDateB = new Date(b.endDate).getTime();
      const timeComparison = endDateB - endDateA;
      if (timeComparison !== 0) return timeComparison;
    }

    // tie-break by title asc
    return a.title.localeCompare(b.title, "he");
  };

  const columns: ColumnDef<UserAuctionItem>[] = [
    {
      key: "title",
      header: "שם המוצר",
      accessor: (auction: UserAuctionItem) => (
        <a
          href={`/auction/${auction.id}`}
          target="_blank"
          rel="noopener noreferrer"
          className={styles.auctionTitle}
        >
          {auction.title}
        </a>
      ),
      comparator: (a: UserAuctionItem, b: UserAuctionItem) =>
        a.title.localeCompare(b.title, "he"),
      customSort: (rows: UserAuctionItem[], direction: SortDirection) => {
        const active: UserAuctionItem[] = [];
        const ended: UserAuctionItem[] = [];
        rows.forEach((r: UserAuctionItem) =>
          r.auctionStatus === "פעיל" ? active.push(r) : ended.push(r)
        );
        const cmp = (a: UserAuctionItem, b: UserAuctionItem) =>
          a.title.localeCompare(b.title, "he");
        const apply = (arr: UserAuctionItem[]) =>
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
      accessor: (auction: UserAuctionItem) => (
        <span className={styles.currentPrice}>
          {formatCurrency(auction.currentPrice)}
        </span>
      ),
      comparator: (a: UserAuctionItem, b: UserAuctionItem) =>
        (a.currentPrice || 0) - (b.currentPrice || 0),
      customSort: (rows: UserAuctionItem[], direction: SortDirection) => {
        const active: UserAuctionItem[] = [];
        const ended: UserAuctionItem[] = [];
        rows.forEach((r: UserAuctionItem) =>
          r.auctionStatus === "פעיל" ? active.push(r) : ended.push(r)
        );
        const cmp = (a: UserAuctionItem, b: UserAuctionItem) =>
          (a.currentPrice || 0) - (b.currentPrice || 0);
        const apply = (arr: UserAuctionItem[]) =>
          arr.sort((a, b) => {
            const primary = direction === "desc" ? -cmp(a, b) : cmp(a, b);
            if (primary !== 0) return primary;
            return a.title.localeCompare(b.title, "he");
          });
        apply(active);
        apply(ended);
        return [...active, ...ended];
      },
      sortable: true,
      className: styles.tableCell,
    },
    {
      key: "bidsCount",
      header: "מספר הצעות",
      accessor: (auction: UserAuctionItem) => (
        <span className={styles.bidsCount}>{auction.bidsCount}</span>
      ),
      comparator: (a: UserAuctionItem, b: UserAuctionItem) =>
        a.bidsCount - b.bidsCount,
      customSort: (rows: UserAuctionItem[], direction: SortDirection) => {
        const active: UserAuctionItem[] = [];
        const ended: UserAuctionItem[] = [];
        rows.forEach((r: UserAuctionItem) =>
          r.auctionStatus === "פעיל" ? active.push(r) : ended.push(r)
        );
        const cmp = (a: UserAuctionItem, b: UserAuctionItem) =>
          a.bidsCount - b.bidsCount;
        const apply = (arr: UserAuctionItem[]) =>
          arr.sort((a, b) => {
            const primary = direction === "desc" ? -cmp(a, b) : cmp(a, b);
            if (primary !== 0) return primary;
            return a.title.localeCompare(b.title, "he");
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
      accessor: (auction: UserAuctionItem) => (
        <span
          className={getTimeRemainingClass(
            auction.endDate,
            auction.auctionStatus as AuctionStatus
          )}
        >
          {auction.auctionStatus === "פעיל"
            ? calculateTimeRemaining(auction.endDate)
            : "הסתיים"}
        </span>
      ),
      comparator: (a: UserAuctionItem, b: UserAuctionItem) =>
        parseTimeRemaining(a.endDate) - parseTimeRemaining(b.endDate),
      customSort: (rows: UserAuctionItem[], direction: SortDirection) => {
        const active: UserAuctionItem[] = [];
        const ended: UserAuctionItem[] = [];
        rows.forEach((r: UserAuctionItem) =>
          r.auctionStatus === "פעיל" ? active.push(r) : ended.push(r)
        );
        active.sort(
          (a: UserAuctionItem, b: UserAuctionItem) =>
            parseTimeRemaining(a.endDate) - parseTimeRemaining(b.endDate)
        );
        if (direction === "desc") active.reverse();
        // ended stay by end_date desc
        ended.sort(
          (a: UserAuctionItem, b: UserAuctionItem) =>
            new Date(b.endDate).getTime() - new Date(a.endDate).getTime()
        );
        // tie-breaker inside groups by title asc
        active.sort((a: UserAuctionItem, b: UserAuctionItem) => {
          const t =
            parseTimeRemaining(a.endDate) - parseTimeRemaining(b.endDate);
          if ((direction === "desc" ? -t : t) !== 0)
            return direction === "desc" ? -t : t;
          return a.title.localeCompare(b.title, "he");
        });
        ended.sort((a: UserAuctionItem, b: UserAuctionItem) => {
          const t =
            new Date(b.endDate).getTime() - new Date(a.endDate).getTime();
          if (t !== 0) return t;
          return a.title.localeCompare(b.title, "he");
        });
        return [...active, ...ended];
      },
      sortable: true,
      className: styles.tableCell,
    },
    {
      key: "auctionStatus",
      header: "סטטוס מכרז",
      accessor: (auction: UserAuctionItem) => (
        <span
          className={`${styles.statusBadge} ${getAuctionStatusClass(
            auction.auctionStatus as AuctionStatus
          )}`}
        >
          {auction.auctionStatus}
        </span>
      ),
      sortable: false,
      className: styles.tableCell,
    },
  ];

  const defaultSort = "grouped" as const;

  const filterOptionsMapped: FilterOption[] = [
    { label: "פעיל", value: "active" },
    { label: "הסתיים בהצלחה", value: "sold" },
    { label: "הסתיים ללא זכייה", value: "unsold" },
  ];

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>
          <div className={styles.spinner}></div>
          <p>טוען את המכרזים שלך...</p>
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

  if (auctions.length === 0) {
    return (
      <div className={styles.container}>
        <div className={styles.headerSection}>
          <h1 className={styles.title}>המכרזים שלי</h1>
          <button
            type="button"
            onClick={() => setIsAddModalOpen(true)}
            className={styles.addButton}
          >
            הוספת מוצר חדש למכרז
          </button>
        </div>
        <div className={styles.emptyState}>
          <h3>אין לך מכרזים פעילים כרגע</h3>
          <p>כשתוסיף מוצרים למכרז, הם יופיעו כאן</p>
        </div>
        {isAddModalOpen && (
          <AddItemModal
            isOpen={isAddModalOpen}
            onClose={() => setIsAddModalOpen(false)}
            onSuccess={() => {
              setIsAddModalOpen(false);
              // Reload auctions
              window.location.reload();
            }}
          />
        )}
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.headerSection}>
        <h1 className={styles.title}>המכרזים שלי (Smart Table)</h1>
        <button
          type="button"
          onClick={() => setIsAddModalOpen(true)}
          className={styles.addButton}
        >
          הוספת מוצר חדש למכרז
        </button>
      </div>

      <SmartTable<UserAuctionItem>
        rows={auctions}
        columns={columns}
        defaultSort={defaultSort}
        groupedSort={groupedSort}
        filterOptions={filterOptionsMapped}
        filterInColumnKey="auctionStatus"
        getRowStatusValue={(auction) => getStatusValue(auction)}
      />

      {isAddModalOpen && (
        <AddItemModal
          isOpen={isAddModalOpen}
          onClose={() => setIsAddModalOpen(false)}
          onSuccess={() => {
            setIsAddModalOpen(false);
            // Reload auctions
            window.location.reload();
          }}
        />
      )}
    </div>
  );
}
