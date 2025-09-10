import { useState } from "react";
import styles from "./index.module.css";

interface FilterSidebarProps {
  categories: Record<string, string>;
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
  // Filters state controlled by parent
  minPrice?: number | null;
  maxPrice?: number | null;
  onPriceApply?: (min: number | null, max: number | null) => void;
  selectedConditions?: string[];
  onConditionsChange?: (conditions: string[]) => void;
  searchText?: string;
  onSearchChange?: (searchText: string) => void;
  loading?: boolean;
}

export default function FilterSidebar({
  categories,
  selectedCategory,
  onCategoryChange,
  minPrice = null,
  maxPrice = null,
  onPriceApply,
  selectedConditions = [],
  onConditionsChange,
  searchText = "",
  onSearchChange,
  loading = false,
}: FilterSidebarProps) {
  const [categoryExpanded, setCategoryExpanded] = useState(true);
  const [priceExpanded, setPriceExpanded] = useState(true);
  const [conditionExpanded, setConditionExpanded] = useState(true);
  const [showAllCategories, setShowAllCategories] = useState(false);
  const [minInput, setMinInput] = useState<string>(
    minPrice != null ? String(minPrice) : ""
  );
  const [maxInput, setMaxInput] = useState<string>(
    maxPrice != null ? String(maxPrice) : ""
  );
  const [priceError, setPriceError] = useState<string>("");
  const [pendingSearch, setPendingSearch] = useState<string>(searchText);

  // פונקציה לטיפול בהקלדה של מחיר - מאפשרת רק מספרים חיוביים
  const handlePriceInput = (value: string, setter: (value: string) => void) => {
    // מסיר תווים שאינם מספרים, נקודה או פסיק
    const cleanedValue = value.replace(/[^0-9.,]/g, "");

    // מחליף פסיק בנקודה לאחידות
    const normalizedValue = cleanedValue.replace(",", ".");

    // מוודא שיש רק נקודה אחת
    const parts = normalizedValue.split(".");
    let finalValue;
    if (parts.length > 2) {
      finalValue = parts[0] + "." + parts.slice(1).join("");
    } else {
      finalValue = normalizedValue;
    }

    // מוודא שהמספר לא שלילי (אם יש מינוס, מסיר אותו)
    finalValue = finalValue.replace("-", "");

    setter(finalValue);
  };

  // מספר הקטגוריות שיוצגו בהתחלה
  const INITIAL_CATEGORIES_COUNT = 6;

  if (loading) {
    return (
      <div className={styles.sidebar}>
        <div className={styles.loading}>טוען סינונים...</div>
      </div>
    );
  }

  return (
    <div className={styles.sidebar}>
      {/* Search Filter (button triggers search) */}
      <div className={styles.filterSection}>
        <div className={styles.searchRow}>
          <button
            type="button"
            className={styles.searchActionButton}
            onClick={() => onSearchChange?.(pendingSearch.trim())}
            title="בצע חיפוש"
          >
            חיפוש
          </button>
          <div className={styles.searchInputWrapper}>
            <input
              type="text"
              className={styles.searchField}
              placeholder="חפש פריט..."
              value={pendingSearch}
              onChange={(e) => setPendingSearch(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  onSearchChange?.(pendingSearch.trim());
                }
                if (e.key === "Escape") {
                  setPendingSearch("");
                }
              }}
            />
            {pendingSearch && (
              <button
                type="button"
                className={styles.clearInlineButton}
                onClick={() => setPendingSearch("")}
                aria-label="נקה חיפוש"
                title="נקה"
              >
                ×
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Category Filter */}
      <div className={styles.filterSection}>
        <button
          className={styles.filterHeader}
          onClick={() => setCategoryExpanded(!categoryExpanded)}
        >
          <span className={styles.filterTitle}>קטגוריות</span>
          <span className={styles.expandIcon}>
            {categoryExpanded ? "−" : "+"}
          </span>
        </button>

        {categoryExpanded && (
          <div className={styles.filterContent}>
            <div className={styles.filterOptions}>
              <label className={styles.filterOption}>
                <input
                  type="radio"
                  name="category"
                  value=""
                  checked={selectedCategory === ""}
                  onChange={() => onCategoryChange("")}
                  className={styles.radioInput}
                />
                <span className={styles.optionText}>הכל</span>
              </label>

              {Object.entries(categories)
                .slice(
                  0,
                  showAllCategories ? undefined : INITIAL_CATEGORIES_COUNT
                )
                .map(([code, hebrewName]) => (
                  <label key={code} className={styles.filterOption}>
                    <input
                      type="radio"
                      name="category"
                      value={code}
                      checked={selectedCategory === code}
                      onChange={() => onCategoryChange(code)}
                      className={styles.radioInput}
                    />
                    <span className={styles.optionText}>{hebrewName}</span>
                  </label>
                ))}

              {Object.entries(categories).length > INITIAL_CATEGORIES_COUNT && (
                <button
                  className={styles.toggleButton}
                  onClick={() => setShowAllCategories(!showAllCategories)}
                >
                  {showAllCategories ? (
                    <>
                      <span className={styles.toggleIcon}>−</span>
                      <span className={styles.toggleText}>פחות</span>
                    </>
                  ) : (
                    <>
                      <span className={styles.toggleIcon}>+</span>
                      <span className={styles.toggleText}>עוד</span>
                    </>
                  )}
                </button>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Price Filter */}
      <div className={styles.filterSection}>
        <button
          className={styles.filterHeader}
          onClick={() => setPriceExpanded(!priceExpanded)}
        >
          <span className={styles.filterTitle}>מחיר</span>
          <span className={styles.expandIcon}>{priceExpanded ? "−" : "+"}</span>
        </button>

        {priceExpanded && (
          <div className={styles.filterContent}>
            <div className={styles.priceRow}>
              <div className={styles.priceInput}>
                <label className={styles.priceLabel}>Min</label>
                <div className={styles.priceField}>
                  <span className={styles.currency}>ILS</span>
                  <input
                    inputMode="decimal"
                    pattern="[0-9]*[.,]?[0-9]*"
                    value={minInput}
                    onChange={(e) => {
                      handlePriceInput(e.target.value, setMinInput);
                      setPriceError("");
                    }}
                    onKeyDown={(e) => {
                      // מנע הקלדת תווים שליליים
                      if (e.key === "-" || e.key === "e" || e.key === "E") {
                        e.preventDefault();
                      }
                    }}
                    placeholder="0"
                  />
                </div>
              </div>
              <span className={styles.toText}>עד</span>
              <div className={styles.priceInput}>
                <label className={styles.priceLabel}>Max</label>
                <div className={styles.priceField}>
                  <span className={styles.currency}>ILS</span>
                  <input
                    inputMode="decimal"
                    pattern="[0-9]*[.,]?[0-9]*"
                    value={maxInput}
                    onChange={(e) => {
                      handlePriceInput(e.target.value, setMaxInput);
                      setPriceError("");
                    }}
                    onKeyDown={(e) => {
                      // מנע הקלדת תווים שליליים
                      if (e.key === "-" || e.key === "e" || e.key === "E") {
                        e.preventDefault();
                      }
                    }}
                    placeholder=""
                  />
                </div>
              </div>
              <button
                className={styles.priceApply}
                onClick={() => {
                  const minVal =
                    minInput.trim() === ""
                      ? null
                      : Number(minInput.replace(",", "."));
                  const maxVal =
                    maxInput.trim() === ""
                      ? null
                      : Number(maxInput.replace(",", "."));
                  if (minVal != null && maxVal != null && minVal > maxVal) {
                    setPriceError("המינימום גדול מהמקסימום");
                    return;
                  }
                  onPriceApply?.(
                    Number.isNaN(minVal as number) ? null : minVal,
                    Number.isNaN(maxVal as number) ? null : maxVal
                  );
                }}
                disabled={(() => {
                  const minVal =
                    minInput.trim() === ""
                      ? null
                      : Number(minInput.replace(",", "."));
                  const maxVal =
                    maxInput.trim() === ""
                      ? null
                      : Number(maxInput.replace(",", "."));
                  return minVal != null && maxVal != null && minVal > maxVal;
                })()}
                title="החל סינון מחיר"
              >
                ←
              </button>
            </div>
            {priceError && (
              <div className={styles.priceError}>{priceError}</div>
            )}
          </div>
        )}
      </div>

      {/* Condition Filter */}
      <div className={styles.filterSection}>
        <button
          className={styles.filterHeader}
          onClick={() => setConditionExpanded(!conditionExpanded)}
        >
          <span className={styles.filterTitle}>מצב</span>
          <span className={styles.expandIcon}>
            {conditionExpanded ? "−" : "+"}
          </span>
        </button>

        {conditionExpanded && (
          <div className={styles.filterContent}>
            <div className={styles.filterOptions}>
              {[
                { code: "new", label: "חדש" },
                { code: "like_new", label: "כמו חדש" },
                { code: "used", label: "משומש" },
                { code: "refurbished", label: "מחודש" },
                { code: "damaged_or_parts", label: "מקולקל/לחלקים" },
              ].map((opt) => (
                <label key={opt.code} className={styles.checkboxOption}>
                  <input
                    type="checkbox"
                    checked={selectedConditions.includes(opt.code)}
                    onChange={(e) => {
                      const next = new Set(selectedConditions);
                      if (e.target.checked) next.add(opt.code);
                      else next.delete(opt.code);
                      onConditionsChange?.(Array.from(next));
                    }}
                  />
                  <span className={styles.optionText}>{opt.label}</span>
                </label>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
