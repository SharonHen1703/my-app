import { useState, useRef, useEffect } from "react";
import { useAuth } from "../../features/auth/useAuth";
import { logout } from "../../features/auth/api";
import styles from "./UserMenu.module.css";

interface UserMenuProps {
  /** Position style - 'fixed' for corner positioning, 'relative' for inline */
  position?: "fixed" | "relative";
  /** Additional CSS class name */
  className?: string;
}

export default function UserMenu({
  position = "fixed",
  className,
}: UserMenuProps) {
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [dropdownPosition, setDropdownPosition] = useState<"bottom" | "top">(
    "bottom"
  );
  const { user } = useAuth();
  const userMenuRef = useRef<HTMLDivElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

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

  // Calculate dropdown position to prevent overflow
  useEffect(() => {
    if (userMenuOpen && userMenuRef.current && dropdownRef.current) {
      const buttonRect = userMenuRef.current.getBoundingClientRect();
      const dropdownHeight = dropdownRef.current.offsetHeight || 150; // estimated height
      const viewportHeight = window.innerHeight;
      const spaceBelow = viewportHeight - buttonRect.bottom;

      // If there's not enough space below, position above
      if (spaceBelow < dropdownHeight + 20) {
        setDropdownPosition("top");
      } else {
        setDropdownPosition("bottom");
      }
    }
  }, [userMenuOpen]);

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

  const containerClass = `${styles.userMenuContainer} ${
    position === "fixed" ? styles.fixed : styles.relative
  } ${className || ""}`;

  const dropdownClass = `${styles.userDropdown} ${
    dropdownPosition === "top" ? styles.dropdownTop : styles.dropdownBottom
  }`;

  return (
    <div className={containerClass} ref={userMenuRef}>
      <button
        onClick={toggleUserMenu}
        className={styles.userMenuButton}
        aria-expanded={userMenuOpen}
      >
        {user.fullName || user.email}
        <span className={styles.chevron}>▼</span>
      </button>

      {userMenuOpen && (
        <div className={dropdownClass} ref={dropdownRef}>
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
