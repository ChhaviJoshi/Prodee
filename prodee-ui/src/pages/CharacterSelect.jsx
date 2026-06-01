import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import AVATARS from "../data/avatars";
import { Check, ChevronRight } from "lucide-react";

const PROPS = [
  { id: "none", label: "None" },
  { id: "sword", label: "⚔ Sword" },
  { id: "shield", label: "🛡 Shield" },
  { id: "crown", label: "👑 Crown" },
  { id: "cape", label: "🧣 Cape" },
];

export default function CharacterSelect() {
  const [selected, setSelected] = useState(null);
  const [prop, setProp] = useState("none");
  const { setUser, user } = useAuth();
  const navigate = useNavigate();

  function handleConfirm() {
    // Store avatar choice locally (would POST to /api/profile in real app)
    localStorage.setItem(
      "prodee-avatar",
      JSON.stringify({ avatarId: selected, prop }),
    );
    if (setUser && user) {
      setUser({ ...user, avatarId: selected, prop });
    }
    navigate("/");
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-10">
      <div className="pixel-border bg-retro-surface p-6 animate-pixel-fade-in">
        <h1 className="font-pixel text-sm text-retro-accent text-center mb-2">
          Choose Your Hero
        </h1>
        <p className="font-pixel text-[8px] text-retro-muted text-center mb-6">
          Select an avatar and equip a starting prop
        </p>

        {/* Avatar grid */}
        <div className="grid grid-cols-5 gap-3 mb-6">
          {AVATARS.map((av) => (
            <button
              key={av.id}
              onClick={() => setSelected(av.id)}
              className={`pixel-border-sm p-3 flex flex-col items-center gap-2 transition-all ${
                selected === av.id
                  ? "bg-retro-accent/20 scale-105"
                  : "bg-retro-card hover:bg-retro-input"
              }`}
            >
              <div
                className="w-16 h-16"
                dangerouslySetInnerHTML={{ __html: av.sprite }}
              />
              <span className="font-pixel text-[7px] text-retro-text">
                {av.name}
              </span>
              {selected === av.id && (
                <Check size={12} className="text-retro-xp" />
              )}
            </button>
          ))}
        </div>

        {/* Props */}
        {selected && (
          <div className="mb-6 animate-pixel-fade-in">
            <p className="font-pixel text-[8px] text-retro-muted mb-2">
              Equip a starting prop:
            </p>
            <div className="flex flex-wrap gap-2">
              {PROPS.map((p) => (
                <button
                  key={p.id}
                  onClick={() => setProp(p.id)}
                  className={`pixel-border-sm px-3 py-1.5 font-pixel text-[8px] transition-colors ${
                    prop === p.id
                      ? "bg-retro-accent text-white"
                      : "bg-retro-card text-retro-text hover:bg-retro-input"
                  }`}
                >
                  {p.label}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Preview and Confirm */}
        {selected && (
          <div className="flex items-center justify-between animate-pixel-fade-in">
            <div className="flex items-center gap-3">
              <div
                className="w-12 h-12"
                dangerouslySetInnerHTML={{
                  __html: AVATARS.find((a) => a.id === selected)?.sprite ?? "",
                }}
              />
              <div>
                <p className="font-pixel text-[9px] text-retro-text">
                  {AVATARS.find((a) => a.id === selected)?.name}
                </p>
                <p className="font-pixel text-[7px] text-retro-muted">
                  Prop: {PROPS.find((p) => p.id === prop)?.label}
                </p>
              </div>
            </div>
            <button
              onClick={handleConfirm}
              className="pixel-btn flex items-center gap-1"
            >
              Start Quest <ChevronRight size={12} />
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
