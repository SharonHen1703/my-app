const conditionTranslations: Record<string, string> = {
  NEW: "חדש",
  LIKE_NEW: "כמו חדש",
  USED: "משומש",
  REFURBISHED: "מחודש",
  DAMAGED_OR_PARTS: "פגום או לחלקים",
};

export const translateCondition = (condition: string): string => {
  return conditionTranslations[condition] || condition;
};
