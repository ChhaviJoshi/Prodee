import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Timer, Pause, Play } from "lucide-react";

const STORAGE_KEY = "prodee-focus-timer";

function readState() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (!parsed?.duration) return null;

    let remaining = parsed.remaining ?? parsed.duration;
    if (parsed.running && parsed.endTime) {
      remaining = Math.max(0, Math.round((parsed.endTime - Date.now()) / 1000));
    }
    if (remaining <= 0) return null;
    return { ...parsed, remaining, running: Boolean(parsed.running) };
  } catch {
    return null;
  }
}

function saveState(next) {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next));
}

export default function FocusMiniPlayer() {
  const navigate = useNavigate();
  const [timerState, setTimerState] = useState(readState());

  useEffect(() => {
    const interval = setInterval(() => setTimerState(readState()), 1000);
    return () => clearInterval(interval);
  }, []);

  if (!timerState) return null;

  const mins = String(Math.floor(timerState.remaining / 60)).padStart(2, "0");
  const secs = String(timerState.remaining % 60).padStart(2, "0");

  function toggleRunning(e) {
    e.stopPropagation();
    const current = readState();
    if (!current) return;

    if (current.running) {
      const next = {
        ...current,
        running: false,
        endTime: null,
        remaining: current.remaining,
      };
      saveState(next);
      setTimerState(next);
      return;
    }

    const next = {
      ...current,
      running: true,
      endTime: Date.now() + current.remaining * 1000,
    };
    saveState(next);
    setTimerState(next);
  }

  return (
    <div className="fixed right-4 bottom-6 z-40 pixel-border bg-retro-card shadow-lg px-3 py-2.5 w-[220px] sm:w-[250px] animate-pixel-fade-in">
      <button
        onClick={() => navigate("/focus")}
        className="w-full flex items-center gap-2 hover:text-retro-accent2"
        title="Open Focus Forge"
      >
        <Timer size={16} />
        <span className="font-pixel text-[8px] text-retro-muted">
          Focus Forge
        </span>
        <span className="ml-auto font-pixel text-[11px] text-retro-text">
          {mins}:{secs}
        </span>
      </button>

      <div className="mt-2 pt-2 border-t-2 border-retro-border flex items-center justify-between">
        <span className="font-pixel text-[7px] text-retro-muted">
          {timerState.running ? "Session running" : "Paused"}
        </span>
        <button
          onClick={toggleRunning}
          className="pixel-border-sm px-2 py-1 bg-retro-surface hover:bg-retro-input flex items-center gap-1 font-pixel text-[7px]"
          title={timerState.running ? "Pause timer" : "Resume timer"}
        >
          {timerState.running ? <Pause size={11} /> : <Play size={11} />}
          {timerState.running ? "Pause" : "Play"}
        </button>
      </div>
    </div>
  );
}
