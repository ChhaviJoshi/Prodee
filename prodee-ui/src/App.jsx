import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ThemeProvider } from "./context/ThemeContext";
import { AuthProvider, useAuth } from "./context/AuthContext";
import Layout from "./components/Layout";
import LoginPage from "./pages/LoginPage";
import CharacterSelect from "./pages/CharacterSelect";
import Dashboard from "./pages/Dashboard";
import TasksPage from "./pages/TasksPage";
import FocusPage from "./pages/FocusPage";
import JournalPage from "./pages/JournalPage";
import CalendarPage from "./pages/CalendarPage";
import AnalyticsPage from "./pages/AnalyticsPage";
import FeedPage from "./pages/FeedPage";
import ShopPage from "./pages/ShopPage";
import ProfilePage from "./pages/ProfilePage";
import CohortsPage from "./pages/CohortsPage";
import ScrapbookPage from "./pages/ScrapbookPage";

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="font-pixel text-sm text-retro-accent animate-pixel-bounce">
          Loading...
        </p>
      </div>
    );
  }
  return user ? children : <Navigate to="/login" replace />;
}

function GuestRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return null;
  return user ? <Navigate to="/" replace /> : children;
}

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            {/* Guest routes */}
            <Route
              path="/login"
              element={
                <GuestRoute>
                  <Layout />
                </GuestRoute>
              }
            >
              <Route index element={<LoginPage />} />
            </Route>

            {/* Protected routes */}
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <Layout />
                </ProtectedRoute>
              }
            >
              <Route index element={<Dashboard />} />
              <Route path="character" element={<CharacterSelect />} />
              <Route path="tasks" element={<TasksPage />} />
              <Route path="focus" element={<FocusPage />} />
              <Route path="journal" element={<JournalPage />} />
              <Route path="calendar" element={<CalendarPage />} />
              <Route path="analytics" element={<AnalyticsPage />} />
              <Route path="feed" element={<FeedPage />} />
              <Route path="shop" element={<ShopPage />} />
              <Route path="profile" element={<ProfilePage />} />
              <Route path="cohorts" element={<CohortsPage />} />
              <Route path="scrapbook" element={<ScrapbookPage />} />
            </Route>

            {/* Catch-all */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}
