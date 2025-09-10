import { AUCTION_CATEGORIES } from "../../utils/categories";
import styles from "./index.module.css";

interface CategoryFilterProps {
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
}

export default function CategoryFilter({
  selectedCategory,
  onCategoryChange,
}: CategoryFilterProps) {
  // השתמש בקטגוריות הקבועות ישירות
  const categoriesMap = AUCTION_CATEGORIES;

  return (
    <div className={styles.container}>
      <div className={styles.filterSection}>
        <label htmlFor="category-select" className={styles.label}>
          סינון לפי קטגוריה:
        </label>
        <select
          id="category-select"
          value={selectedCategory}
          onChange={(e) => onCategoryChange(e.target.value)}
          className={styles.select}
        >
          <option value="">כל הקטגוריות</option>
          {Object.entries(categoriesMap).map(([code, hebrewName]) => (
            <option key={code} value={code}>
              {hebrewName}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}
