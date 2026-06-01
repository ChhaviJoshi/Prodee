import { useEffect, useMemo, useState } from "react";
import { Activity } from "lucide-react";
import { apiGet } from "../utils/api";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip,
  BarChart,
  Bar,
} from "recharts";

const CHART_TICK = { fontSize: 8, fontFamily: "'Press Start 2P'" };
const TOOLTIP_STYLE = {
  background: "var(--color-retro-surface)",
  border: "2px solid var(--color-retro-border)",
  fontFamily: "'Press Start 2P'",
  fontSize: 8,
};

const COMPACT_TICK = { fontSize: 6, fontFamily: "'Press Start 2P'" };

export default function DashboardAnalytics({ compact = false }) {
  const [history, setHistory] = useState([]);

  useEffect(() => {
    apiGet("/api/journal/analytics/weekly")
      .then((data) => {
        if (!Array.isArray(data)) {
          setHistory([]);
          return;
        }
        setHistory(
          data.map((entry) => ({
            ...entry,
            label: new Date(entry.date).toLocaleDateString("en-US", {
              month: "short",
              day: "numeric",
            }),
          })),
        );
      })
      .catch(() => setHistory([]));
  }, []);

  const averages = useMemo(() => {
    if (!history.length) {
      return {
        sleepHours: 0,
        screenTimeHours: 0,
        waterGlasses: 0,
        exerciseMinutes: 0,
      };
    }
    const sum = history.reduce(
      (acc, entry) => ({
        sleepHours: acc.sleepHours + (entry.sleepHours || 0),
        screenTimeHours: acc.screenTimeHours + (entry.screenTimeHours || 0),
        waterGlasses: acc.waterGlasses + (entry.waterGlasses || 0),
        exerciseMinutes: acc.exerciseMinutes + (entry.exerciseMinutes || 0),
      }),
      {
        sleepHours: 0,
        screenTimeHours: 0,
        waterGlasses: 0,
        exerciseMinutes: 0,
      },
    );
    return {
      sleepHours: (sum.sleepHours / history.length).toFixed(1),
      screenTimeHours: (sum.screenTimeHours / history.length).toFixed(1),
      waterGlasses: Math.round(sum.waterGlasses / history.length),
      exerciseMinutes: Math.round(sum.exerciseMinutes / history.length),
    };
  }, [history]);

  if (compact) {
    if (!history.length) {
      return (
        <div className="pixel-border-sm bg-retro-surface/70 p-3 min-h-[118px] flex items-center">
          <p className="font-pixel text-[7px] leading-relaxed text-retro-muted">
            Add a few Stats Tavern logs to unlock the tavern charts here.
          </p>
        </div>
      );
    }

    return (
      <div className="space-y-2 pointer-events-none">
        <div className="grid grid-cols-4 gap-2">
          <div className="pixel-border-sm bg-retro-surface/70 p-2">
            <p className="font-pixel text-[5px] text-retro-muted mb-1">SLEEP</p>
            <p className="font-pixel text-[8px] text-retro-text">
              {averages.sleepHours}h
            </p>
          </div>
          <div className="pixel-border-sm bg-retro-surface/70 p-2">
            <p className="font-pixel text-[5px] text-retro-muted mb-1">
              SCREEN
            </p>
            <p className="font-pixel text-[8px] text-retro-text">
              {averages.screenTimeHours}h
            </p>
          </div>
          <div className="pixel-border-sm bg-retro-surface/70 p-2">
            <p className="font-pixel text-[5px] text-retro-muted mb-1">WATER</p>
            <p className="font-pixel text-[8px] text-retro-text">
              {averages.waterGlasses}
            </p>
          </div>
          <div className="pixel-border-sm bg-retro-surface/70 p-2">
            <p className="font-pixel text-[5px] text-retro-muted mb-1">MOVE</p>
            <p className="font-pixel text-[8px] text-retro-text">
              {averages.exerciseMinutes}m
            </p>
          </div>
        </div>

        <div className="pixel-border-sm bg-retro-surface/70 p-2">
          <p className="font-pixel text-[5px] text-retro-muted mb-1">
            7 DAY TREND
          </p>
          <div className="h-[78px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={history}>
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke="var(--color-retro-muted)"
                  opacity={0.18}
                />
                <XAxis dataKey="label" tick={COMPACT_TICK} tickMargin={3} />
                <YAxis tick={COMPACT_TICK} width={18} />
                <Tooltip contentStyle={TOOLTIP_STYLE} />
                <Line
                  type="monotone"
                  dataKey="sleepHours"
                  stroke="#8bc34a"
                  strokeWidth={2}
                  dot={false}
                />
                <Line
                  type="monotone"
                  dataKey="screenTimeHours"
                  stroke="#e85d04"
                  strokeWidth={2}
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="pixel-border-sm bg-retro-surface/70 p-2">
          <p className="font-pixel text-[5px] text-retro-muted mb-1">
            HYDRATION VS EXERCISE
          </p>
          <div className="h-[58px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={history}>
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke="var(--color-retro-muted)"
                  opacity={0.18}
                />
                <XAxis dataKey="label" tick={COMPACT_TICK} tickMargin={3} />
                <YAxis tick={COMPACT_TICK} width={18} />
                <Tooltip contentStyle={TOOLTIP_STYLE} />
                <Bar
                  dataKey="waterGlasses"
                  fill="#2196f3"
                  radius={[1, 1, 0, 0]}
                />
                <Bar
                  dataKey="exerciseMinutes"
                  fill="#4caf50"
                  radius={[1, 1, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    );
  }

  return (
    <section className="pixel-border bg-retro-surface p-4 sm:p-5 mb-6">
      <div className="flex items-center gap-2 mb-4">
        <Activity size={16} className="text-retro-danger" />
        <h2 className="font-pixel text-[9px] sm:text-[10px] text-retro-text">
          Dashboard Analytics
        </h2>
      </div>

      {!history.length ? (
        <div className="pixel-border-sm bg-retro-card p-6 text-center">
          <p className="font-pixel text-[8px] text-retro-muted">
            Log sleep, screen time, water, and exercise in Stats Tavern to see
            your dashboard charts here.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 xl:grid-cols-[1.1fr_0.9fr] gap-4">
          <div className="pixel-border-sm bg-retro-card p-3">
            <p className="font-pixel text-[7px] text-retro-muted mb-2">
              WEEKLY TRENDS
            </p>
            <div className="h-[220px]">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={history}>
                  <CartesianGrid
                    strokeDasharray="3 3"
                    stroke="var(--color-retro-muted)"
                    opacity={0.2}
                  />
                  <XAxis dataKey="label" tick={CHART_TICK} />
                  <YAxis tick={CHART_TICK} />
                  <Tooltip contentStyle={TOOLTIP_STYLE} />
                  <Line
                    type="monotone"
                    dataKey="sleepHours"
                    stroke="#7c4dff"
                    strokeWidth={2}
                    dot={{ r: 2 }}
                  />
                  <Line
                    type="monotone"
                    dataKey="screenTimeHours"
                    stroke="#e85d04"
                    strokeWidth={2}
                    dot={{ r: 2 }}
                  />
                  <Line
                    type="monotone"
                    dataKey="waterGlasses"
                    stroke="#2196f3"
                    strokeWidth={2}
                    dot={{ r: 2 }}
                  />
                  <Line
                    type="monotone"
                    dataKey="exerciseMinutes"
                    stroke="#4caf50"
                    strokeWidth={2}
                    dot={{ r: 2 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div className="pixel-border-sm bg-retro-card p-3">
                <p className="font-pixel text-[7px] text-retro-muted mb-1">
                  AVG SLEEP
                </p>
                <p className="font-pixel text-[10px] text-retro-text">
                  {averages.sleepHours}h
                </p>
              </div>
              <div className="pixel-border-sm bg-retro-card p-3">
                <p className="font-pixel text-[7px] text-retro-muted mb-1">
                  AVG SCREEN
                </p>
                <p className="font-pixel text-[10px] text-retro-text">
                  {averages.screenTimeHours}h
                </p>
              </div>
              <div className="pixel-border-sm bg-retro-card p-3">
                <p className="font-pixel text-[7px] text-retro-muted mb-1">
                  AVG WATER
                </p>
                <p className="font-pixel text-[10px] text-retro-text">
                  {averages.waterGlasses}
                </p>
              </div>
              <div className="pixel-border-sm bg-retro-card p-3">
                <p className="font-pixel text-[7px] text-retro-muted mb-1">
                  AVG EXERCISE
                </p>
                <p className="font-pixel text-[10px] text-retro-text">
                  {averages.exerciseMinutes}m
                </p>
              </div>
            </div>

            <div className="pixel-border-sm bg-retro-card p-3">
              <p className="font-pixel text-[7px] text-retro-muted mb-2">
                HYDRATION VS EXERCISE
              </p>
              <div className="h-[140px]">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={history}>
                    <CartesianGrid
                      strokeDasharray="3 3"
                      stroke="var(--color-retro-muted)"
                      opacity={0.2}
                    />
                    <XAxis dataKey="label" tick={CHART_TICK} />
                    <YAxis tick={CHART_TICK} />
                    <Tooltip contentStyle={TOOLTIP_STYLE} />
                    <Bar dataKey="waterGlasses" fill="#2196f3" />
                    <Bar dataKey="exerciseMinutes" fill="#4caf50" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
