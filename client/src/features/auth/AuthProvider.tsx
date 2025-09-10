import { createContext, useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import type { ReactNode } from "react";
import { me, logout as logoutApi } from "./api";

export type User = { id: number; email: string; fullName?: string };

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  refresh: () => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

// דפים ציבוריים שלא צריכים authentication check
const PUBLIC_ROUTES = ["/login", "/signup"];

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const location = useLocation();

  const refresh = async () => {
    try {
      const userData = await me();
      setUser(userData);
    } catch {
      setUser(null);
    }
  };

  const logout = async () => {
    try {
      await logoutApi();
      setUser(null);
    } catch (error) {
      console.error("Error during logout:", error);
      // Even if logout API fails, clear user state
      setUser(null);
    }
  };

  // Listen for logout events from other tabs/windows
  useEffect(() => {
    // Method 1: localStorage events (works across all tabs)
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === "auth_logout") {
        console.log(
          "🔄 Logout detected from another tab/window via localStorage"
        );
        setUser(null);
        // Redirect to login if not already on a public route
        if (!PUBLIC_ROUTES.includes(location.pathname)) {
          window.location.href = "/login";
        }
      } else if (e.key === "auth_login") {
        console.log(
          "🔄 Login detected from another tab/window via localStorage"
        );
        // Refresh user data when login happens in another tab
        refresh();
      }
    };

    // Method 2: BroadcastChannel (more modern, works within same domain)
    let broadcastChannel: BroadcastChannel | null = null;
    if ("BroadcastChannel" in window) {
      broadcastChannel = new BroadcastChannel("auth_channel");
      broadcastChannel.addEventListener("message", (event) => {
        if (event.data.type === "LOGOUT") {
          console.log(
            "🔄 Logout detected from another tab/window via BroadcastChannel"
          );
          setUser(null);
          if (!PUBLIC_ROUTES.includes(location.pathname)) {
            window.location.href = "/login";
          }
        } else if (event.data.type === "LOGIN") {
          console.log(
            "🔄 Login detected from another tab/window via BroadcastChannel"
          );
          refresh();
        }
      });
    }

    window.addEventListener("storage", handleStorageChange);

    return () => {
      window.removeEventListener("storage", handleStorageChange);
      if (broadcastChannel) {
        broadcastChannel.close();
      }
    };
  }, [location.pathname]);

  useEffect(() => {
    const initAuth = async () => {
      // בדיקה אם הדף הנוכחי הוא דף ציבורי
      if (PUBLIC_ROUTES.includes(location.pathname)) {
        setIsLoading(false);
        return;
      }

      await refresh();
      setIsLoading(false);
    };

    initAuth();
  }, [location.pathname]);

  const value = {
    user,
    isLoading,
    refresh,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// Export the context for the hook
export { AuthContext };
