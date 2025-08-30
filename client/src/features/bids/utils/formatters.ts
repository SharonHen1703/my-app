/**
 * Format currency value to Hebrew Shekel format
 */
export function formatCurrency(amount: number | null): string {
  if (amount === null || amount === undefined) {
    return "לא זמין";
  }
  return `${amount.toLocaleString("he-IL")} ₪`;
}
