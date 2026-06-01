import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import DashboardAnalytics from "../components/DashboardAnalytics";
import {
  Swords,
  CalendarDays,
  Timer,
  Grid3X3,
  BarChart3,
  ShoppingBag,
  Newspaper,
  Flame,
  Users,
  BookHeart,
} from "lucide-react";

const MODULES = [
  {
    key: "tasks",
    title: "Quest Board",
    subtitle: "Tasks & Habits",
    icon: Swords,
    color: "bg-retro-accent",
    path: "/tasks",
    size: "large",
    description:
      "Track daily quests, manage habits, and earn XP for every completion.",
  },
  {
    key: "focus",
    title: "Focus Forge",
    subtitle: "Pomodoro Timer",
    icon: Timer,
    color: "bg-retro-accent2",
    path: "/focus",
    size: "medium",
    description: "Forge focus with ambient RPG sounds and timed sessions.",
  },
  {
    key: "journal",
    title: "Year in Pixels",
    subtitle: "Pixel Journal",
    icon: Grid3X3,
    color: "bg-retro-xp",
    path: "/journal",
    size: "medium",
    description: "Color each day — track moods and moments across the year.",
  },
  {
    key: "scrapbook",
    title: "Digital Diary",
    subtitle: "Scrapbook Memories",
    icon: BookHeart,
    color: "bg-retro-danger",
    path: "/scrapbook",
    size: "medium",
    description:
      "Write diary pages, upload memories, and decorate them with stickers.",
  },
  {
    key: "calendar",
    title: "Countdown Map",
    subtitle: "Deadline Calendar",
    icon: CalendarDays,
    color: "bg-retro-coin",
    path: "/calendar",
    size: "small",
    description: "Set milestones and see days remaining at a glance.",
  },
  {
    key: "analytics",
    title: "Stats Tavern",
    subtitle: "Daily Analytics",
    icon: BarChart3,
    color: "bg-retro-danger",
    path: "/analytics",
    size: "large",
    description:
      "Log sleep, screen time, and more — visualize lifestyle trends.",
  },
  {
    key: "feed",
    title: "Smart Feed",
    subtitle: "Articles & News",
    icon: Newspaper,
    color: "bg-retro-accent2",
    path: "/feed",
    size: "small",
    description: "Dev.to articles curated around your active goals.",
  },
  {
    key: "shop",
    title: "The Shop",
    subtitle: "Spend Coins",
    icon: ShoppingBag,
    color: "bg-retro-coin",
    path: "/shop",
    size: "small",
    description: "Buy avatar props and power-ups with earned coins.",
  },
  {
    key: "cohorts",
    title: "Cohort Arena",
    subtitle: "Multiplayer Board",
    icon: Users,
    color: "bg-retro-accent2",
    path: "/cohorts",
    size: "small",
    description:
      "Create or join cohorts and compete on daily and weekly progress.",
  },
];

function sizeClasses(size) {
  switch (size) {
    case "large":
      return "col-span-2 row-span-2 min-h-[220px]";
    case "medium":
      return "col-span-1 row-span-2 min-h-[220px]";
    default:
      return "col-span-1 row-span-1 min-h-[120px]";
  }
}

export default function Dashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <div className="max-w-6xl mx-auto px-4 py-6">
      {/* Hero greeting */}
      <div className="mb-6 flex items-center gap-3">
        <Flame size={20} className="text-retro-accent animate-pixel-bounce" />
        <div>
          <h1 className="font-pixel text-sm text-retro-text">
            Welcome back, {user?.username ?? "Adventurer"}!
          </h1>
          <p className="font-pixel text-[8px] text-retro-muted mt-1">
            Choose your quest from the map below
          </p>
        </div>
      </div>

      {/* Game Map Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 auto-rows-auto gap-4 stagger-children">
        {MODULES.map((mod) => {
          const Icon = mod.icon;
          return (
            <button
              key={mod.key}
              onClick={() => navigate(mod.path)}
              className={`${sizeClasses(mod.size)} pixel-border bg-retro-card pixel-card-hover text-left p-4 flex flex-col justify-between group cursor-pointer`}
            >
              {/* Top: Icon + Title */}
              <div>
                <div
                  className={`${mod.color} w-10 h-10 flex items-center justify-center text-white pixel-border-sm mb-3 group-hover:animate-pixel-bounce`}
                >
                  <Icon size={18} />
                </div>
                <h2 className="font-pixel text-[11px] text-retro-text leading-relaxed">
                  {mod.title}
                </h2>
                <p className="font-pixel text-[7px] text-retro-muted mt-1">
                  {mod.subtitle}
                </p>
              </div>

              {mod.key === "analytics" ? (
                <div className="mt-3">
                  <DashboardAnalytics compact />
                </div>
              ) : null}

              {/* Bottom: Description */}
              {mod.key !== "analytics" ? (
                <p className="font-pixel text-[7px] text-retro-muted mt-3 leading-relaxed opacity-70 group-hover:opacity-100 transition-opacity">
                  {mod.description}
                </p>
              ) : (
                <p className="font-pixel text-[6px] text-retro-muted mt-2 leading-relaxed opacity-80">
                  Peek at your latest sleep, screen, hydration, and movement
                  trends.
                </p>
              )}

              {/* Bottom accent bar */}
              <div
                className={`${mod.color} h-1 mt-3 w-0 group-hover:w-full transition-all duration-300`}
              />
            </button>
          );
        })}
      </div>
    </div>
  );
}
