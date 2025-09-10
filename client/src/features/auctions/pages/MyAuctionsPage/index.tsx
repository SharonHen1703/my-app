import { useState, useEffect, useMemo, useRef } from "react";
import { getUserAuctions } from "../../api";
import type { UserAuctionItem } from "../../utils/types";
import { formatCurrency } from "../../../bids/utils/formatters";
import {
  calculateTimeRemaining,
  parseTimeRemaining,
} from "../../utils/timeUtils";
import AddItemModal from "../../components/AddItemModal";
import styles from "./index.module.css";
import { useAuth } from "../../../auth/useAuth";
import { UserMenu } from "../../../../components/common";

type SortColumn =
  | "title"
  | "currentPrice"
  | "bidsCount"
  | "timeRemaining"
  | "default";
type SortDirection = "asc" | "desc";
type AuctionStatus = "פעיל" | "הסתיים בהצלחה" | "הסתיים ללא זכייה";

// Status priority for sorting
const STATUS_PRIORITY = {
  פעיל: 1,
  "הסתיים בהצלחה": 2,
  "הסתיים ללא זכייה": 3,
} as const;

// All available statuses for filter
const ALL_STATUSES: AuctionStatus[] = [
  "פעיל",
  "הסתיים בהצלחה",
  "הסתיים ללא זכייה",
];

