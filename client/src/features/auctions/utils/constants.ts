export const MIN_TITLE_CHARS = 5;
export const MIN_DESC_CHARS = 20;

// Condition options (English storage keys with Hebrew labels)
export const CONDITION_OPTIONS = [
  { value: "new", label: "חדש" },
  { value: "like_new", label: "כמו חדש" },
  { value: "used", label: "משומש" },
  { value: "refurbished", label: "מחודש" },
  { value: "damaged_or_parts", label: "פגום/לחלקים" },
] as const;

// Validation messages
export const VALIDATION_MESSAGES = {
  required: "שדה חובה",
  minTitle: `יש להזין כותרת באורך מינימלי של ${MIN_TITLE_CHARS} תווים`,
  minDescription: `יש להזין תיאור באורך מינימלי של ${MIN_DESC_CHARS} תווים`,
  positiveNumber: "יש להזין מספר חיובי",
  maxDecimalPlaces: "מותרות עד שלוש ספרות אחרי הנקודה",
  endDateAfterStart: "תאריך הסיום חייב להיות אחרי תאריך ההתחלה",
} as const;

// Toast messages
export const TOAST_MESSAGES = {
  addSuccess: "הפריט נוסף בהצלחה",
  addError: "אירעה שגיאה בעת הוספת הפריט",
} as const;
