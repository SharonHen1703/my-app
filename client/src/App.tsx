import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ToastProvider } from "./components/Toast/ToastProvider";
import { AuthProvider } from "./features/auth/AuthProvider";
import { RequireAuth } from "./features/auth/RequireAuth";
import styles from "./App.module.css";
import AuctionsList from "./features/auctions/components/AuctionsList";
import AuctionDetailPage from "./features/auctions/pages/AuctionDetailPage";
import BidHistoryPage from "./features/auctions/pages/BidHistoryPage";
import MyBidsPage from "./features/bids/pages/MyBidsPage";
import MyAuctionsPage from "./features/auctions/pages/MyAuctionsPage";
import LoginPage from "./features/auth/pages/LoginPage";
import SignupPage from "./features/auth/pages/SignupPage";

function App() {
  return (
    <ToastProvider>
      <Router>
        <AuthProvider>
          <div className={styles.container}>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/signup" element={<SignupPage />} />
              <Route
                path="/"
                element={
                  <RequireAuth>
                    <AuctionsList />
                  </RequireAuth>
                }
              />
              <Route
                path="/auction/:id"
                element={
                  <RequireAuth>
                    <AuctionDetailPage />
                  </RequireAuth>
                }
              />
              <Route
                path="/auction/:id/bids"
                element={
                  <RequireAuth>
                    <BidHistoryPage />
                  </RequireAuth>
                }
              />
              <Route
                path="/my-bids"
                element={
                  <RequireAuth>
                    <MyBidsPage />
                  </RequireAuth>
                }
              />
              <Route
                path="/my-auctions"
                element={
                  <RequireAuth>
                    <MyAuctionsPage />
                  </RequireAuth>
                }
              />
            </Routes>
          </div>
        </AuthProvider>
      </Router>
    </ToastProvider>
  );
}
export default App;
