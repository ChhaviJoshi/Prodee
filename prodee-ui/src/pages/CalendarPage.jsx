import { useMemo, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { CalendarDays, ArrowLeft, Trash2, X } from "lucide-react";
import { apiGet, apiPost, apiDelete } from "../utils/api";

const MONTHS = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];

const DAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

function formatLocalDate(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

export default function CalendarPage() {
  const navigate = useNavigate();
  const now = new Date();

  const [milestones, setMilestones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [month, setMonth] = useState(now.getMonth());
  const [year, setYear] = useState(now.getFullYear());

  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ title: "", targetDate: "" });

  useEffect(() => {
    loadMilestones();
  }, []);

  async function loadMilestones() {
    setLoading(true);
    try {
      const data = await apiGet("/api/milestones");
      setMilestones(Array.isArray(data) ? data : []);
    } catch {
      setMilestones([]);
    } finally {
      setLoading(false);
    }
  }

  async function addMilestone(e) {
    e.preventDefault();
    try {
      await apiPost("/api/milestones", {
        title: form.title,
        targetDate: form.targetDate,
      });
      setShowModal(false);
      setForm({ title: "", targetDate: "" });
      loadMilestones();
    } catch (err) {
      alert(err.message);
    }
  }

  async function deleteMilestone(id) {
    try {
      await apiDelete(`/api/milestones/${id}`);
      loadMilestones();
    } catch {
      // ignore
    }
  }

  function prevMonth() {
    if (month === 0) {
      setMonth(11);
      setYear((y) => y - 1);
    } else {
      setMonth((m) => m - 1);
    }
  }

  function nextMonth() {
    if (month === 11) {
      setMonth(0);
      setYear((y) => y + 1);
    } else {
      setMonth((m) => m + 1);
    }
  }

  const daysInMonth = useMemo(
    () => new Date(year, month + 1, 0).getDate(),
    [year, month],
  );
  const startDay = useMemo(
    () => new Date(year, month, 1).getDay(),
    [year, month],
  );

  const milestoneDateSet = useMemo(() => {
    return new Set(milestones.map((m) => m.targetDate));
  }, [milestones]);

  function openCreateModalWithDate(dateObj) {
    const targetDate = formatLocalDate(dateObj);
    setForm({ title: "", targetDate });
    setShowModal(true);
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-6">
      <div className="flex items-center gap-3 mb-5">
        <button
          onClick={() => navigate("/")}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <ArrowLeft size={14} />
        </button>
        <CalendarDays size={20} className="text-retro-coin" />
        <h1 className="font-pixel text-sm text-retro-text">Countdown Map</h1>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-[1.2fr_1fr] gap-4">
        <div className="pixel-border bg-retro-surface p-4">
          <div className="flex items-center justify-between mb-4">
            <button onClick={prevMonth} className="pixel-btn text-[8px]">
              ◀
            </button>
            <span className="font-pixel text-[10px] text-retro-text">
              {MONTHS[month]} {year}
            </span>
            <button onClick={nextMonth} className="pixel-btn text-[8px]">
              ▶
            </button>
          </div>

          <div className="grid grid-cols-7 gap-1 mb-1">
            {DAYS.map((d) => (
              <div
                key={d}
                className="font-pixel text-[7px] text-retro-muted text-center py-1"
              >
                {d}
              </div>
            ))}
          </div>

          <div className="grid grid-cols-7 gap-1">
            {Array.from({ length: startDay }).map((_, i) => (
              <div key={`empty-${i}`} className="h-11" />
            ))}

            {Array.from({ length: daysInMonth }).map((_, i) => {
              const day = i + 1;
              const dateObj = new Date(year, month, day);
              const dateStr = formatLocalDate(dateObj);
              const isToday = dateStr === formatLocalDate(now);
              const hasMilestone = milestoneDateSet.has(dateStr);

              return (
                <button
                  key={day}
                  onClick={() => openCreateModalWithDate(dateObj)}
                  className={`h-11 relative pixel-border-sm font-pixel text-[8px] flex items-center justify-center transition-all ${
                    isToday
                      ? "bg-retro-accent text-white"
                      : "bg-retro-card text-retro-text hover:bg-retro-input"
                  } ${hasMilestone ? "border-retro-coin" : ""}`}
                  title={`Create milestone for ${dateStr}`}
                >
                  {day}
                  {hasMilestone && (
                    <span className="absolute -top-1.5 text-[7px]">★</span>
                  )}
                </button>
              );
            })}
          </div>

          <p className="font-pixel text-[7px] text-retro-muted mt-3">
            Click any date to create a milestone. The selected date is
            auto-filled.
          </p>
        </div>

        <div className="pixel-border bg-retro-surface p-4">
          <p className="font-pixel text-[8px] text-retro-muted mb-3">
            ACTIVE COUNTDOWNS
          </p>

          {loading ? (
            <p className="font-pixel text-[9px] text-retro-muted text-center py-8">
              Loading...
            </p>
          ) : milestones.length === 0 ? (
            <p className="pixel-border-sm bg-retro-card p-4 font-pixel text-[8px] text-retro-muted text-center">
              No milestones yet.
            </p>
          ) : (
            <div className="space-y-3">
              {milestones.map((m) => {
                const pct =
                  m.totalDays > 0
                    ? Math.round((m.daysPassed / m.totalDays) * 100)
                    : 100;
                const done = m.daysRemaining <= 0;

                return (
                  <div key={m.id} className="pixel-border-sm bg-retro-card p-3">
                    <div className="flex items-start justify-between mb-2 gap-2">
                      <div>
                        <p className="font-pixel text-[9px] text-retro-text">
                          {m.title}
                        </p>
                        <p className="font-pixel text-[7px] text-retro-muted">
                          {m.startDate} {"->"} {m.targetDate}
                        </p>
                      </div>
                      <button
                        onClick={() => deleteMilestone(m.id)}
                        className="text-retro-danger hover:scale-110"
                      >
                        <Trash2 size={12} />
                      </button>
                    </div>

                    <div className="xp-bar-track mb-2">
                      <div
                        className="xp-bar-fill"
                        style={{
                          width: `${pct}%`,
                          background: done
                            ? "var(--color-retro-xp)"
                            : "var(--color-retro-coin)",
                        }}
                      />
                    </div>

                    <div className="flex items-center justify-between font-pixel text-[7px] text-retro-muted">
                      <span>
                        {m.daysPassed}/{m.totalDays} days ({pct}%)
                      </span>
                      <span
                        className={done ? "text-retro-xp" : "text-retro-coin"}
                      >
                        {done ? "Reached" : `${m.daysRemaining}d left`}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/40 flex items-center justify-center p-4">
          <form
            onSubmit={addMilestone}
            className="pixel-border bg-retro-surface p-5 w-full max-w-md space-y-3 animate-pixel-fade-in"
          >
            <div className="flex items-center justify-between">
              <h2 className="font-pixel text-[10px] text-retro-text">
                New Milestone
              </h2>
              <button
                type="button"
                onClick={() => setShowModal(false)}
                className="text-retro-muted hover:text-retro-danger"
              >
                <X size={14} />
              </button>
            </div>

            <input
              className="pixel-input w-full"
              placeholder="Milestone title"
              value={form.title}
              onChange={(e) =>
                setForm((f) => ({ ...f, title: e.target.value }))
              }
              required
            />

            <div>
              <p className="font-pixel text-[7px] text-retro-muted mb-1">
                Target date
              </p>
              <input
                type="date"
                className="pixel-input w-full"
                value={form.targetDate}
                onChange={(e) =>
                  setForm((f) => ({ ...f, targetDate: e.target.value }))
                }
                required
              />
            </div>

            <button type="submit" className="pixel-btn w-full">
              Create Countdown
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
