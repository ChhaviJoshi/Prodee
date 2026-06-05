import { useState, useEffect } from "react";
import { apiGet, apiPost, apiPut, apiDelete } from "../utils/api";
import {
  Plus,
  Trash2,
  Swords,
  RefreshCw,
  ArrowLeft,
  Pencil,
  X,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function TasksPage() {
  const navigate = useNavigate();
  const { refreshProfile } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [habits, setHabits] = useState([]);
  const [tab, setTab] = useState("tasks");
  const [showForm, setShowForm] = useState(false);
  const [editItem, setEditItem] = useState(null); // { type: 'task'|'habit', ...fields }
  const [form, setForm] = useState({
    title: "",
    description: "",
    tags: "",
    difficulty: "MEDIUM",
    dueDate: "",
    frequency: "DAILY",
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    try {
      const [t, h] = await Promise.all([
        apiGet("/api/tasks").catch(() => []),
        apiGet("/api/habits").catch(() => []),
      ]);
      setTasks(Array.isArray(t) ? t : []);
      setHabits(Array.isArray(h) ? h : []);
    } finally {
      setLoading(false);
    }
  }

  const EMPTY_FORM = {
    title: "",
    description: "",
    tags: "",
    difficulty: "MEDIUM",
    dueDate: "",
    frequency: "DAILY",
  };

  async function addTask(e) {
    e.preventDefault();
    try {
      if (tab === "tasks") {
        await apiPost("/api/tasks", {
          title: form.title,
          description: form.description,
          difficulty: form.difficulty,
          tags: form.tags || null,
          dueDate: form.dueDate || null,
        });
      } else {
        await apiPost("/api/habits", {
          title: form.title,
          tag: form.tags,
          frequency: form.frequency || "DAILY",
        });
      }
      setForm(EMPTY_FORM);
      setShowForm(false);
      loadData();
    } catch (err) {
      alert(err.message);
    }
  }

  function openEditTask(t) {
    setEditItem({
      type: "task",
      id: t.id,
      title: t.title,
      description: t.description || "",
      difficulty: t.difficulty || "MEDIUM",
      tags: t.tags || "",
      dueDate: t.dueDate || "",
    });
  }

  function openEditHabit(h) {
    setEditItem({
      type: "habit",
      id: h.id,
      title: h.title,
      tag: h.tag || "",
      frequency: h.frequency || "DAILY",
    });
  }

  async function saveEdit(e) {
    e.preventDefault();
    try {
      if (editItem.type === "task") {
        await apiPut(`/api/tasks/${editItem.id}`, {
          title: editItem.title,
          description: editItem.description,
          difficulty: editItem.difficulty,
          tags: editItem.tags || null,
          dueDate: editItem.dueDate || null,
        });
      } else {
        await apiPut(`/api/habits/${editItem.id}`, {
          title: editItem.title,
          tag: editItem.tag,
          frequency: editItem.frequency,
        });
      }
      setEditItem(null);
      loadData();
    } catch (err) {
      alert(err.message);
    }
  }

  async function toggleTask(id) {
    try {
      await apiPost(`/api/tasks/${id}/complete`);
      loadData();
      refreshProfile();
    } catch {
      // fallback
    }
  }

  async function completeHabit(id) {
    try {
      await apiPost(`/api/habits/${id}/complete`);
      loadData();
      refreshProfile();
    } catch (err) {
      alert(err.message);
    }
  }

  async function deleteItem(type, id) {
    try {
      await apiDelete(`/api/${type}/${id}`);
      loadData();
    } catch {
      // ignore
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-6">
      {/* Header */}
      <div className="flex items-center gap-3 mb-5">
        <button
          onClick={() => navigate("/")}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <ArrowLeft size={14} />
        </button>
        <Swords size={20} className="text-retro-accent" />
        <h1 className="font-pixel text-sm text-retro-text">Quest Board</h1>
      </div>

      {/* Tabs */}
      <div className="flex gap-0 mb-4">
        <button
          onClick={() => setTab("tasks")}
          className={`flex-1 font-pixel text-[9px] py-2 border-2 border-retro-border ${
            tab === "tasks"
              ? "bg-retro-accent text-white"
              : "bg-retro-card text-retro-muted"
          }`}
        >
          ⚔ Tasks ({tasks.length})
        </button>
        <button
          onClick={() => setTab("habits")}
          className={`flex-1 font-pixel text-[9px] py-2 border-2 border-l-0 border-retro-border ${
            tab === "habits"
              ? "bg-retro-accent text-white"
              : "bg-retro-card text-retro-muted"
          }`}
        >
          🔥 Habits ({habits.length})
        </button>
      </div>

      {/* Add button */}
      <div className="flex justify-between items-center mb-4">
        <button
          onClick={() => setShowForm((s) => !s)}
          className="pixel-btn flex items-center gap-1 text-[8px]"
        >
          <Plus size={12} /> Add {tab === "tasks" ? "Task" : "Habit"}
        </button>
        <button
          onClick={loadData}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <RefreshCw size={12} />
        </button>
      </div>

      {/* Form */}
      {showForm && (
        <form
          onSubmit={addTask}
          className="pixel-border bg-retro-surface p-4 mb-4 animate-pixel-fade-in space-y-2"
        >
          <input
            className="pixel-input w-full"
            placeholder={tab === "tasks" ? "Quest title..." : "Habit name..."}
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
          />
          {tab === "tasks" && (
            <input
              className="pixel-input w-full"
              placeholder="Description (optional)"
              value={form.description}
              onChange={(e) =>
                setForm({ ...form, description: e.target.value })
              }
            />
          )}
          <div className="flex gap-2">
            <input
              className="pixel-input flex-1"
              placeholder="Tag (e.g. coding)"
              value={form.tags}
              onChange={(e) => setForm({ ...form, tags: e.target.value })}
            />
            {tab === "tasks" ? (
              <select
                className="pixel-input"
                value={form.difficulty}
                onChange={(e) =>
                  setForm({ ...form, difficulty: e.target.value })
                }
              >
                <option value="EASY">Easy</option>
                <option value="MEDIUM">Medium</option>
                <option value="HARD">Hard</option>
                <option value="EPIC">Epic</option>
              </select>
            ) : (
              <select
                className="pixel-input"
                value={form.frequency}
                onChange={(e) =>
                  setForm({ ...form, frequency: e.target.value })
                }
              >
                <option value="DAILY">Daily</option>
                <option value="WEEKLY">Weekly</option>
              </select>
            )}
          </div>
          {tab === "tasks" && (
            <input
              type="date"
              className="pixel-input w-full"
              value={form.dueDate}
              onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
              placeholder="Due date (optional)"
            />
          )}
          <button type="submit" className="pixel-btn w-full">
            ✦ Create
          </button>
        </form>
      )}

      {/* List */}
      {loading ? (
        <p className="font-pixel text-[9px] text-retro-muted text-center py-8">
          Loading...
        </p>
      ) : tab === "tasks" ? (
        <div className="space-y-2">
          {tasks.length === 0 && (
            <p className="pixel-border bg-retro-card p-4 font-pixel text-[9px] text-retro-muted text-center">
              No quests yet — add one above!
            </p>
          )}
          {tasks.map((t) => (
            <div
              key={t.id}
              className={`pixel-border-sm bg-retro-card p-3 flex items-center gap-3 ${
                t.completed ? "opacity-50" : ""
              }`}
            >
              <input
                type="checkbox"
                className="retro-checkbox"
                checked={t.completed ?? false}
                onChange={() => toggleTask(t.id)}
              />
              <div className="flex-1">
                <p
                  className={`font-pixel text-[9px] ${t.completed ? "line-through text-retro-muted" : "text-retro-text"}`}
                >
                  {t.title}
                </p>
                <div className="flex gap-2 mt-1 flex-wrap">
                  {t.tags && (
                    <span className="inline-block px-2 py-0.5 bg-retro-accent2/20 text-retro-accent2 font-pixel text-[7px] border border-retro-accent2/40">
                      {t.tags}
                    </span>
                  )}
                  {t.dueDate && (
                    <span className="inline-block px-2 py-0.5 bg-retro-coin/20 text-retro-coin font-pixel text-[7px] border border-retro-coin/40">
                      📅 {t.dueDate}
                    </span>
                  )}
                </div>
              </div>
              <span className="font-pixel text-[7px] text-retro-muted">
                {t.difficulty}
              </span>
              {!t.completed && (
                <button
                  onClick={() => openEditTask(t)}
                  className="text-retro-accent hover:scale-110"
                >
                  <Pencil size={12} />
                </button>
              )}
              <button
                onClick={() => deleteItem("tasks", t.id)}
                className="text-retro-danger hover:scale-110"
              >
                <Trash2 size={12} />
              </button>
            </div>
          ))}
        </div>
      ) : (
        <div className="space-y-2">
          {habits.length === 0 && (
            <p className="pixel-border bg-retro-card p-4 font-pixel text-[9px] text-retro-muted text-center">
              No habits yet — build your streaks!
            </p>
          )}
          {habits.map((h) => (
            <div
              key={h.id}
              className="pixel-border-sm bg-retro-card p-3 flex items-center gap-3"
            >
              <button
                onClick={() => completeHabit(h.id)}
                className="pixel-border-sm bg-retro-xp text-white p-1 hover:scale-110 transition-transform"
                title="Complete for today"
              >
                ✦
              </button>
              <div className="flex-1">
                <p className="font-pixel text-[9px] text-retro-text">
                  {h.title}
                </p>
                {h.tag && (
                  <span className="inline-block mt-1 px-2 py-0.5 bg-retro-xp/20 text-retro-xp font-pixel text-[7px] border border-retro-xp/40">
                    {h.tag}
                  </span>
                )}
              </div>
              <div className="text-right">
                <p className="font-pixel text-[9px] text-retro-accent">
                  🔥 {h.streak ?? 0}
                </p>
                <p className="font-pixel text-[7px] text-retro-muted">
                  {h.frequency}
                </p>
              </div>
              <button
                onClick={() => openEditHabit(h)}
                className="text-retro-accent hover:scale-110"
              >
                <Pencil size={12} />
              </button>
              <button
                onClick={() => deleteItem("habits", h.id)}
                className="text-retro-danger hover:scale-110"
              >
                <Trash2 size={12} />
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Edit Modal */}
      {editItem && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
          <form
            onSubmit={saveEdit}
            className="pixel-border bg-retro-surface p-5 w-full max-w-md space-y-3 animate-pixel-fade-in"
          >
            <div className="flex items-center justify-between mb-2">
              <p className="font-pixel text-[10px] text-retro-text">
                ✏ Edit {editItem.type === "task" ? "Task" : "Habit"}
              </p>
              <button
                type="button"
                onClick={() => setEditItem(null)}
                className="text-retro-muted hover:text-retro-danger"
              >
                <X size={14} />
              </button>
            </div>
            <input
              className="pixel-input w-full"
              value={editItem.title}
              onChange={(e) =>
                setEditItem({ ...editItem, title: e.target.value })
              }
              required
            />
            {editItem.type === "task" && (
              <>
                <input
                  className="pixel-input w-full"
                  placeholder="Description"
                  value={editItem.description}
                  onChange={(e) =>
                    setEditItem({ ...editItem, description: e.target.value })
                  }
                />
                <div className="flex gap-2">
                  <input
                    className="pixel-input flex-1"
                    placeholder="Tags"
                    value={editItem.tags}
                    onChange={(e) =>
                      setEditItem({ ...editItem, tags: e.target.value })
                    }
                  />
                  <select
                    className="pixel-input"
                    value={editItem.difficulty}
                    onChange={(e) =>
                      setEditItem({ ...editItem, difficulty: e.target.value })
                    }
                  >
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                    <option value="EPIC">Epic</option>
                  </select>
                </div>
                <input
                  type="date"
                  className="pixel-input w-full"
                  value={editItem.dueDate}
                  onChange={(e) =>
                    setEditItem({ ...editItem, dueDate: e.target.value })
                  }
                />
              </>
            )}
            {editItem.type === "habit" && (
              <div className="flex gap-2">
                <input
                  className="pixel-input flex-1"
                  placeholder="Tag"
                  value={editItem.tag}
                  onChange={(e) =>
                    setEditItem({ ...editItem, tag: e.target.value })
                  }
                />
                <select
                  className="pixel-input"
                  value={editItem.frequency}
                  onChange={(e) =>
                    setEditItem({ ...editItem, frequency: e.target.value })
                  }
                >
                  <option value="DAILY">Daily</option>
                  <option value="WEEKLY">Weekly</option>
                </select>
              </div>
            )}
            <button type="submit" className="pixel-btn w-full">
              ✦ Save
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
