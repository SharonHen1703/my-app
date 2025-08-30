/**
 * Consolidated time utility functions for auctions
 */

/**
 * Calculate time remaining until auction ends
 * This is the single source of truth for time remaining calculations
 */
export function calculateTimeRemaining(endDate: string): string {
  const now = new Date();
  const end = new Date(endDate);
  const diffInMs = end.getTime() - now.getTime();

  if (diffInMs <= 0) {
    return "הסתיים";
  }

  const days = Math.floor(diffInMs / (1000 * 60 * 60 * 24));
  const hours = Math.floor(
    (diffInMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
  );
  const minutes = Math.floor((diffInMs % (1000 * 60 * 60)) / (1000 * 60));
  const seconds = Math.floor((diffInMs % (1000 * 60)) / 1000);

  if (days > 0) {
    // Show only days and hours for long periods
    return `${days} ימים${hours > 0 ? ` ו-${hours} שעות` : ""}`;
  } else if (hours > 0) {
    // Show hours and minutes for medium periods
    return `${hours} שעות${minutes > 0 ? ` ו-${minutes} דקות` : ""}`;
  } else if (minutes > 0) {
    // Show minutes and seconds for short periods
    return `${minutes} דקות${seconds > 0 ? ` ו-${seconds} שניות` : ""}`;
  } else {
    // Show seconds for very short periods
    return `${seconds} שניות`;
  }
}

export function isAuctionEnded(endDate: string): boolean {
  const now = new Date();
  const end = new Date(endDate);
  return end.getTime() <= now.getTime();
}

export function formatEndDate(endDate: string): string {
  const date = new Date(endDate);
  return date.toLocaleDateString("he-IL", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

/**
 * Check if auction is urgent (less than 24 hours remaining)
 */
export function isAuctionUrgent(endDate: string): boolean {
  const now = new Date();
  const end = new Date(endDate);
  const diffInMs = end.getTime() - now.getTime();
  const hoursRemaining = diffInMs / (1000 * 60 * 60);
  return hoursRemaining <= 24 && hoursRemaining > 0;
}

/**
 * Parse time remaining in milliseconds for sorting purposes
 * Returns negative value for ended auctions
 */
export function parseTimeRemaining(endDate: string): number {
  const now = new Date();
  const end = new Date(endDate);
  return end.getTime() - now.getTime();
}
