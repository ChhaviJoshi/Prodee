import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, UserCircle2, Coins, Star, Shield } from "lucide-react";
import { apiGet } from "../utils/api";
import AVATARS from "../data/avatars";
import { getItemIcon } from "../utils/iconMapper";

const LOADOUT_KEY = "prodee-avatar-loadout";

function readLoadout() {
  try {
    const raw = localStorage.getItem(LOADOUT_KEY);
    return raw ? JSON.parse(raw) : { avatarId: "knight", equippedItemId: null };
  } catch {
    return { avatarId: "knight", equippedItemId: null };
  }
}

export default function ProfilePage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [status, setStatus] = useState(null);
  const [inventory, setInventory] = useState([]);
  const [shopItems, setShopItems] = useState([]);
  const [loadout, setLoadout] = useState(readLoadout());

  useEffect(() => {
    apiGet("/api/users/me")
      .then((data) => setProfile(data))
      .catch(() => {});

    apiGet("/api/gamification/status")
      .then((data) => setStatus(data))
      .catch(() => {});

    apiGet("/api/gamification/inventory")
      .then((data) => setInventory(Array.isArray(data) ? data : []))
      .catch(() => setInventory([]));

    apiGet("/api/gamification/shop")
      .then((data) => setShopItems(Array.isArray(data) ? data : []))
      .catch(() => setShopItems([]));
  }, []);

  const itemById = useMemo(() => {
    const map = {};
    shopItems.forEach((item) => {
      map[item.id] = item;
    });
    return map;
  }, [shopItems]);

  const avatarItems = useMemo(
    () =>
      inventory.filter((inv) => {
        const item = itemById[inv.itemId];
        return (
          inv.category === "AVATAR_PROP" ||
          inv.category === "BADGE" ||
          (item && item.category === "AVATAR_PROP")
        );
      }),
    [inventory, itemById],
  );

  function saveLoadout(next) {
    setLoadout(next);
    localStorage.setItem(LOADOUT_KEY, JSON.stringify(next));
  }

  const level = status?.level ?? profile?.level ?? 1;
  const xp = status?.xp ?? profile?.xp ?? 0;
  const coins = status?.coins ?? profile?.coins ?? 0;
  const selectedAvatar =
    AVATARS.find((a) => a.id === loadout.avatarId) || AVATARS[0];

  return (
    <div className="max-w-6xl mx-auto px-4 py-6">
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => navigate("/")}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <ArrowLeft size={14} />
        </button>
        <UserCircle2 size={20} className="text-retro-accent2" />
        <h1 className="font-pixel text-sm text-retro-text">
          Adventurer Profile
        </h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_1.2fr] gap-4">
        <section className="pixel-border bg-retro-surface p-4">
          <p className="font-pixel text-[8px] text-retro-muted mb-3">
            PLAYER STATS
          </p>
          <div className="space-y-2 font-pixel text-[9px] text-retro-text">
            <p>Username: {profile?.username ?? "Player"}</p>
            <p className="flex items-center gap-1">
              <Star size={12} /> Level: {level}
            </p>
            <p>XP: {xp}</p>
            <p className="flex items-center gap-1 text-retro-coin">
              <Coins size={12} /> Coins: {coins}
            </p>
          </div>

          <div className="mt-4 pixel-border-sm bg-retro-card p-3">
            <p className="font-pixel text-[8px] text-retro-muted mb-2">
              CURRENT AVATAR
            </p>
            <div className="flex items-center gap-3">
              <div
                className="w-16 h-16"
                dangerouslySetInnerHTML={{
                  __html: selectedAvatar?.sprite || "",
                }}
              />
              <div>
                <p className="font-pixel text-[9px]">{selectedAvatar?.name}</p>
                <p className="font-pixel text-[7px] text-retro-muted">
                  Equipped:{" "}
                  {avatarItems.find((i) => i.itemId === loadout.equippedItemId)
                    ?.itemName || "None"}
                </p>
              </div>
            </div>
          </div>
        </section>

        <section className="pixel-border bg-retro-surface p-4">
          <p className="font-pixel text-[8px] text-retro-muted mb-3">
            INVENTORY
          </p>
          {inventory.length === 0 ? (
            <p className="font-pixel text-[8px] text-retro-muted">
              Inventory is empty. Visit the shop to buy gear.
            </p>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
              {inventory.map((item) => (
                <div
                  key={item.inventoryId}
                  className="pixel-border-sm bg-retro-card p-2.5"
                >
                  <div className="text-2xl leading-none mb-2">
                    {getItemIcon(item.itemName, item.category)}
                  </div>
                  <p className="font-pixel text-[8px] text-retro-text">
                    {item.itemName}
                  </p>
                  <p className="font-pixel text-[7px] text-retro-muted">
                    x{item.quantity}
                  </p>
                </div>
              ))}
            </div>
          )}

          <div className="mt-5">
            <p className="font-pixel text-[8px] text-retro-muted mb-3">
              AVATAR CUSTOMIZATION
            </p>

            <div className="grid grid-cols-3 sm:grid-cols-5 gap-2 mb-4">
              {AVATARS.map((avatar) => (
                <button
                  key={avatar.id}
                  onClick={() =>
                    saveLoadout({ ...loadout, avatarId: avatar.id })
                  }
                  className={`pixel-border-sm p-2 bg-retro-card ${
                    loadout.avatarId === avatar.id
                      ? "ring-2 ring-retro-accent"
                      : ""
                  }`}
                >
                  <div
                    className="w-10 h-10 mx-auto"
                    dangerouslySetInnerHTML={{ __html: avatar.sprite }}
                  />
                  <p className="font-pixel text-[6px] mt-1">{avatar.name}</p>
                </button>
              ))}
            </div>

            <p className="font-pixel text-[7px] text-retro-muted mb-2">
              Equip owned props
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {avatarItems.length === 0 && (
                <p className="font-pixel text-[7px] text-retro-muted">
                  No avatar props owned yet.
                </p>
              )}
              {avatarItems.map((inv) => {
                const shopItem = itemById[inv.itemId];
                const levelRequired = shopItem?.levelRequired ?? 1;
                const locked = level < levelRequired;

                return (
                  <button
                    key={inv.inventoryId}
                    disabled={locked}
                    onClick={() =>
                      saveLoadout({ ...loadout, equippedItemId: inv.itemId })
                    }
                    className={`pixel-border-sm p-2 text-left ${
                      loadout.equippedItemId === inv.itemId
                        ? "bg-retro-accent text-white"
                        : "bg-retro-card"
                    } ${locked ? "opacity-50 cursor-not-allowed" : "hover:bg-retro-input"}`}
                  >
                    <p className="font-pixel text-[8px]">{inv.itemName}</p>
                    <p className="font-pixel text-[7px] text-retro-muted flex items-center gap-1">
                      <Shield size={10} /> Needs Level {levelRequired}
                    </p>
                  </button>
                );
              })}
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
