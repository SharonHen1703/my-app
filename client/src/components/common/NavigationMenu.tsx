import { useState, useRef, useEffect } from "react";
import { useAuth } from "../../features/auth/useAuth";
import { logout } from "../../features/auth/api";
import styles from "./NavigationMenu.module.css";

interface NavigationMenuProps {
  /** Position style - 'fixed' for corner positioning, 'relative' for inline */
  position?: "fixed" | "relative";
  /** Additional CSS class name */
  className?: string;
  /** Show navigation links (My Auctions, My Bids) */
  showNavLinks?: boolean;
}

export default function NavigationMenu({
  position = "fixed",
  className,
  showNavLinks = true,
}: NavigationMenuProps) {
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const { user } = useAuth();
  const userMenuRef = useRef<HTMLDivElement>(null);

  const handleLogout = async () => {
    try {
      await logout();
      window.location.href = "/login";
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  const toggleUserMenu = () => {
    setUserMenuOpen(!userMenuOpen);
  };

  // Close user menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        userMenuRef.current &&
        !userMenuRef.current.contains(event.target as Node)
      ) {
        setUserMenuOpen(false);
      }
    };

    if (userMenuOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [userMenuOpen]);

  if (!user) {
    return null;
  }

  const containerClass = `${styles.navigationMenu} ${
    position === "fixed" ? styles.fixed : styles.relative
  } ${className || ""}`;

  return (
    <div className={containerClass} ref={userMenuRef}>
      <div className={styles.navigationButtons}>
        {showNavLinks && (
          <>
            <a
              href="/my-auctions"
              target="_blank"
              rel="noopener noreferrer"
              className={styles.navigationLink}
            >
              המכרזים שלי
            </a>
            <a
              href="/my-bids"
              target="_blank"
              rel="noopener noreferrer"
              className={styles.navigationLink}
            >
              ההצעות שלי
            </a>
          </>
        )}
        <button
          className={styles.userMenuButton}
          onClick={toggleUserMenu}
          aria-expanded={userMenuOpen}
        >
          {user.fullName || user.email}
          <span className={styles.userMenuArrow}>▼</span>
        </button>
      </div>

      {userMenuOpen && (
        <div className={styles.userMenuDropdown}>
          <div className={styles.userInfo}>
            <div className={styles.userName}>{user.fullName || user.email}</div>
            <div className={styles.userEmail}>{user.email}</div>
          </div>
          <hr className={styles.dropdownSeparator} />
          <button onClick={handleLogout} className={styles.logoutButton}>
            התנתק
          </button>
        </div>
      )}
    </div>
  );
}
