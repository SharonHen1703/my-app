// תרגומים למצב הפריט
export const conditionTranslations: Record<string, string> = {
  // אותיות קטנות
  new: "חדש",
  like_new: "כמו חדש",
  used: "משומש",
  refurbished: "משופץ",
  damaged_or_parts: "פגום/חלקים",
  // אותיות גדולות
  NEW: "חדש",
  LIKE_NEW: "כמו חדש",
  USED: "משומש",
  REFURBISHED: "משופץ",
  DAMAGED_OR_PARTS: "פגום/חלקים",
};

export function translateCondition(condition: string): string {
  if (!condition) return "לא צוין";

  // נסה קודם עם הערך המקורי
  if (conditionTranslations[condition]) {
    return conditionTranslations[condition];
  }

  // נסה עם אותיות קטנות
  const lowerCase = condition.toLowerCase();
  if (conditionTranslations[lowerCase]) {
    return conditionTranslations[lowerCase];
  }

  // נסה עם אותיות גדולות
  const upperCase = condition.toUpperCase();
  if (conditionTranslations[upperCase]) {
    return conditionTranslations[upperCase];
  }

  // אם לא נמצא תרגום, החזר את הערך המקורי
  return condition;
}
