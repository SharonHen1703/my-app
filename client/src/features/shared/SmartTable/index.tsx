import React, { useMemo, useRef, useEffect, useState } from "react";
import styles from "./index.module.css";

export type SortDirection = "asc" | "desc";

export type ColumnDef<T> = {
  key: string;
  header: string;
  accessor: (row: T) => React.ReactNode;
  comparator?: (a: T, b: T) => number;
  sortable?: boolean;
  className?: string;
  customSort?: (rows: T[], direction: SortDirection) => T[]; // optional column-specific sort
};

export type FilterOption = {
  label: string;
  value: string;
};

export type SmartTableProps<T> = {
  rows: T[];
  columns: ColumnDef<T>[];
  defaultSort?: { key: string; direction: SortDirection } | "grouped";
  groupedSort?: (a: T, b: T) => number; // for default grouped sort logic
  filterOptions?: FilterOption[];
  getRowStatusValue?: (row: T) => string; // used for filtering
  filterInColumnKey?: string; // which column header should host the filter UI
  onRowClick?: (row: T) => void;
};

export function SmartTable<T>(props: SmartTableProps<T>) {
  const {
    rows,
    columns,
    defaultSort = "grouped",
    groupedSort,
    filterOptions,
    getRowStatusValue,
    filterInColumnKey,
    onRowClick,
  } = props;

  const [sortKey, setSortKey] = useState<string | null>(
    defaultSort && defaultSort !== "grouped" ? defaultSort.key : null
  );
  const [sortDirection, setSortDirection] = useState<SortDirection>(
    defaultSort && defaultSort !== "grouped" ? defaultSort.direction : "asc"
  );

  const allFilterValues = useMemo(
    () =>
      filterOptions
        ? new Set(filterOptions.map((o) => o.value))
        : new Set<string>(),
    [filterOptions]
  );
  const [selectedFilters, setSelectedFilters] = useState<Set<string>>(
    new Set(allFilterValues)
  );

  useEffect(() => {
    // ensure defaults sync if options change
    setSelectedFilters(new Set(allFilterValues));
  }, [allFilterValues]);

  const [filterOpen, setFilterOpen] = useState(false);
  const [menuPosition, setMenuPosition] = useState({ top: 0, left: 0 });
  const dropdownRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    const onDocClick = (e: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target as Node)
      ) {
        setFilterOpen(false);
      }
    };
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") setFilterOpen(false);
      if (e.key === "Tab" && filterOpen && dropdownRef.current) {
        const focusables = dropdownRef.current.querySelectorAll<HTMLElement>(
          'input[type="checkbox"],button,[tabindex]:not([tabindex="-1"])'
        );
        if (focusables.length > 0) {
          const first = focusables[0];
          const last = focusables[focusables.length - 1];
          if (e.shiftKey && document.activeElement === first) {
            e.preventDefault();
            last.focus();
          } else if (!e.shiftKey && document.activeElement === last) {
            e.preventDefault();
            first.focus();
          }
        }
      }
    };
    if (filterOpen) {
      document.addEventListener("mousedown", onDocClick);
      document.addEventListener("keydown", onKeyDown);
    }
    return () => {
      document.removeEventListener("mousedown", onDocClick);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [filterOpen]);

  const toggleFilter = (val: string) => {
    const next = new Set(selectedFilters);
    if (next.has(val)) next.delete(val);
    else next.add(val);
    setSelectedFilters(next);
  };

  const handleToggleMenu = () => {
    if (!filterOpen && buttonRef.current) {
      const buttonRect = buttonRef.current.getBoundingClientRect();
      const scrollX = window.pageXOffset || document.documentElement.scrollLeft;
      const scrollY = window.pageYOffset || document.documentElement.scrollTop;

      setMenuPosition({
        top: buttonRect.bottom + scrollY + 4,
        left: buttonRect.right - 200 + scrollX, // Align right edge with menu width (200px)
      });
    }
    setFilterOpen(!filterOpen);
  };

  const filteredRows = useMemo(() => {
    if (!filterOptions || !getRowStatusValue) return rows;
    return rows.filter((r) => selectedFilters.has(getRowStatusValue(r)));
  }, [rows, filterOptions, getRowStatusValue, selectedFilters]);

  const sortedRows = useMemo(() => {
    const data = [...filteredRows];

    if (!sortKey && groupedSort) {
      data.sort(groupedSort);
      return data;
    }

    const col = columns.find((c) => c.key === sortKey);
    if (!col || !col.comparator) return data; // no sort when no comparator

    // Column-specific custom sort (e.g., timeRemaining grouping logic)
    if (col.customSort) {
      return col.customSort(data, sortDirection);
    }

    // Standard sort with stable tie-breaker (title asc if provided)
    const titleCol = columns.find((c) => c.key === "title");
    data.sort((a, b) => {
      const primary = col.comparator!(a, b);
      const applied = sortDirection === "desc" ? -primary : primary;
      if (applied !== 0) return applied;
      if (titleCol && titleCol.comparator) {
        return titleCol.comparator(a, b);
      }
      return 0;
    });

    return data;
  }, [filteredRows, sortKey, sortDirection, columns, groupedSort]);

  const setSort = (key: string, dir: SortDirection) => {
    setSortKey(key);
    setSortDirection(dir);
  };

  return (
    <div className={styles.wrapper}>
      <table className={styles.table}>
        <thead className={styles.tableHeader}>
          <tr className={styles.tableRow}>
            {columns.map((col) => (
              <th
                key={col.key}
                className={`${styles.tableCell} ${col.className ?? ""}`}
                aria-sort={
                  sortKey === col.key
                    ? sortDirection === "asc"
                      ? "ascending"
                      : "descending"
                    : "none"
                }
              >
                <div className={styles.sortableHeader}>
                  <span>{col.header}</span>
                  {col.sortable && col.comparator && (
                    <div className={styles.sortIcons}>
                      <button
                        className={`${styles.sortIcon} ${
                          sortKey === col.key && sortDirection === "desc"
                            ? styles.active
                            : ""
                        }`}
                        onClick={() => setSort(col.key, "desc")}
                        title="מיון יורד (גדול לקטן / ת-א)"
                        aria-label={`מיון ${col.header} יורד`}
                        aria-pressed={
                          sortKey === col.key && sortDirection === "desc"
                        }
                      >
                        <svg viewBox="0 0 12 6">
                          <path d="M6 0L0 6h12L6 0z" />
                        </svg>
                      </button>
                      <button
                        className={`${styles.sortIcon} ${
                          sortKey === col.key && sortDirection === "asc"
                            ? styles.active
                            : ""
                        }`}
                        onClick={() => setSort(col.key, "asc")}
                        title="מיון עולה (קטן לגדול / א-ת)"
                        aria-label={`מיון ${col.header} עולה`}
                        aria-pressed={
                          sortKey === col.key && sortDirection === "asc"
                        }
                      >
                        <svg viewBox="0 0 12 6">
                          <path d="M6 6L0 0h12L6 6z" />
                        </svg>
                      </button>
                    </div>
                  )}
                  {filterOptions && filterInColumnKey === col.key && (
                    <div className={styles.filterDropdown} ref={dropdownRef}>
                      <button
                        ref={buttonRef}
                        className={styles.filterButton}
                        onClick={handleToggleMenu}
                        aria-label="סנן לפי סטטוס"
                      >
                        סנן
                        <svg
                          width="12"
                          height="8"
                          viewBox="0 0 12 8"
                          fill="currentColor"
                        >
                          <path d="M6 8L0 2h12L6 8z" />
                        </svg>
                      </button>
                      {filterOpen &&
                        filterOptions &&
                        filterInColumnKey === col.key && (
                          <div
                            className={styles.filterMenu}
                            style={{
                              position: "fixed",
                              top: menuPosition.top,
                              left: menuPosition.left,
                              zIndex: 50,
                            }}
                            role="dialog"
                            aria-label="סינון לפי סטטוס"
                            aria-modal
                          >
                            {filterOptions.map((opt) => (
                              <label
                                key={opt.value}
                                className={styles.filterOption}
                              >
                                <input
                                  type="checkbox"
                                  className={styles.filterCheckbox}
                                  checked={selectedFilters.has(opt.value)}
                                  onChange={() => toggleFilter(opt.value)}
                                />
                                <span className={styles.filterOptionText}>
                                  {opt.label}
                                </span>
                              </label>
                            ))}
                          </div>
                        )}
                    </div>
                  )}
                </div>
              </th>
            ))}
          </tr>
        </thead>
        {sortedRows.length === 0 ? (
          <tbody>
            <tr>
              <td colSpan={columns.length} className={styles.emptyResultsCell}>
                <div className={styles.emptyResults}>
                  <p>לא נמצאו תוצאות העומדות בתנאי הסינון</p>
                  <p>נסה לשנות את הסינון או לבטל חלק מהקטגוריות</p>
                </div>
              </td>
            </tr>
          </tbody>
        ) : (
          <tbody className={styles.tableBody}>
            {sortedRows.map((row, idx) => (
              <tr
                key={idx}
                className={styles.tableRow}
                onClick={onRowClick ? () => onRowClick(row) : undefined}
              >
                {columns.map((col) => (
                  <td key={col.key} className={styles.tableCell}>
                    {col.accessor(row)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        )}
      </table>
    </div>
  );
}

export default SmartTable;
