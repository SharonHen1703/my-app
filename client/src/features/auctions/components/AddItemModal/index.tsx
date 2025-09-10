import { useState, useEffect, useCallback } from "react";
import type { CreateAuctionForm } from "../../utils/types";
import { validateForm } from "../../utils/validation";
import { CONDITION_OPTIONS, TOAST_MESSAGES } from "../../utils/constants";
import { AUCTION_CATEGORIES } from "../../utils/categories";
import { createAuction } from "../../api";
import { showSuccessToast, showErrorToast } from "../../../shared/ui/Toast";
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
  const [selectedImages, setSelectedImages] = useState<File[]>([]);
  const [imagePreviewUrls, setImagePreviewUrls] = useState<string[]>([]);

  // השתמש בקטגוריות הקבועות
  const categoriesMap = AUCTION_CATEGORIES;

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

  // נקה URLs כאשר המודל נסגר
  useEffect(() => {
    return () => {
      // נקה את כל ה-preview URLs כאשר הקומפוננט מתבטל
      imagePreviewUrls.forEach((url) => URL.revokeObjectURL(url));
    };
  }, [imagePreviewUrls]);

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

  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    // בדוק גודל קבצים (מקסימום 5MB לכל קובץ)
    const maxSize = 5 * 1024 * 1024; // 5MB
    const invalidFiles = files.filter((file) => file.size > maxSize);

    if (invalidFiles.length > 0) {
      setErrors((prev) => ({
        ...prev,
        images: `הקבצים הבאים גדולים מדי (מקסימום 5MB): ${invalidFiles
          .map((f) => f.name)
          .join(", ")}`,
      }));
      return;
    }

    // בדוק סוג קבצים (רק תמונות)
    const validTypes = ["image/jpeg", "image/jpg", "image/png", "image/webp"];
    const invalidTypes = files.filter(
      (file) => !validTypes.includes(file.type)
    );

    if (invalidTypes.length > 0) {
      setErrors((prev) => ({
        ...prev,
        images: `סוג קבצים לא נתמך. אנא העלה רק תמונות (JPG, PNG, WEBP): ${invalidTypes
          .map((f) => f.name)
          .join(", ")}`,
      }));
      return;
    }

    // הוסף תמונות לרשימה הקיימת (מקסימום 10 תמונות)
    const currentImages = [...selectedImages];
    const newImages = files.filter(
      (file) =>
        !currentImages.some(
          (existing) =>
            existing.name === file.name && existing.size === file.size
        )
    );

    const totalImages = currentImages.length + newImages.length;
    if (totalImages > 10) {
      setErrors((prev) => ({
        ...prev,
        images: `ניתן להעלות מקסימום 10 תמונות. כרגע יש ${currentImages.length} תמונות.`,
      }));
      return;
    }

    const updatedImages = [...currentImages, ...newImages];
    setSelectedImages(updatedImages);

    // צור URL לתצוגה מקדימה
    const newPreviewUrls = newImages.map((file) => URL.createObjectURL(file));
    setImagePreviewUrls((prev) => [...prev, ...newPreviewUrls]);

    // נקה שגיאות תמונות
    if (errors.images) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors.images;
        return newErrors;
      });
    }
  };

  const removeImage = (index: number) => {
    const updatedImages = selectedImages.filter((_, i) => i !== index);
    const updatedPreviews = imagePreviewUrls.filter((_, i) => i !== index);

    // שחרר זיכרון של URL המוסר
    URL.revokeObjectURL(imagePreviewUrls[index]);

    setSelectedImages(updatedImages);
    setImagePreviewUrls(updatedPreviews);
  };

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

      console.log("🚀 Creating auction with data:", requestData);
      console.log("📷 Images to upload:", selectedImages.length);

      // Pass the actual File objects to the API
      await createAuction(requestData, selectedImages);

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

      // נקה תמונות
      imagePreviewUrls.forEach((url) => URL.revokeObjectURL(url));
      setSelectedImages([]);
      setImagePreviewUrls([]);
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
          <h2 className={styles.modalTitle}>הוסף מכרז חדש</h2>
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
              כותרת <span className={styles.required}>*</span>
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
              תיאור <span className={styles.required}>*</span>
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

          {/* Images */}
          <div className={styles.formGroup}>
            <label className={styles.label}>תמונות פריט (אופציונלי)</label>

            <div className={styles.imageUpload}>
              <input
                type="file"
                id="imageUpload"
                accept="image/jpeg,image/jpg,image/png,image/webp"
                multiple
                onChange={handleImageSelect}
                className={styles.imageInput}
              />
              <label htmlFor="imageUpload" className={styles.imageUploadLabel}>
                <div className={styles.uploadIcon}>📷</div>
                <div className={styles.uploadText}>
                  {selectedImages.length === 0
                    ? "לחץ להעלאת תמונות"
                    : `${selectedImages.length} תמונות נבחרו`}
                </div>
                <div className={styles.uploadHint}>
                  JPG, PNG, WEBP - מקסימום 10 תמונות, 5MB לכל תמונה
                </div>
              </label>
            </div>

            {errors.images && (
              <span className={styles.error}>{errors.images}</span>
            )}

            {imagePreviewUrls.length > 0 && (
              <div className={styles.imagePreview}>
                {imagePreviewUrls.map((url, index) => (
                  <div key={index} className={styles.previewItem}>
                    <img
                      src={url}
                      alt={`תמונה ${index + 1}`}
                      className={styles.previewImage}
                    />
                    <button
                      type="button"
                      onClick={() => removeImage(index)}
                      className={styles.removeImageButton}
                      title="הסר תמונה"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
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
              disabled={isSubmitting}
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
