import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import {
  Timer,
  Play,
  Pause,
  RotateCcw,
  ArrowLeft,
  Volume2,
  VolumeX,
} from "lucide-react";
import { apiGet, apiPost } from "../utils/api";

const SOUNDS = [
  { id: "campfire", icon: "🔥", label: "Campfire" },
  { id: "rain", icon: "🌧", label: "Rain" },
  { id: "forest", icon: "🌲", label: "Forest" },
  { id: "stream", icon: "💧", label: "Stream" },
  { id: "wind", icon: "🌀", label: "Wind" },
];

const STORAGE_KEY = "prodee-focus-timer";

function loadTimerState() {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const s = JSON.parse(raw);
    if (!s?.duration) return null;
    if (s.running && s.endTime) {
      const now = Date.now();
      const remaining = Math.max(0, Math.round((s.endTime - now) / 1000));
      return { ...s, remaining };
    }
    return { ...s, remaining: s.remaining ?? s.duration };
  } catch {
    return null;
  }
}

function saveTimerState(state) {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

function clearTimerState() {
  sessionStorage.removeItem(STORAGE_KEY);
}

export default function FocusPage() {
  const navigate = useNavigate();
  const saved = loadTimerState();

  const [mode, setMode] = useState(saved?.mode || "work");
  const [customMin, setCustomMin] = useState(saved?.customMin || 25);
  const [duration, setDuration] = useState(saved?.duration || 25 * 60);
  const [remaining, setRemaining] = useState(
    saved?.remaining ?? saved?.duration ?? 25 * 60,
  );
  const [running, setRunning] = useState(
    saved?.running && (saved?.remaining ?? 0) > 0,
  );
  const [activeSound, setActiveSound] = useState(null);
  const [baseProductiveMins, setBaseProductiveMins] = useState(0);
  const [sessionDone, setSessionDone] = useState(false);
  const [sessionStartedAt, setSessionStartedAt] = useState(
    saved?.startedAt || null,
  );
  const intervalRef = useRef(null);
  const loggedRef = useRef(false);

  // Load productive hours
  useEffect(() => {
    apiGet("/api/focus-sessions/productive-minutes")
      .then((mins) =>
        setBaseProductiveMins(typeof mins === "number" ? mins : 0),
      )
      .catch(() => {});
  }, [sessionDone]);

  // Persist state so the floating mini player can control play/pause across pages.
  useEffect(() => {
    if (remaining <= 0) {
      clearTimerState();
      return;
    }

    saveTimerState({
      mode,
      customMin,
      duration,
      remaining,
      running,
      endTime: running ? Date.now() + remaining * 1000 : null,
      startedAt: sessionStartedAt || new Date().toISOString(),
    });
  }, [running, remaining, mode, customMin, duration, sessionStartedAt]);

  // Timer interval
  useEffect(() => {
    if (running) {
      intervalRef.current = setInterval(() => {
        setRemaining((r) => {
          if (r <= 1) {
            clearInterval(intervalRef.current);
            setRunning(false);
            clearTimerState();
            return 0;
          }
          return r - 1;
        });
      }, 1000);
    } else {
      clearInterval(intervalRef.current);
    }
    return () => clearInterval(intervalRef.current);
  }, [running]);

  // Auto-log completed session
  useEffect(() => {
    if (remaining === 0 && !loggedRef.current && mode === "work") {
      loggedRef.current = true;
      setSessionDone((d) => !d);
      const elapsedSeconds = duration - remaining;
      const startedAt =
        sessionStartedAt ||
        new Date(Date.now() - elapsedSeconds * 1000).toISOString();
      apiPost("/api/focus-sessions", {
        expectedDurationMinutes: Math.round(duration / 60),
        actualDurationMinutes: Math.round(duration / 60),
        ambientType: activeSound?.toUpperCase() || "SILENCE",
        startedAt,
        endedAt: new Date().toISOString(),
      }).catch(() => {});
    }
  }, [remaining, mode, duration, activeSound, sessionStartedAt]);

  function applyCustom() {
    const mins = Math.max(1, Math.min(180, +customMin || 25));
    const d = mins * 60;
    setDuration(d);
    setRemaining(d);
    setRunning(false);
    setSessionStartedAt(null);
    loggedRef.current = false;
    clearTimerState();
  }

  function switchMode(m) {
    setMode(m);
    const mins = m === "work" ? customMin : 5;
    const d = mins * 60;
    setCustomMin(m === "work" ? customMin : 5);
    setDuration(d);
    setRemaining(d);
    setRunning(false);
    setSessionStartedAt(null);
    loggedRef.current = false;
    clearTimerState();
  }

  function reset() {
    setRemaining(duration);
    setRunning(false);
    setSessionStartedAt(null);
    loggedRef.current = false;
    clearTimerState();
  }

  function toggleRun() {
    if (!running) {
      const nextStart = sessionStartedAt || new Date().toISOString();
      setSessionStartedAt(nextStart);
      loggedRef.current = false;
      saveTimerState({
        mode,
        customMin,
        duration,
        remaining,
        running: true,
        endTime: Date.now() + remaining * 1000,
        startedAt: nextStart,
      });
    }
    setRunning((r) => !r);
  }

  const sessionEarnedMins =
    mode === "work" ? Math.floor((duration - remaining) / 60) : 0;
  const productiveMins = baseProductiveMins + sessionEarnedMins;

  const mins = String(Math.floor(remaining / 60)).padStart(2, "0");
  const secs = String(remaining % 60).padStart(2, "0");
  const pct = ((duration - remaining) / duration) * 100;

  return (
    <div className="max-w-md mx-auto px-4 py-6">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => navigate("/")}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <ArrowLeft size={14} />
        </button>
        <Timer size={20} className="text-retro-accent2" />
        <h1 className="font-pixel text-sm text-retro-text">Focus Forge</h1>
        <span className="ml-auto font-pixel text-[8px] text-retro-xp">
          🔥 {Math.floor(productiveMins / 60)}h {productiveMins % 60}m total
        </span>
      </div>

      {/* Mode toggle */}
      <div className="flex gap-0 mb-4">
        <button
          onClick={() => switchMode("work")}
          className={`flex-1 font-pixel text-[9px] py-2 border-2 border-retro-border ${
            mode === "work"
              ? "bg-retro-accent text-white"
              : "bg-retro-card text-retro-muted"
          }`}
        >
          ⚔ Work
        </button>
        <button
          onClick={() => switchMode("break")}
          className={`flex-1 font-pixel text-[9px] py-2 border-2 border-l-0 border-retro-border ${
            mode === "break"
              ? "bg-retro-xp text-white"
              : "bg-retro-card text-retro-muted"
          }`}
        >
          ☕ Break (5m)
        </button>
      </div>

      {/* Custom duration */}
      {mode === "work" && !running && (
        <div className="flex gap-2 mb-4 items-center">
          <label className="font-pixel text-[8px] text-retro-muted whitespace-nowrap">
            Duration:
          </label>
          <input
            type="number"
            min="1"
            max="180"
            className="pixel-input w-20 text-center"
            value={customMin}
            onChange={(e) => setCustomMin(e.target.value)}
          />
          <span className="font-pixel text-[7px] text-retro-muted">min</span>
          <button onClick={applyCustom} className="pixel-btn text-[8px]">
            Set
          </button>
        </div>
      )}

      {/* Timer */}
      <div className="pixel-border bg-retro-card p-8 text-center mb-6">
        <div className="xp-bar-track mb-6 mx-auto max-w-xs">
          <div
            className="xp-bar-fill"
            style={{
              width: `${pct}%`,
              background:
                mode === "work"
                  ? "var(--color-retro-accent)"
                  : "var(--color-retro-xp)",
            }}
          />
        </div>

        <p className="font-pixel text-3xl text-retro-text mb-6 tracking-widest">
          {mins}:{secs}
        </p>

        <div className="flex justify-center gap-3">
          <button
            onClick={toggleRun}
            className="pixel-btn flex items-center gap-1"
            style={{
              background: running
                ? "var(--color-retro-danger)"
                : mode === "work"
                  ? "var(--color-retro-accent)"
                  : "var(--color-retro-xp)",
            }}
          >
            {running ? <Pause size={12} /> : <Play size={12} />}
            {running ? "Pause" : "Start"}
          </button>
          <button
            onClick={reset}
            className="pixel-btn flex items-center gap-1"
            style={{ background: "var(--color-retro-muted)" }}
          >
            <RotateCcw size={12} /> Reset
          </button>
        </div>

        {remaining === 0 && mode === "work" && (
          <p className="font-pixel text-[10px] text-retro-xp mt-4 animate-pixel-bounce">
            ✦ Session Complete! Logged automatically.
          </p>
        )}
      </div>

      {/* Ambient sounds */}
      <div className="pixel-border bg-retro-surface p-4">
        <p className="font-pixel text-[8px] text-retro-muted mb-3 flex items-center gap-2">
          {activeSound ? <Volume2 size={10} /> : <VolumeX size={10} />} AMBIENT
          SOUNDS
        </p>
        <div className="flex justify-between gap-2">
          {SOUNDS.map((s) => (
            <button
              key={s.id}
              onClick={() => setActiveSound(activeSound === s.id ? null : s.id)}
              className={`pixel-border-sm flex-1 py-3 flex flex-col items-center gap-1 transition-all ${
                activeSound === s.id
                  ? "bg-retro-accent/20 scale-105"
                  : "bg-retro-card hover:bg-retro-input"
              }`}
              title={s.label}
            >
              <span className="text-lg">{s.icon}</span>
              <span className="font-pixel text-[6px] text-retro-muted">
                {s.label}
              </span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
