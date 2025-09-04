import { toast } from "react-toastify";
import type { ToastOptions } from "react-toastify";

export type ToastType = "success" | "error" | "info" | "warning";

export const showToast = (
  message: string,
  type: ToastType,
  options?: ToastOptions
) => {
  toast[type](message, options);
};

export const showSuccessToast = (message: string, options?: ToastOptions) =>
  showToast(message, "success", options);

export const showErrorToast = (message: string, options?: ToastOptions) =>
  showToast(message, "error", options);
