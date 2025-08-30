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
      console.log("🔄 AuthProvider refresh - fetching user data...");
      const userData = await me();
      console.log("✅ AuthProvider - user data fetched:", userData);
      setUser(userData);
    } catch (error) {
      console.error("❌ AuthProvider - error fetching user:", error);
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
