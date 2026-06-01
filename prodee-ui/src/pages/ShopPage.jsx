import { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { ShoppingBag, ArrowLeft, Coins, Sticker } from "lucide-react";
import { apiGet, apiPost } from "../utils/api";
import { useAuth } from "../context/AuthContext";
import {
  getItemIcon,
  getStickerIcon,
  isNatureSticker,
} from "../utils/iconMapper";

export default function ShopPage() {
  const navigate = useNavigate();
  const { user, setUser } = useAuth();
  const [items, setItems] = useState([]);
  const [stickers, setStickers] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    apiGet("/api/gamification/shop")
      .then((data) => setItems(Array.isArray(data) ? data : []))
      .catch(() => setItems([]));

    apiGet("/api/gamification/stickers/shop")
      .then((data) => setStickers(Array.isArray(data) ? data : []))
      .catch(() => setStickers([]));
  }, []);

  const classicStickers = useMemo(
    () =>
      stickers.filter(
        (sticker) => !isNatureSticker(sticker.name, sticker.imageUrl),
      ),
    [stickers],
  );

  const natureStickers = useMemo(
    () =>
      stickers.filter((sticker) =>
        isNatureSticker(sticker.name, sticker.imageUrl),
      ),
    [stickers],
  );

  const stickerPacks = useMemo(() => {
    const packs = [
      {
        id: "classic",
        name: "Classic Sticker Pack",
        subtitle: "Star, Heart, Spark",
        icon: "🎴",
        stickers: classicStickers,
      },
      {
        id: "nature",
        name: "Nature Sticker Pack",
        subtitle: "Trees, Pinecone, Branches",
        icon: "🌿",
        stickers: natureStickers,
      },
    ];

    return packs
      .map((pack) => ({
        ...pack,
        price: pack.stickers.reduce(
          (sum, sticker) => sum + Number(sticker.price || 0),
          0,
        ),
      }))
      .filter((pack) => pack.stickers.length > 0);
  }, [classicStickers, natureStickers]);

  async function buyItem(item) {
    const coins = user?.coins ?? 0;
    if (coins < item.price) {
      setMessage("Not enough coins! Complete quests to earn more.");
      setTimeout(() => setMessage(""), 3000);
      return;
    }

    try {
      await apiPost(`/api/gamification/shop/buy/${item.id}`);
      if (user) setUser({ ...user, coins: coins - item.price });
      setMessage(`✦ Purchased ${item.name}!`);
    } catch {
      setMessage(`Unable to purchase ${item.name}.`);
    }
    setTimeout(() => setMessage(""), 3000);
  }

  async function buySticker(sticker) {
    const coins = user?.coins ?? 0;
    if (coins < sticker.price) {
      setMessage("Not enough coins! Complete quests to earn more.");
      setTimeout(() => setMessage(""), 3000);
      return;
    }

    try {
      await apiPost(`/api/gamification/stickers/buy/${sticker.id}`);
      if (user) setUser({ ...user, coins: coins - sticker.price });
      setMessage(`✦ Purchased ${sticker.name}!`);
    } catch {
      setMessage(`Unable to purchase ${sticker.name}.`);
    }
    setTimeout(() => setMessage(""), 3000);
  }

  async function buyStickerPack(pack) {
    const coins = user?.coins ?? 0;
    if (!pack?.stickers?.length) {
      setMessage("No stickers found in this pack.");
      setTimeout(() => setMessage(""), 3000);
      return;
    }
    if (coins < pack.price) {
      setMessage("Not enough coins! Complete quests to earn more.");
      setTimeout(() => setMessage(""), 3000);
      return;
    }

    try {
      for (const sticker of pack.stickers) {
        await apiPost(`/api/gamification/stickers/buy/${sticker.id}`);
      }
      if (user) setUser({ ...user, coins: coins - pack.price });
      setMessage(`✦ Purchased ${pack.name}!`);
    } catch {
      setMessage(`Unable to purchase ${pack.name}.`);
    }
    setTimeout(() => setMessage(""), 3000);
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-6">
      {/* Header */}
      <div className="flex items-center gap-3 mb-5">
        <button
          onClick={() => navigate("/")}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <ArrowLeft size={14} />
        </button>
        <ShoppingBag size={20} className="text-retro-coin" />
        <h1 className="font-pixel text-sm text-retro-text">The Shop</h1>
        <div className="flex-1" />
        <span className="font-pixel text-[9px] text-retro-coin">
          🪙 {user?.coins ?? 0} Coins
        </span>
      </div>

      {message && (
        <div
          className={`pixel-border-sm p-2 mb-4 font-pixel text-[8px] text-center animate-pixel-fade-in ${
            message.includes("Not enough")
              ? "bg-retro-danger/10 text-retro-danger"
              : "bg-retro-xp/10 text-retro-xp"
          }`}
        >
          {message}
        </div>
      )}

      {/* Item grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 stagger-children mb-6">
        {items.map((item) => (
          <div
            key={item.id}
            className="pixel-border bg-retro-card p-4 flex flex-col items-center text-center pixel-card-hover"
          >
            <div className="text-3xl leading-none mb-3">
              {getItemIcon(item.name, item.category)}
            </div>
            <h3 className="font-pixel text-[9px] text-retro-text mb-1">
              {item.name}
            </h3>
            <p className="font-pixel text-[7px] text-retro-muted mb-3 leading-relaxed">
              {item.description}
            </p>
            <div className="flex items-center gap-1 mb-3">
              <Coins size={10} className="text-retro-coin" />
              <span className="font-pixel text-[9px] text-retro-coin">
                {item.price}
              </span>
            </div>
            <button
              onClick={() => buyItem(item)}
              className="pixel-btn w-full text-[7px]"
              style={{ background: "var(--color-retro-coin)", color: "#333" }}
            >
              Buy
            </button>
          </div>
        ))}
      </div>

      <div className="pixel-border bg-retro-surface p-4">
        <div className="flex items-center gap-2 mb-3">
          <Sticker size={14} className="text-retro-accent2" />
          <p className="font-pixel text-[8px] text-retro-muted">
            SCRAPBOOK STICKERS
          </p>
        </div>

        {stickerPacks.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-4">
            {stickerPacks.map((pack) => (
              <div
                key={pack.id}
                className="pixel-border-sm bg-retro-card p-3 flex flex-col gap-2"
              >
                <div className="flex items-center gap-2">
                  <span className="text-xl leading-none">{pack.icon}</span>
                  <div>
                    <p className="font-pixel text-[8px] text-retro-text">
                      {pack.name}
                    </p>
                    <p className="font-pixel text-[6px] text-retro-muted">
                      {pack.subtitle}
                    </p>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="font-pixel text-[7px] text-retro-muted">
                    {pack.stickers.length} stickers
                  </span>
                  <span className="font-pixel text-[8px] text-retro-coin">
                    🪙 {pack.price}
                  </span>
                </div>
                <button
                  onClick={() => buyStickerPack(pack)}
                  className="pixel-btn w-full text-[7px]"
                  style={{
                    background: "var(--color-retro-coin)",
                    color: "#333",
                  }}
                >
                  Buy Pack
                </button>
              </div>
            ))}
          </div>
        )}

        <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
          {stickers.map((sticker) => (
            <div
              key={sticker.id}
              className="pixel-border bg-retro-card p-4 text-center"
            >
              <div className="mb-2 min-h-[50px] flex items-center justify-center relative">
                <span className="text-3xl leading-none">
                  {getStickerIcon(sticker.name, sticker.imageUrl)}
                </span>
                {isNatureSticker(sticker.name, sticker.imageUrl) ? (
                  <span className="absolute top-0 right-0 font-pixel text-[5px] px-1 py-0.5 bg-retro-accent2 text-white">
                    NATURE
                  </span>
                ) : null}
              </div>
              <h3 className="font-pixel text-[8px] text-retro-text mb-1">
                {sticker.name}
              </h3>
              <div className="flex items-center justify-center gap-1 mb-3">
                <Coins size={10} className="text-retro-coin" />
                <span className="font-pixel text-[8px] text-retro-coin">
                  {sticker.price}
                </span>
              </div>
              <button
                onClick={() => buySticker(sticker)}
                className="pixel-btn w-full text-[7px]"
                style={{ background: "var(--color-retro-accent2)" }}
              >
                Buy Sticker
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
