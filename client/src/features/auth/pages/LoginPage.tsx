import { useRef, useState } from "react";
import { login } from "../api";
import { useAuth } from "../useAuth";
// removed failure toast per requirement
import styles from "./auth.module.css";
import { Link, useNavigate } from "react-router-dom";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [submitAttempted, setSubmitAttempted] = useState(false);
  const [credentialsError, setCredentialsError] = useState<string | null>(null);
  const emailRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();
  const { refresh } = useAuth();

  const emailInvalid = submitAttempted && email.trim() === "";
  const passwordInvalid = submitAttempted && password.trim() === "";
  const credsInvalid = Boolean(credentialsError);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitAttempted(true);
    if (!email || !password) {
      setCredentialsError(null);
      return;
    }
    setLoading(true);
    try {
      await login({ email, password });
      setCredentialsError(null);
      await refresh(); // Refresh auth context
      navigate("/");
    } catch {
      // Show inline credentials error, clear inputs, focus email
      setCredentialsError("האימייל ו/או הסיסמה שגויים");
      setEmail("");
      setPassword("");
      setSubmitAttempted(false);
      setTimeout(() => emailRef.current?.focus(), 0);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className={styles.wrapper} dir="rtl">
      <h1 className={styles.title}>התחברות</h1>
      <form
        onSubmit={onSubmit}
        className={styles.form}
        autoComplete="off"
        noValidate
      >
        <label className={styles.label}>
          <span className={styles.labelText}>אימייל</span>
          <input
            className={`${styles.input} ${styles.inputLtr} ${
              emailInvalid || credsInvalid ? styles.inputError : ""
            }`}
            type="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              if (credentialsError) setCredentialsError(null);
            }}
            required
            aria-invalid={emailInvalid || credsInvalid ? "true" : undefined}
            autoComplete="off"
            autoCorrect="off"
            autoCapitalize="none"
            inputMode="email"
            ref={emailRef}
          />
          {emailInvalid && <span className={styles.errorText}>שדה חובה</span>}
        </label>
        <label className={styles.label}>
          <span className={styles.labelText}>סיסמה</span>
          <div className={`${styles.passwordWrapper} ${styles.withToggle}`}>
            <input
              className={`${styles.input} ${
                passwordInvalid || credsInvalid ? styles.inputError : ""
              }`}
              type={showPassword ? "text" : "password"}
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                if (credentialsError) setCredentialsError(null);
              }}
              required
              aria-invalid={
                passwordInvalid || credsInvalid ? "true" : undefined
              }
              ref={passwordRef}
              autoComplete="new-password"
              autoCorrect="off"
              autoCapitalize="none"
            />
            <button
              type="button"
              className={styles.passwordToggle}
              onMouseDown={(e) => {
                // prevent moving focus away from input
                e.preventDefault();
              }}
              onClick={() => {
                const input = passwordRef.current;
                const start = input?.selectionStart ?? undefined;
                const end = input?.selectionEnd ?? undefined;
                setShowPassword((prev) => !prev);
                // restore focus and caret after toggle
                setTimeout(() => {
                  if (input) {
                    input.focus({ preventScroll: true });
                    if (start !== undefined && end !== undefined) {
                      try {
                        input.setSelectionRange(start, end);
                      } catch {
                        // ignore selection errors on some browsers
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
          </div>
          {passwordInvalid && (
            <span className={styles.errorText}>שדה חובה</span>
          )}
          {credentialsError && (
            <span className={styles.errorText}>{credentialsError}</span>
          )}
        </label>
        <button className={styles.button} type="submit" disabled={loading}>
          {loading ? "טוען..." : "התחבר"}
        </button>
      </form>
      <p className={styles.switch}>
        אין לך משתמש? <Link to="/signup">להרשמה</Link>
      </p>
    </div>
  );
}