export default function MyAuctionsPage() {
  const [auctions, setAuctions] = useState<UserAuctionItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const { user } = useAuth();

  // Sorting state - default to prioritized sort (active by time remaining, non-active by end date)
  const [sortColumn, setSortColumn] = useState<SortColumn>("default");
  const [sortDirection, setSortDirection] = useState<SortDirection>("asc");

  // Status filter state - default all selected
  const [selectedStatuses, setSelectedStatuses] = useState<Set<AuctionStatus>>(
    new Set(ALL_STATUSES)
  );

  // Filter and sort auctions
  const filteredAndSortedAuctions = useMemo(() => {
    // First filter by status
    const filtered = auctions.filter((auction) =>
      selectedStatuses.has(auction.auctionStatus as AuctionStatus)
    );

    // Then sort
    filtered.sort((a, b) => {
      let comparison = 0;

      if (sortColumn === "default") {
        // Default prioritized sort
        // 1. Status priority: active → sold → unsold
        const statusA = a.auctionStatus as AuctionStatus;
        const statusB = b.auctionStatus as AuctionStatus;
        const statusComparison =
          STATUS_PRIORITY[statusA] - STATUS_PRIORITY[statusB];

        if (statusComparison !== 0) {
          comparison = statusComparison;
        } else {
          // Within same status group
          if (statusA === "פעיל") {
            // Active: sort by time remaining ascending (ends soonest first)
            comparison =
              parseTimeRemaining(a.endDate) - parseTimeRemaining(b.endDate);
          } else {
            // Non-active (sold/unsold): sort by end date descending (most recently ended first)
            // Handle edge cases: if end_date is missing, fall back to other dates
            const getEffectiveEndDate = (auction: UserAuctionItem) => {
              if (auction.endDate) {
                return new Date(auction.endDate).getTime();
              }
              // Fallback dates - you may need to add these fields to UserAuctionItem type
              // For now, using endDate as the only available field
              return 0; // Push items without dates to the bottom
            };

            const endDateA = getEffectiveEndDate(a);
            const endDateB = getEffectiveEndDate(b);
            comparison = endDateB - endDateA; // descending
          }
        }
      } else {
        // Regular column sorting
        switch (sortColumn) {
          case "title":
            comparison = a.title.localeCompare(b.title, "he");
            break;
          case "currentPrice":
            comparison = (a.currentPrice || 0) - (b.currentPrice || 0);
            break;
          case "bidsCount":
            comparison = a.bidsCount - b.bidsCount;
            break;
          case "timeRemaining":
            comparison =
              parseTimeRemaining(a.endDate) - parseTimeRemaining(b.endDate);
            break;
        }

        // Apply sort direction for regular columns
        if (sortDirection === "desc") {
          comparison = -comparison;
        }
      }

      // Stable sort: tie-break by title ascending
      if (comparison === 0 && sortColumn !== "title") {
        comparison = a.title.localeCompare(b.title, "he");
      }

      return comparison;
    });

    return filtered;
  }, [auctions, selectedStatuses, sortColumn, sortDirection]);

  // Sorting handlers
  const handleSort = (column: SortColumn, direction: SortDirection) => {
    setSortColumn(column);
    setSortDirection(direction);
  };

  // Status filter handlers
  const handleStatusToggle = (status: AuctionStatus) => {
    const newSelected = new Set(selectedStatuses);
    if (newSelected.has(status)) {
      newSelected.delete(status);
    } else {
      newSelected.add(status);
    }
    setSelectedStatuses(newSelected);
  };

  // Component for sortable headers
  const SortableHeader = ({
    children,
    column,
    className = "",
  }: {
    children: React.ReactNode;
    column: SortColumn;
    className?: string;
  }) => (
    <th className={`${styles.tableCell} ${className}`}>
      <div className={styles.sortableHeader}>
        <span>{children}</span>
        <div className={styles.sortIcons}>
          <button
            className={`${styles.sortIcon} ${
              sortColumn === column && sortDirection === "desc"
                ? styles.active
                : ""
            }`}
            onClick={() => handleSort(column, "desc")}
            title="מיון יורד"
            aria-label={`מיון ${children} יורד`}
          >
            <svg viewBox="0 0 12 6">
              <path d="M6 0L0 6h12L6 0z" />
            </svg>
          </button>
          <button
            className={`${styles.sortIcon} ${
              sortColumn === column && sortDirection === "asc"
                ? styles.active
                : ""
            }`}
            onClick={() => handleSort(column, "asc")}
            title="מיון עולה"
            aria-label={`מיון ${children} עולה`}
          >
            <svg viewBox="0 0 12 6">
              <path d="M6 6L0 0h12L6 6z" />
            </svg>
          </button>
        </div>
      </div>
    </th>
  );

  // Component for status filter header
  const StatusFilterHeader = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [menuPosition, setMenuPosition] = useState({ top: 0, left: 0 });
    const dropdownRef = useRef<HTMLDivElement>(null);
    const buttonRef = useRef<HTMLButtonElement>(null);

    // Calculate menu position when opening
    const handleToggleMenu = () => {
      if (!isOpen && buttonRef.current) {
        const buttonRect = buttonRef.current.getBoundingClientRect();
        setMenuPosition({
          top: buttonRect.bottom + window.scrollY + 4, // 4px gap below button
          left: buttonRect.right - 200 + window.scrollX, // Align right edge (200px is min-width)
        });
      }
      setIsOpen(!isOpen);
    };

    // Handle clicks outside dropdown to close it
    useEffect(() => {
      const handleClickOutside = (event: MouseEvent) => {
        if (
          dropdownRef.current &&
          !dropdownRef.current.contains(event.target as Node)
        ) {
          setIsOpen(false);
        }
      };

      const handleEscapeKey = (event: KeyboardEvent) => {
        if (event.key === "Escape") {
          setIsOpen(false);
        }
      };

      if (isOpen) {
        document.addEventListener("mousedown", handleClickOutside);
        document.addEventListener("keydown", handleEscapeKey);
      }

      return () => {
        document.removeEventListener("mousedown", handleClickOutside);
        document.removeEventListener("keydown", handleEscapeKey);
      };
    }, [isOpen]);

    return (
      <th className={styles.tableCell}>
        <div className={styles.sortableHeader}>
          <span>סטטוס מכרז</span>
          <div className={styles.filterDropdown} ref={dropdownRef}>
            <button
              ref={buttonRef}
              className={styles.filterButton}
              onClick={handleToggleMenu}
              aria-label="סנן לפי סטטוס"
            >
              סנן
              <svg width="12" height="8" viewBox="0 0 12 8" fill="currentColor">
                <path d="M6 8L0 2h12L6 8z" />
              </svg>
            </button>
            {isOpen && (
              <div
                className={styles.filterMenu}
                style={{
                  top: `${menuPosition.top}px`,
                  left: `${menuPosition.left}px`,
                }}
              >
                {ALL_STATUSES.map((status) => (
                  <label key={status} className={styles.filterOption}>
                    <input
                      type="checkbox"
                      className={styles.filterCheckbox}
                      checked={selectedStatuses.has(status)}
                      onChange={() => handleStatusToggle(status)}
                    />
                    <span>{status}</span>
                  </label>
                ))}
              </div>
            )}
          </div>
        </div>
      </th>
    );
  };

  useEffect(() => {
    if (user) {
      loadAuctions();
    }
  }, [user]);

  const handleAddItem = () => {
    setIsAddModalOpen(true);
  };

  const handleModalClose = () => {
    setIsAddModalOpen(false);
  };

  const handleAddSuccess = () => {
    // Refresh the auctions list
    loadAuctions();
  };

  const loadAuctions = async () => {
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

  const getStatusClass = (status: string) => {
    if (status === "פעיל") return styles.statusActive;
    if (status === "הסתיים בהצלחה") return styles.statusSold;
    if (status === "הסתיים ללא זכייה") return styles.statusUnsold;
    return styles.statusBadge;
  };

  const handleViewBidHistory = (auctionId: number) => {
    // Open bid history page in new tab
    window.open(`/auction/${auctionId}/bids`, "_blank");
  };

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

  return (
    <div className={styles.container}>
      <UserMenu />

      <div className={styles.headerSection}>
        <h1 className={styles.title}>המכרזים שלי</h1>
        <div className={styles.headerActions}>
          <button onClick={handleAddItem} className={styles.addItemButton}>
            הוסף מכרז
          </button>
        </div>
      </div>

      {auctions.length === 0 ? (
        <div className={styles.emptyState}>
          <h3>אין לך מכרזים פעילים כרגע</h3>
          <p>כשתפרסם מכרזים, הם יופיעו כאן</p>
        </div>
      ) : (
        <>
          {/* Header wrapper with reset button and table header */}
          <div className={styles.tableWrapper}>
            <table className={styles.table}>
              <thead className={styles.tableHeader}>
                <tr className={styles.tableRow}>
                  <SortableHeader column="title">מכרז</SortableHeader>
                  <SortableHeader column="currentPrice">
                    מחיר נוכחי
                  </SortableHeader>
                  <StatusFilterHeader />
                  <SortableHeader column="bidsCount">כמות הצעות</SortableHeader>
                  <SortableHeader column="timeRemaining">
                    זמן שנותר
                  </SortableHeader>
                </tr>
              </thead>

              {filteredAndSortedAuctions.length === 0 ? (
                <tbody>
                  <tr>
                    <td colSpan={5} className={styles.emptyResultsCell}>
                      <div className={styles.emptyResults}>
                        <p>לא נמצאו תוצאות העומדות בתנאי הסינון</p>
                        <p>נסה לשנות את הסינון או לבטל חלק מהקטגוריות</p>
                      </div>
                    </td>
                  </tr>
                </tbody>
              ) : (
                <tbody className={styles.tableBody}>
                  {filteredAndSortedAuctions.map((auction) => (
                    <tr key={auction.id} className={styles.tableRow}>
                      <td className={styles.tableCell}>
                        <a
                          href={`/auction/${auction.id}`}
                          target="_blank"
                          rel="noopener noreferrer"
                          className={styles.auctionTitle}
                        >
                          {auction.title}
                        </a>
                      </td>
                      <td className={styles.tableCell}>
                        <span className={styles.currentPrice}>
                          {formatCurrency(auction.currentPrice)}
                        </span>
                      </td>
                      <td className={styles.tableCell}>
                        <span
                          className={`${styles.statusBadge} ${getStatusClass(
                            auction.auctionStatus
                          )}`}
                        >
                          {auction.auctionStatus}
                        </span>
                      </td>
                      <td className={styles.tableCell}>
                        <button
                          onClick={() => handleViewBidHistory(auction.id)}
                          className={styles.bidsCountButton}
                        >
                          {auction.bidsCount} הצעות
                        </button>
                      </td>
                      <td className={styles.tableCell}>
                        <span className={styles.timeRemaining}>
                          {calculateTimeRemaining(auction.endDate)}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              )}
            </table>
          </div>
        </>
      )}

      <AddItemModal
        isOpen={isAddModalOpen}
        onClose={handleModalClose}
        onSuccess={handleAddSuccess}
      />
    </div>
  );
}
