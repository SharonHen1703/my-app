import { useState, useEffect } from "react";
import { fetchCategoriesMap } from "../../api";
import styles from "./index.module.css";

interface CategoryFilterProps {
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
}

export default function CategoryFilter({
  selectedCategory,
  onCategoryChange,
}: CategoryFilterProps) {
  const [categoriesMap, setCategoriesMap] = useState<Record<string, string>>(
    {}
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadCategories = async () => {
      try {
        setLoading(true);
        const categoriesMapData = await fetchCategoriesMap();
        setCategoriesMap(categoriesMapData);
      } catch (err) {
        console.error("Failed to load categories:", err);
        setError("שגיאה בטעינת קטגוריות");
      } finally {
        setLoading(false);
      }
    };

    loadCategories();
  }, []);

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>טוען קטגוריות...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

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
