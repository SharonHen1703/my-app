import { useRef, useState } from "react";
import { signup } from "../api";
import { showErrorToast, showSuccessToast } from "../../shared/ui/Toast";
import styles from "./auth.module.css";
import { Link, useNavigate } from "react-router-dom";

const MIN_FULL_NAME_CHARS = 2;
const MIN_PASSWORD_CHARS = 8;

export default function SignupPage() {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [confirm, setConfirm] = useState("");
  const [showConfirm, setShowConfirm] = useState(false);
  const [confirmError, setConfirmError] = useState<string | null>(null);
  const [phone, setPhone] = useState("");
  const [loading, setLoading] = useState(false);
  const [submitAttempted, setSubmitAttempted] = useState(false);
  const [fullNameError, setFullNameError] = useState<string | null>(null);
  const [emailError, setEmailError] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [phoneError, setPhoneError] = useState<string | null>(null);

  const navigate = useNavigate();
  const fullNameRef = useRef<HTMLInputElement>(null);
  const emailRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);
  const confirmRef = useRef<HTMLInputElement>(null);

  // Submit-only invalid state for required fields
  const fullNameInvalid =
    (submitAttempted && !fullName.trim()) || !!fullNameError;
  const emailInvalid = (submitAttempted && !email.trim()) || !!emailError;
  const passwordInvalid =
    (submitAttempted && !password.trim()) || !!passwordError;
  const confirmInvalid =
    (submitAttempted &&
      (!confirm.trim() || confirm.trim() !== password.trim())) ||
    !!confirmError;

  function validateConfirm(value: string) {
    const pwd = password.trim();
    const conf = value.trim();
    // Only show inline error for mismatch on blur; emptiness handled on submit visuals
    if (!conf) {
      if (confirmError !== null) setConfirmError(null);
      return false;
    }
    if (pwd !== conf) {
      if (confirmError !== "הסיסמאות אינן תואמות")
        setConfirmError("הסיסמאות אינן תואמות");
      return false;
    }
    if (confirmError !== null) setConfirmError(null);
    return true;
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitAttempted(true);

    // Clear existing errors
    setFullNameError(null);
    setEmailError(null);
    setPasswordError(null);
    setPhoneError(null);

    let hasError = false;

    // Check full name - show "required" message for empty field
    if (!fullName.trim()) {
      setFullNameError("שדה חובה");
      hasError = true;
    } else if (fullName.trim().length < MIN_FULL_NAME_CHARS) {
      setFullNameError(
        `השם המלא חייב להיות באורך של לפחות ${MIN_FULL_NAME_CHARS} תווים`
      );
      hasError = true;
    }

    // Check email - show "required" message for empty field
    if (!email.trim()) {
      setEmailError("שדה חובה");
      hasError = true;
    }

    // Check password - show "required" message for empty field
    if (!password.trim()) {
      setPasswordError("שדה חובה");
      hasError = true;
    } else if (password.trim().length < MIN_PASSWORD_CHARS) {
      setPasswordError(
        `הסיסמה חייבת להיות באורך של לפחות ${MIN_PASSWORD_CHARS} תווים`
      );
      hasError = true;
    }

    // Check confirm - show "required" message for empty field
    if (!confirm.trim()) {
      setConfirmError("שדה חובה");
      hasError = true;
    } else if (confirm.trim() !== password.trim()) {
      setConfirmError("הסיסמאות אינן תואמות");
      hasError = true;
    }

    // Check phone (optional but if provided, must be 10 digits)
    if (phone.trim() && !/^\d{10}$/.test(phone.trim())) {
      setPhoneError("מספר הטלפון חייב להכיל בדיוק 10 ספרות");
      hasError = true;
    }

    if (hasError) {
      // Focus and scroll to first invalid field
      let firstInvalidField = null;
      if (!fullName.trim() || fullName.trim().length < MIN_FULL_NAME_CHARS) {
        firstInvalidField = fullNameRef.current;
      } else if (!email.trim()) {
        firstInvalidField = emailRef.current;
      } else if (
        !password.trim() ||
        password.trim().length < MIN_PASSWORD_CHARS
      ) {
        firstInvalidField = passwordRef.current;
      } else if (!confirm.trim() || confirm.trim() !== password.trim()) {
        firstInvalidField = confirmRef.current;
      }

      if (firstInvalidField) {
        firstInvalidField.focus();
        firstInvalidField.scrollIntoView({
          behavior: "smooth",
          block: "center",
        });
      }
      return;
    }

    setLoading(true);
    try {
      await signup({
        fullName: fullName.trim(),
        email: email.trim(),
        password: password.trim(),
        phone: phone.trim() || undefined,
      });
      showSuccessToast("ההרשמה הושלמה בהצלחה");
      navigate("/login");
    } catch (error: unknown) {
      if (error && typeof error === "object" && "response" in error) {
        const response = (
          error as {
            response?: {
              status?: number;
              data?: {
                errors?: Array<{ field: string; defaultMessage: string }>;
              };
            };
          }
        ).response;
        if (response?.status === 409) {
          // Duplicate email - show inline error instead of toast
          setEmailError("האימייל כבר קיים במערכת");
          // Focus email field only if it's the first invalid field
          let firstInvalidField = null;
          if (
            !fullName.trim() ||
            fullName.trim().length < MIN_FULL_NAME_CHARS
          ) {
            firstInvalidField = fullNameRef.current;
          } else {
            firstInvalidField = emailRef.current;
          }
          if (firstInvalidField) {
            firstInvalidField.focus();
            firstInvalidField.scrollIntoView({
              behavior: "smooth",
              block: "center",
            });
          }
        } else if (response?.status === 400 && response?.data?.errors) {
          const errors = response.data.errors;
          errors.forEach((err: { field: string; defaultMessage: string }) => {
            const field = err.field;
            const message = err.defaultMessage;
            switch (field) {
              case "fullName":
                setFullNameError(message);
                break;
              case "email":
                setEmailError(message);
                break;
              case "password":
                setPasswordError(message);
                break;
              case "phone":
                setPhoneError(message);
                break;
            }
          });
        } else {
          showErrorToast("שגיאה בהרשמה. אנא נסה שוב.");
        }
      } else {
        showErrorToast("שגיאה בהרשמה. אנא נסה שוב.");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className={styles.wrapper} dir="rtl">
      <h1 className={styles.title}>הרשמה</h1>
      <form onSubmit={onSubmit} className={styles.form} noValidate>
        <label className={styles.label}>
          <span className={styles.labelText}>
            שם מלא{" "}
            <span
              className={`${styles.required} ${
                fullNameError || fullNameInvalid ? styles.requiredError : ""
              }`}
              aria-hidden="true"
            >
              *
            </span>
          </span>
          <input
            ref={fullNameRef}
            id="signup-fullname-input"
            className={`${styles.input} ${styles.inputRtl} ${
              fullNameError || fullNameInvalid ? styles.inputError : ""
            }`}
            value={fullName}
            onChange={(e) => {
              setFullName(e.target.value);
              if (fullNameError) setFullNameError(null);
            }}
            required
            aria-required="true"
            aria-invalid={fullNameInvalid ? "true" : undefined}
            aria-describedby={
              fullNameError || (submitAttempted && !fullName.trim())
                ? "fullname-error"
                : undefined
            }
          />
          {(fullNameError || (submitAttempted && !fullName.trim())) && (
            <span id="fullname-error" className={styles.errorText}>
              {fullNameError || "שדה חובה"}
            </span>
          )}
        </label>

        <label className={styles.label}>
          <span className={styles.labelText}>
            אימייל{" "}
            <span
              className={`${styles.required} ${
                emailError || emailInvalid ? styles.requiredError : ""
              }`}
              aria-hidden="true"
            >
              *
            </span>
          </span>
          <input
            ref={emailRef}
            id="signup-email-input"
            className={`${styles.input} ${styles.inputRtl} ${
              emailError || emailInvalid ? styles.inputError : ""
            }`}
            type="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              if (emailError) setEmailError(null);
            }}
            required
            aria-required="true"
            aria-invalid={emailInvalid ? "true" : undefined}
            aria-describedby={
              emailError || (submitAttempted && !email.trim())
                ? "email-error"
                : undefined
            }
          />
          {(emailError || (submitAttempted && !email.trim())) && (
            <span id="email-error" className={styles.errorText}>
              {emailError || "שדה חובה"}
            </span>
          )}
        </label>

        <label className={styles.label}>
          <span className={styles.labelText}>
            סיסמה{" "}
            <span
              className={`${styles.required} ${
                passwordError || passwordInvalid ? styles.requiredError : ""
              }`}
              aria-hidden="true"
            >
              *
            </span>
          </span>
          <div className={styles.passwordWrapper}>
            <input
              ref={passwordRef}
              id="signup-password-input"
              className={`${styles.input} ${styles.inputRtl} ${
                passwordError || passwordInvalid ? styles.inputError : ""
              }`}
              type={showPassword ? "text" : "password"}
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                if (passwordError) setPasswordError(null);
                if (confirmError && confirm.trim()) validateConfirm(confirm);
              }}
              required
              minLength={8}
              aria-required="true"
              aria-describedby={
                passwordError || (submitAttempted && !password.trim())
                  ? "password-error"
                  : "password-required"
              }
              aria-invalid={passwordInvalid ? "true" : undefined}
            />
            <button
              type="button"
              className={styles.passwordToggle}
              onMouseDown={(e) => e.preventDefault()}
              onClick={() => {
                const input = passwordRef.current;
                let start: number | null = null;
                let end: number | null = null;
                try {
                  start = input?.selectionStart ?? null;
                  end = input?.selectionEnd ?? null;
                } catch {
                  // ignore selection access errors
                }
                setShowPassword((prev) => !prev);
                setTimeout(() => {
                  if (input) {
                    input.focus({ preventScroll: true });
                    if (start !== null && end !== null) {
                      try {
                        input.setSelectionRange(start, end);
                      } catch {
                        // ignore selection set errors
                      }
                    }
                  }
                }, 0);
              }}
              aria-label={showPassword ? "הסתר סיסמה" : "הצג סיסמה"}
              title={showPassword ? "הסתר סיסמה" : "הצג סיסמה"}
              aria-pressed={showPassword}
              tabIndex={0}
            >
              {showPassword ? (
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                  <line x1="1" y1="1" x2="23" y2="23" />
                </svg>
              ) : (
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
              )}
            </button>
            <span id="password-required" className={styles["sr-only"]}>
              שדה חובה
            </span>
          </div>
          {(passwordError || (submitAttempted && !password.trim())) && (
            <span id="password-error" className={styles.errorText}>
              {passwordError || "שדה חובה"}
            </span>
          )}
        </label>

        <label className={styles.label}>
          <span className={styles.labelText}>
            אימות סיסמה{" "}
            <span
              className={`${styles.required} ${
                confirmError || confirmInvalid ? styles.requiredError : ""
              }`}
              aria-hidden="true"
            >
              *
            </span>
          </span>
          <div className={styles.passwordWrapper}>
            <input
              ref={confirmRef}
              id="signup-confirm-input"
              className={`${styles.input} ${styles.inputRtl} ${
                confirmError || confirmInvalid ? styles.inputError : ""
              }`}
              type={showConfirm ? "text" : "password"}
              value={confirm}
              onChange={(e) => {
                const v = e.target.value;
                setConfirm(v);
                // Clear inline mismatch error as user corrects
                if (confirmError) validateConfirm(v);
              }}
              onBlur={(e) => {
                // Show mismatch error on blur (exception case)
                const v = e.target.value;
                if (v.trim() && v.trim() !== password.trim()) {
                  setConfirmError("הסיסמאות אינן תואמות");
                } else {
                  setConfirmError(null);
                }
              }}
              required
              minLength={8}
              aria-required="true"
              aria-invalid={
                confirmError || (submitAttempted && !confirm.trim())
                  ? "true"
                  : undefined
              }
              aria-describedby={
                confirmError || (submitAttempted && !confirm.trim())
                  ? "confirm-required confirmPasswordError"
                  : "confirm-required"
              }
            />
            <button
              type="button"
              className={styles.passwordToggle}
              onMouseDown={(e) => e.preventDefault()}
              onClick={() => {
                const input = confirmRef.current;
                let start: number | null = null;
                let end: number | null = null;
                try {
                  start = input?.selectionStart ?? null;
                  end = input?.selectionEnd ?? null;
                } catch {
                  // ignore selection access errors
                }
                setShowConfirm((prev) => !prev);
                setTimeout(() => {
                  if (input) {
                    input.focus({ preventScroll: true });
                    if (start !== null && end !== null) {
                      try {
                        input.setSelectionRange(start, end);
                      } catch {
                        // ignore selection set errors
                      }
                    }
                  }
                }, 0);
              }}
              aria-label={showConfirm ? "הסתר אימות סיסמה" : "הצג אימות סיסמה"}
              title={showConfirm ? "הסתר אימות סיסמה" : "הצג אימות סיסמה"}
              aria-pressed={showConfirm}
              tabIndex={0}
            >
              {showConfirm ? (
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                  <line x1="1" y1="1" x2="23" y2="23" />
                </svg>
              ) : (
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
              )}
            </button>
            <span id="confirm-required" className={styles["sr-only"]}>
              שדה חובה
            </span>
          </div>
          {(confirmError || (submitAttempted && !confirm.trim())) && (
            <span id="confirmPasswordError" className={styles.errorText}>
              {confirmError || "שדה חובה"}
            </span>
          )}
        </label>

        <label className={styles.label}>
          טלפון
          <input
            id="signup-phone-input"
            className={`${styles.input} ${styles.inputRtl} ${
              phoneError ? styles.inputError : ""
            }`}
            type="tel"
            value={phone}
            onChange={(e) => {
              setPhone(e.target.value);
              if (phoneError) setPhoneError(null);
            }}
            aria-invalid={phoneError ? "true" : undefined}
            aria-describedby={phoneError ? "phone-error" : undefined}
          />
          {phoneError && (
            <span id="phone-error" className={styles.errorText}>
              {phoneError}
            </span>
          )}
        </label>

        <div className={styles.requiredNote}>* שדה חובה</div>

        <button className={styles.button} type="submit" disabled={loading}>
          {loading ? "טוען..." : "הרשמה"}
        </button>
      </form>
      <p className={styles.switch}>
        כבר יש לך משתמש? <Link to="/login">להתחברות</Link>
      </p>
    </div>
  );
}
