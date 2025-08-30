import type { CreateAuctionForm } from "./types";
import {
  MIN_TITLE_CHARS,
  MIN_DESC_CHARS,
  VALIDATION_MESSAGES,
} from "./constants";

export interface ValidationErrors {
  [key: string]: string;
}

export function validateDecimalNumber(value: string): string | null {
  if (!value.trim()) {
    return VALIDATION_MESSAGES.required;
  }

  const num = parseFloat(value);
  if (isNaN(num) || num <= 0) {
    return VALIDATION_MESSAGES.positiveNumber;
  }

  // Check decimal places (max 3)
  const decimalPart = value.split(".")[1];
  if (decimalPart && decimalPart.length > 3) {
    return VALIDATION_MESSAGES.maxDecimalPlaces;
  }

  return null;
}

export function validateForm(formData: CreateAuctionForm): ValidationErrors {
  const errors: ValidationErrors = {};

  // Title validation
  if (!formData.title.trim()) {
    errors.title = VALIDATION_MESSAGES.required;
  } else if (formData.title.trim().length < MIN_TITLE_CHARS) {
    errors.title = VALIDATION_MESSAGES.minTitle;
  }

  // Description validation
  if (!formData.description.trim()) {
    errors.description = VALIDATION_MESSAGES.required;
  } else if (formData.description.trim().length < MIN_DESC_CHARS) {
    errors.description = VALIDATION_MESSAGES.minDescription;
  }

  // Condition validation
  if (!formData.condition) {
    errors.condition = VALIDATION_MESSAGES.required;
  }

  // Categories validation (at least one required)
  if (!formData.categories || formData.categories.length === 0) {
    errors.categories = VALIDATION_MESSAGES.required;
  }

  // Min price validation
  const minPriceError = validateDecimalNumber(formData.minPrice);
  if (minPriceError) {
    errors.minPrice = minPriceError;
  }

  // Bid increment validation
  const bidIncrementError = validateDecimalNumber(formData.bidIncrement);
  if (bidIncrementError) {
    errors.bidIncrement = bidIncrementError;
  }

  // Start date validation
  if (!formData.startDate.trim()) {
    errors.startDate = VALIDATION_MESSAGES.required;
  }

  // End date validation
  if (!formData.endDate.trim()) {
    errors.endDate = VALIDATION_MESSAGES.required;
  } else if (formData.startDate.trim() && formData.endDate.trim()) {
    const startDate = new Date(formData.startDate);
    const endDate = new Date(formData.endDate);
    if (endDate <= startDate) {
      errors.endDate = VALIDATION_MESSAGES.endDateAfterStart;
    }
  }

  return errors;
}
