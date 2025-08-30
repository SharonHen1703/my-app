import { useState, useEffect, useCallback } from "react";
import type { CreateAuctionForm } from "../../utils/types";
import { validateForm } from "../../utils/validation";
import { CONDITION_OPTIONS, TOAST_MESSAGES } from "../../utils/constants";
import { createAuction, fetchCategoriesMap } from "../../api";
import {
  showSuccessToast,
  showErrorToast,
} from "../../../../components/Toast/toastUtils";
import styles from "./index.module.css";

interface AddItemModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function AddItemModal({
  isOpen,
  onClose,
  onSuccess,
}: AddItemModalProps) {
  const [formData, setFormData] = useState<CreateAuctionForm>({
    title: "",
    description: "",
    condition: "",
    categories: [],
    minPrice: "",
    bidIncrement: "",
    startDate: "",
    endDate: "",
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [categoriesMap, setCategoriesMap] = useState<Record<string, string>>(
    {}
  );
  const [loadingCategories, setLoadingCategories] = useState(true);

  // Load categories on component mount
  useEffect(() => {
    async function loadCategories() {
      try {
        const categories = await fetchCategoriesMap();
        setCategoriesMap(categories);
      } catch (error) {
        console.error("Failed to load categories:", error);
      } finally {
        setLoadingCategories(false);
      }
    }

    if (isOpen) {
      loadCategories();
    }
  }, [isOpen]);

  // Focus trap and ESC handler
  useEffect(() => {
    if (!isOpen) return;

    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onClose();
      }
    };

    document.addEventListener("keydown", handleEscape);
    return () => document.removeEventListener("keydown", handleEscape);
  }, [isOpen, onClose]);

  const handleInputChange = useCallback(
    (field: keyof CreateAuctionForm, value: string) => {
      setFormData((prev) => ({
        ...prev,
        [field]: value,
      }));

      // Clear error when user starts typing
      if (errors[field]) {
        setErrors((prev) => ({
          ...prev,
          [field]: "",
        }));
      }
    },
    [errors]
  );

  const handleCategoryChange = useCallback(
    (categoryCode: string, checked: boolean) => {
      setFormData((prev) => ({
        ...prev,
        categories: checked
          ? [...prev.categories, categoryCode]
          : prev.categories.filter((c) => c !== categoryCode),
      }));

      // Clear categories error
      if (errors.categories) {
        setErrors((prev) => ({
          ...prev,
          categories: "",
        }));
      }
    },
    [errors.categories]
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validate form
    const validationErrors = validateForm(formData);
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      // Prepare request data
      const requestData = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        condition: formData.condition,
        categories: formData.categories,
        minPrice: parseFloat(formData.minPrice),
        bidIncrement: parseFloat(formData.bidIncrement),
        startDate: new Date(formData.startDate).toISOString(),
        endDate: new Date(formData.endDate).toISOString(),
        status: "active",
        bidsCount: 0,
      };

      await createAuction(requestData);

      // Success - show toast and close modal
      showSuccessToast(TOAST_MESSAGES.addSuccess);
      onSuccess();
      onClose();

      // Reset form
      setFormData({
        title: "",
        description: "",
        condition: "",
        categories: [],
        minPrice: "",
        bidIncrement: "",
        startDate: "",
        endDate: "",
      });
      setErrors({});
    } catch (error) {
      console.error("Failed to create auction:", error);
      showErrorToast(TOAST_MESSAGES.addError);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div
      className={styles.modal}
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div className={styles.modalContent}>
        <div className={styles.modalHeader}>
          <h2 className={styles.modalTitle}>הוסף פריט חדש</h2>
          <button
            type="button"
            className={styles.closeButton}
            onClick={onClose}
          >
            ×
          </button>
        </div>

        <form className={styles.form} onSubmit={handleSubmit}>
          {/* Title */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              כותרת הפריט <span className={styles.required}>*</span>
            </label>
            <input
              type="text"
              className={`${styles.input} ${errors.title ? styles.error : ""}`}
              value={formData.title}
              onChange={(e) => handleInputChange("title", e.target.value)}
              placeholder="הזן כותרת לפריט"
            />
            {errors.title && (
              <span className={styles.error}>{errors.title}</span>
            )}
          </div>

          {/* Description */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              תיאור הפריט <span className={styles.required}>*</span>
            </label>
            <textarea
              className={`${styles.textarea} ${
                errors.description ? styles.error : ""
              }`}
              value={formData.description}
              onChange={(e) => handleInputChange("description", e.target.value)}
              placeholder="תאר את הפריט בפירוט"
            />
            {errors.description && (
              <span className={styles.error}>{errors.description}</span>
            )}
          </div>

          {/* Condition */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              מצב הפריט <span className={styles.required}>*</span>
            </label>
            <select
              className={`${styles.select} ${
                errors.condition ? styles.error : ""
              }`}
              value={formData.condition}
              onChange={(e) => handleInputChange("condition", e.target.value)}
            >
              <option value="">בחר מצב</option>
              {CONDITION_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            {errors.condition && (
              <span className={styles.error}>{errors.condition}</span>
            )}
          </div>

          {/* Categories */}
          <div className={styles.formGroup}>
            <label className={styles.label}>
              קטגוריות <span className={styles.required}>*</span>
            </label>
            {loadingCategories ? (
              <div>טוען קטגוריות...</div>
            ) : (
              <div className={styles.checkboxGroup}>
                {Object.entries(categoriesMap).map(([code, label]) => (
                  <div key={code} className={styles.checkboxItem}>
                    <input
                      type="checkbox"
                      id={`category-${code}`}
                      className={styles.checkbox}
                      checked={formData.categories.includes(code)}
                      onChange={(e) =>
                        handleCategoryChange(code, e.target.checked)
                      }
                    />
                    <label
                      htmlFor={`category-${code}`}
                      className={styles.checkboxLabel}
                    >
                      {label}
                    </label>
                  </div>
                ))}
              </div>
            )}
            {errors.categories && (
              <span className={styles.error}>{errors.categories}</span>
            )}
          </div>

          {/* Prices */}
          <div className={styles.twoColumns}>
            <div className={styles.formGroup}>
              <label className={styles.label}>
                מחיר מינימלי לפתיחת המכרז{" "}
                <span className={styles.required}>*</span>
              </label>
              <input
                type="number"
                step="0.001"
                min="0"
                className={`${styles.input} ${
                  errors.minPrice ? styles.error : ""
                }`}
                value={formData.minPrice}
                onChange={(e) => handleInputChange("minPrice", e.target.value)}
                placeholder="0.00"
              />
              {errors.minPrice && (
                <span className={styles.error}>{errors.minPrice}</span>
              )}
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label}>
                תוספת בין הצעות (מינימום){" "}
                <span className={styles.required}>*</span>
              </label>
              <input
                type="number"
                step="0.001"
                min="0"
                className={`${styles.input} ${
                  errors.bidIncrement ? styles.error : ""
                }`}
                value={formData.bidIncrement}
                onChange={(e) =>
                  handleInputChange("bidIncrement", e.target.value)
                }
                placeholder="0.00"
              />
              {errors.bidIncrement && (
                <span className={styles.error}>{errors.bidIncrement}</span>
              )}
            </div>
          </div>

          {/* Dates - TODO: Replace with proper date pickers */}
          <div className={styles.twoColumns}>
            <div className={styles.formGroup}>
              <label className={styles.label}>
                תאריך התחלת המכרז <span className={styles.required}>*</span>
              </label>
              <input
                type="datetime-local"
                className={`${styles.input} ${
                  errors.startDate ? styles.error : ""
                }`}
                value={formData.startDate}
                onChange={(e) => handleInputChange("startDate", e.target.value)}
              />
              {errors.startDate && (
                <span className={styles.error}>{errors.startDate}</span>
              )}
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label}>
                תאריך סיום המכרז <span className={styles.required}>*</span>
              </label>
              <input
                type="datetime-local"
                className={`${styles.input} ${
                  errors.endDate ? styles.error : ""
                }`}
                value={formData.endDate}
                onChange={(e) => handleInputChange("endDate", e.target.value)}
              />
              {errors.endDate && (
                <span className={styles.error}>{errors.endDate}</span>
              )}
            </div>
          </div>

          <div className={styles.modalActions}>
            <button
              type="submit"
              className={styles.submitButton}
              disabled={isSubmitting || loadingCategories}
            >
              {isSubmitting ? "שומר..." : "שמור"}
            </button>
            <button
              type="button"
              className={styles.cancelButton}
              onClick={onClose}
              disabled={isSubmitting}
            >
              ביטול
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
