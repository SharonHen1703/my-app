/**
 * Utility functions for SmartTable sorting operations
 */

/**
 * Parse and normalize numeric values for sorting
 * Handles Hebrew formatted numbers with thousands separators
 */
export function parseNumericValue(
  value: number | string | null | undefined
): number {
  if (value === null || value === undefined) {
    return -Infinity; // Push null/undefined to bottom
  }

  if (typeof value === "number") {
    return isNaN(value) ? -Infinity : value;
  }

  // Handle string values (strip formatting)
  const cleanStr = String(value)
    .replace(/[,\s]/g, "") // Remove thousands separators and whitespace
    .replace(/[^\d.-]/g, ""); // Keep only digits, dots, and minus

  const parsed = parseFloat(cleanStr);
  return isNaN(parsed) ? -Infinity : parsed;
}

/**
 * Parse time remaining in milliseconds for sorting
 * Returns negative values for ended auctions (they sort by end date instead)
 */
export function parseTimeRemainingMs(endDate: string): number {
  const now = new Date();
  const end = new Date(endDate);
  const diffMs = end.getTime() - now.getTime();

  // For ended auctions, return a large negative number so they sort separately
  return diffMs <= 0 ? -Infinity : diffMs;
}

/**
 * Hebrew-aware string comparison for titles
 */
export function compareHebrewStrings(a: string, b: string): number {
  return a.localeCompare(b, "he-IL", {
    sensitivity: "base",
    ignorePunctuation: true,
    numeric: true,
  });
}

/**
 * Create a numeric comparator that handles null/undefined values and formatting
 */
export function createNumericComparator<T>(
  accessor: (item: T) => number | string | null | undefined
) {
  return (a: T, b: T): number => {
    const valA = parseNumericValue(accessor(a));
    const valB = parseNumericValue(accessor(b));
    return valA - valB;
  };
}

/**
 * Create a time-remaining comparator for auction sorting
 * Active auctions sort by time remaining, ended auctions sort by end date descending
 */
export function createTimeRemainingComparator<T>(
  endDateAccessor: (item: T) => string,
  statusAccessor: (item: T) => string
) {
  return (a: T, b: T): number => {
    const statusA = statusAccessor(a);
    const statusB = statusAccessor(b);

    // Both active - sort by time remaining ascending (soonest first)
    if (statusA === "active" && statusB === "active") {
      const timeA = parseTimeRemainingMs(endDateAccessor(a));
      const timeB = parseTimeRemainingMs(endDateAccessor(b));
      return timeA - timeB;
    }

    // Both ended - sort by end date descending (most recent first)
    if (statusA === "ended" && statusB === "ended") {
      const endA = new Date(endDateAccessor(a)).getTime();
      const endB = new Date(endDateAccessor(b)).getTime();
      return endB - endA;
    }

    // Active before ended
    if (statusA === "active" && statusB === "ended") return -1;
    if (statusA === "ended" && statusB === "active") return 1;

    return 0;
  };
}

/**
 * Create a custom sort function that maintains active-before-ended invariant
 */
export function createGroupedSort<T>(
  statusAccessor: (item: T) => string,
  comparator: (a: T, b: T) => number,
  titleComparator?: (a: T, b: T) => number
) {
  return (rows: T[], direction: "asc" | "desc"): T[] => {
    const active: T[] = [];
    const ended: T[] = [];

    // Group by status
    rows.forEach((row) => {
      if (statusAccessor(row) === "active") {
        active.push(row);
      } else {
        ended.push(row);
      }
    });

    // Sort each group
    const sortFn = (a: T, b: T) => {
      const primary = comparator(a, b);
      const applied = direction === "desc" ? -primary : primary;

      // Tie-break with title if available
      if (applied === 0 && titleComparator) {
        return titleComparator(a, b);
      }

      return applied;
    };

    active.sort(sortFn);
    ended.sort(sortFn);

    return [...active, ...ended];
  };
}
