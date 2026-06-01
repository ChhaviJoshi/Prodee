import { Outlet } from "react-router-dom";
import TopBar from "./TopBar";
import RetroBackground from "./RetroBackground";
import FocusMiniPlayer from "./FocusMiniPlayer";
import { useAuth } from "../context/AuthContext";

export default function Layout() {
  const { user } = useAuth();

  return (
    <div className="min-h-screen flex flex-col relative">
      {user && <TopBar />}
      <main className="flex-1 relative z-10 pb-32">
        <Outlet />
      </main>
      {user && <FocusMiniPlayer />}
      <RetroBackground />
    </div>
  );
}
