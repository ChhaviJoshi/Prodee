import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { BarChart3, ArrowLeft, Save } from "lucide-react";
import { apiGet, apiPost } from "../utils/api";
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  Legend,
} from "recharts";

const METRICS = [
  { key: "sleepHours", label: "Sleep (hrs)", color: "#7c4dff", max: 12 },
  {
    key: "screenTimeHours",
    label: "Screen Time (hrs)",
    color: "#e85d04",
    max: 16,
  },
  { key: "waterGlasses", label: "Water (glasses)", color: "#2196f3", max: 15 },
  {
    key: "exerciseMinutes",
    label: "Exercise (min)",
    color: "#4caf50",
    max: 180,
  },
];

const PIE_COLORS = ["#7c4dff", "#e85d04", "#4caf50"];
const CHART_TICK = { fontSize: 8, fontFamily: "'Press Start 2P'" };
const TOOLTIP_STYLE = {
  background: "var(--color-retro-surface)",
  border: "2px solid var(--color-retro-border)",
  fontFamily: "'Press Start 2P'",
  fontSize: 8,
};

export default function AnalyticsPage() {
  const navigate = useNavigate();
  const [range, setRange] = useState("weekly");
  const [form, setForm] = useState({
    sleepHours: "",
    screenTimeHours: "",
    waterGlasses: "",
    exerciseMinutes: "",
  });
  const [history, setHistory] = useState([]);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    loadData();
  }, [range]);

  async function loadData() {
    try {
      const data = await apiGet(`/api/journal/analytics/${range}`);
      if (Array.isArray(data)) {
        setHistory(
          data.map((d) => ({
            ...d,
            label: new Date(d.date).toLocaleDateString("en-US", {
              month: "short",
              day: "numeric",
            }),
          })),
        );
      }
    } catch {
      setHistory([]);
    }
  }

  async function handleSave(e) {
    e.preventDefault();
    const today = new Date().toISOString().split("T")[0];
    try {
      await apiPost("/api/journal/analytics", {
        date: today,
        sleepHours: +form.sleepHours || 0,
        screenTimeHours: +form.screenTimeHours || 0,
        waterGlasses: +form.waterGlasses || 0,
        exerciseMinutes: +form.exerciseMinutes || 0,
      });
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
      loadData();
    } catch (err) {
      alert(err.message);
    }
  }

  // Pie data: average hours distribution
  const avgSleep = history.length
    ? history.reduce((s, d) => s + (d.sleepHours || 0), 0) / history.length
    : 0;
  const avgScreen = history.length
    ? history.reduce((s, d) => s + (d.screenTimeHours || 0), 0) / history.length
    : 0;
  const avgOther = Math.max(0, 24 - avgSleep - avgScreen);
  const pieData = [
    { name: "Sleep", value: +avgSleep.toFixed(1) },
    { name: "Screen", value: +avgScreen.toFixed(1) },
    { name: "Other", value: +avgOther.toFixed(1) },
  ];

  return (
    <div className="max-w-5xl mx-auto px-4 py-6">
      {/* Header */}
      <div className="flex items-center gap-3 mb-5">
        <button
          onClick={() => navigate("/")}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <ArrowLeft size={14} />
        </button>
        <BarChart3 size={20} className="text-retro-danger" />
        <h1 className="font-pixel text-sm text-retro-text">Stats Tavern</h1>
      </div>

      {/* Range Toggle */}
      <div className="flex gap-0 mb-4">
        {["weekly", "monthly"].map((r) => (
          <button
            key={r}
            onClick={() => setRange(r)}
            className={`flex-1 font-pixel text-[9px] py-2 border-2 border-retro-border ${
              range === r
                ? "bg-retro-accent text-white"
                : "bg-retro-card text-retro-muted"
            } ${r === "monthly" ? "border-l-0" : ""}`}
          >
            {r === "weekly" ? "📅 Weekly" : "📆 Monthly"}
          </button>
        ))}
      </div>

      <div className="flex flex-col lg:flex-row gap-4">
        {/* Left: Log Form */}
        <div className="w-full lg:w-64 pixel-border bg-retro-surface p-4">
          <p className="font-pixel text-[8px] text-retro-muted mb-3">
            📊 LOG TODAY'S STATS
          </p>
          <form onSubmit={handleSave} className="space-y-2">
            {METRICS.map((m) => (
              <div key={m.key}>
                <label className="font-pixel text-[7px] text-retro-muted block mb-0.5">
                  {m.label}
                </label>
                <input
                  type="number"
                  step="0.1"
                  min="0"
                  max={m.max}
                  className="pixel-input w-full"
                  value={form[m.key]}
                  onChange={(e) =>
                    setForm({ ...form, [m.key]: e.target.value })
                  }
                  placeholder="0"
                />
              </div>
            ))}
            <button
              type="submit"
              className="pixel-btn w-full flex items-center justify-center gap-1 mt-2"
            >
              <Save size={10} /> Save Entry
            </button>
            {saved && (
              <p className="font-pixel text-[8px] text-retro-xp text-center animate-pixel-bounce">
                ✦ Saved!
              </p>
            )}
          </form>
        </div>

        {/* Right: Charts */}
        <div className="flex-1 space-y-4">
          {history.length === 0 ? (
            <div className="pixel-border bg-retro-surface p-8 text-center">
              <p className="font-pixel text-[9px] text-retro-muted">
                No data yet — log your first entry!
              </p>
            </div>
          ) : (
            <>
              {/* Pie: Daily hours breakdown */}
              <div className="pixel-border bg-retro-surface p-4">
                <p className="font-pixel text-[8px] text-retro-muted mb-3">
                  🕐 AVG DAILY HOURS
                </p>
                <ResponsiveContainer width="100%" height={200}>
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={40}
                      outerRadius={70}
                      dataKey="value"
                      label={({ name, value }) => `${name}: ${value}h`}
                      labelLine={false}
                      style={{ fontSize: 8, fontFamily: "'Press Start 2P'" }}
                    >
                      {pieData.map((_, i) => (
                        <Cell key={i} fill={PIE_COLORS[i]} />
                      ))}
                    </Pie>
                    <Tooltip contentStyle={TOOLTIP_STYLE} />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              {/* Bar: Sleep vs Screen time */}
              <div className="pixel-border bg-retro-surface p-4">
                <p className="font-pixel text-[8px] text-retro-muted mb-3">
                  🛌 SLEEP vs 📱 SCREEN TIME
                </p>
                <ResponsiveContainer width="100%" height={200}>
                  <BarChart data={history}>
                    <CartesianGrid
                      strokeDasharray="3 3"
                      stroke="var(--color-retro-muted)"
                      opacity={0.2}
                    />
                    <XAxis dataKey="label" tick={CHART_TICK} />
                    <YAxis tick={CHART_TICK} />
                    <Tooltip contentStyle={TOOLTIP_STYLE} />
                    <Legend
                      wrapperStyle={{
                        fontFamily: "'Press Start 2P'",
                        fontSize: 7,
                      }}
                    />
                    <Bar dataKey="sleepHours" fill="#7c4dff" name="Sleep (h)" />
                    <Bar
                      dataKey="screenTimeHours"
                      fill="#e85d04"
                      name="Screen (h)"
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>

              {/* Line: Exercise trend */}
              <div className="pixel-border bg-retro-surface p-4">
                <p className="font-pixel text-[8px] text-retro-muted mb-3">
                  🏃 EXERCISE TREND
                </p>
                <ResponsiveContainer width="100%" height={200}>
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
                      dataKey="exerciseMinutes"
                      stroke="#4caf50"
                      strokeWidth={2}
                      dot={{ r: 3 }}
                      name="Exercise (min)"
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>

              {/* Bar: Water intake */}
              <div className="pixel-border bg-retro-surface p-4">
                <p className="font-pixel text-[8px] text-retro-muted mb-3">
                  💧 WATER INTAKE
                </p>
                <ResponsiveContainer width="100%" height={200}>
                  <BarChart data={history}>
                    <CartesianGrid
                      strokeDasharray="3 3"
                      stroke="var(--color-retro-muted)"
                      opacity={0.2}
                    />
                    <XAxis dataKey="label" tick={CHART_TICK} />
                    <YAxis tick={CHART_TICK} />
                    <Tooltip contentStyle={TOOLTIP_STYLE} />
                    <Bar
                      dataKey="waterGlasses"
                      fill="#2196f3"
                      name="Water (glasses)"
                    />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
