import { useTheme } from "../context/ThemeContext";
import { useAuth } from "../context/AuthContext";
import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  Sun,
  Moon,
  Bell,
  ChevronDown,
  LogOut,
  User,
  Backpack,
  Newspaper,
} from "lucide-react";
import { apiGet, apiPost } from "../utils/api";

export default function TopBar() {
  const { dark, toggle } = useTheme();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [profileOpen, setProfileOpen] = useState(false);
  const [notifsOpen, setNotifsOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const profileRef = useRef(null);
  const notifsRef = useRef(null);

  useEffect(() => {
    if (!user) return;
    apiGet("/api/notifications/unread-count")
      .then((res) => setUnreadCount(res?.count ?? 0))
      .catch(() => {});
  }, [user]);

  async function openNotifications() {
    const opening = !notifsOpen;
    setNotifsOpen(opening);
    if (!opening) return;

    setUnreadCount(0);
    apiPost("/api/notifications/read-all", {}).catch(() => {});

    try {
      const list = await apiGet("/api/notifications");
      setNotifications(Array.isArray(list) ? list : []);
    } catch {
      setNotifications([]);
    }
  }

  // Close dropdowns on outside click
  useEffect(() => {
    function handleClick(e) {
      if (profileRef.current && !profileRef.current.contains(e.target))
        setProfileOpen(false);
      if (notifsRef.current && !notifsRef.current.contains(e.target))
        setNotifsOpen(false);
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  if (!user) return null;

  const level = user.level ?? 1;
  const xp = user.xp ?? 0;
  const xpForNext = level * 100;
  const xpPct = Math.min((xp / xpForNext) * 100, 100);
  const coins = user.coins ?? 0;

  return (
    <header
      className="sticky top-0 z-50 bg-retro-surface pixel-border-sm flex items-center justify-between px-4 py-2 gap-3"
      style={{ borderTop: "none", borderLeft: "none", borderRight: "none" }}
    >
      {/* Left: Logo */}
      <button
        onClick={() => navigate("/")}
        className="flex items-center gap-2 group"
      >
        <span className="text-retro-accent font-pixel text-sm tracking-wider group-hover:animate-pixel-bounce">
          ⚔ PRODEE
        </span>
      </button>

      {/* Center: XP Bar */}
      <div className="hidden sm:flex items-center gap-3 flex-1 max-w-md mx-4">
        <span className="font-pixel text-[8px] text-retro-muted whitespace-nowrap">
          LVL {level}
        </span>
        <div className="xp-bar-track flex-1">
          <div className="xp-bar-fill" style={{ width: `${xpPct}%` }} />
        </div>
        <span className="font-pixel text-[8px] text-retro-xp whitespace-nowrap">
          {xp}/{xpForNext} XP
        </span>
        <span className="font-pixel text-[8px] text-retro-coin whitespace-nowrap">
          🪙 {coins}
        </span>
      </div>

      {/* Right: Actions */}
      <div className="flex items-center gap-2">
        {/* Theme toggle */}
        <button
          onClick={toggle}
          className="p-1.5 pixel-border-sm bg-retro-card hover:bg-retro-accent hover:text-white transition-colors"
          title={dark ? "Switch to Light" : "Switch to Dark"}
        >
          {dark ? <Sun size={14} /> : <Moon size={14} />}
        </button>

        {/* Notifications */}
        <div ref={notifsRef} className="relative">
          <button
            onClick={openNotifications}
            className="p-1.5 pixel-border-sm bg-retro-card hover:bg-retro-accent hover:text-white transition-colors relative"
          >
            <Bell size={14} />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 w-2.5 h-2.5 bg-retro-danger border border-retro-border" />
            )}
          </button>

          {notifsOpen && (
            <div className="absolute right-0 top-full mt-2 w-72 bg-retro-surface pixel-border p-3 animate-pixel-fade-in">
              <p className="font-pixel text-[8px] text-retro-muted mb-2">
                ★ DAILY REMINDERS
              </p>
              <div className="space-y-2 text-[9px]">
                {notifications.length === 0 ? (
                  <div className="p-2 bg-retro-card pixel-border-sm">
                    No new alerts right now.
                  </div>
                ) : (
                  notifications.map((n) => (
                    <div
                      key={n.id}
                      className="p-2 bg-retro-card pixel-border-sm flex items-start gap-2"
                    >
                      <Newspaper
                        size={12}
                        className="text-retro-accent2 flex-shrink-0 mt-0.5"
                      />
                      <span>{n.message}</span>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* Profile */}
        <div ref={profileRef} className="relative">
          <div className="flex items-center gap-1">
            <button
              onClick={() => {
                setProfileOpen(false);
                navigate("/profile");
              }}
              className="flex items-center gap-2 p-1.5 pixel-border-sm bg-retro-card hover:bg-retro-accent hover:text-white transition-colors"
              title="Open profile"
            >
              <div className="w-5 h-5 bg-retro-accent flex items-center justify-center text-white text-[8px] font-pixel">
                {(user.username ?? "P")[0].toUpperCase()}
              </div>
              <span className="font-pixel text-[8px] hidden md:inline">
                {user.username ?? "Player"}
              </span>
            </button>
            <button
              onClick={() => setProfileOpen((o) => !o)}
              className="p-1.5 pixel-border-sm bg-retro-card hover:bg-retro-accent hover:text-white transition-colors"
              title="Open account menu"
            >
              <ChevronDown size={10} />
            </button>
          </div>

          {profileOpen && (
            <div className="absolute right-0 top-full mt-2 w-56 bg-retro-surface pixel-border p-3 animate-pixel-fade-in">
              <div className="flex items-center gap-2 mb-3 pb-2 border-b-2 border-retro-border">
                <div className="w-8 h-8 bg-retro-accent flex items-center justify-center text-white font-pixel text-xs">
                  {(user.username ?? "P")[0].toUpperCase()}
                </div>
                <div>
                  <p className="font-pixel text-[9px]">
                    {user.username ?? "Player"}
                  </p>
                  <p className="font-pixel text-[7px] text-retro-muted">
                    Level {level} Adventurer
                  </p>
                </div>
              </div>
              <div className="sm:hidden mb-2 pb-2 border-b-2 border-retro-border">
                <div className="xp-bar-track mb-1">
                  <div className="xp-bar-fill" style={{ width: `${xpPct}%` }} />
                </div>
                <p className="font-pixel text-[7px] text-retro-muted">
                  {xp}/{xpForNext} XP &nbsp; 🪙 {coins}
                </p>
              </div>
              <button
                onClick={() => {
                  setProfileOpen(false);
                  navigate("/shop");
                }}
                className="flex items-center gap-2 w-full text-left p-1.5 hover:bg-retro-card font-pixel text-[9px] transition-colors"
              >
                <Backpack size={12} /> Inventory & Shop
              </button>
              <button
                onClick={() => {
                  setProfileOpen(false);
                  navigate("/cohorts");
                }}
                className="flex items-center gap-2 w-full text-left p-1.5 hover:bg-retro-card font-pixel text-[9px] transition-colors"
              >
                <User size={12} /> Cohorts
              </button>
              <hr className="border-retro-border my-1" />
              <button
                onClick={() => {
                  logout();
                  navigate("/login");
                }}
                className="flex items-center gap-2 w-full text-left p-1.5 hover:bg-retro-card font-pixel text-[9px] text-retro-danger transition-colors"
              >
                <LogOut size={12} /> Logout
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
