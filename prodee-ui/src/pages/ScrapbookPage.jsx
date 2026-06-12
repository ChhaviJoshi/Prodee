import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  BookHeart,
  ImagePlus,
  Save,
  Sticker,
  Trash2,
} from "lucide-react";
import { apiDelete, apiGet, apiPostForm, apiPutForm } from "../utils/api";
import { getStickerIcon, isNatureSticker } from "../utils/iconMapper";

function createStickerPlacement(stickerId, index) {
  const col = index % 5;
  const row = Math.floor(index / 5);
  return { stickerId, x: 14 + col * 42, y: 12 + row * 38 };
}

export default function ScrapbookPage() {
  const navigate = useNavigate();
  const [entries, setEntries] = useState([]);
  const [stickers, setStickers] = useState([]);
  const [activeEntryId, setActiveEntryId] = useState(null);
  const [draft, setDraft] = useState({
    title: "",
    content: "",
    placedStickers: [],
  });
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState("");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [draggingStickerId, setDraggingStickerId] = useState(null);
  const [isDraggingPlaced, setIsDraggingPlaced] = useState(false);
  const canvasRef = useRef(null);

  const activeEntry = useMemo(
    () => entries.find((entry) => entry.id === activeEntryId) || null,
    [entries, activeEntryId],
  );

  const stickerGroups = useMemo(() => {
    const owned = Array.isArray(stickers) ? stickers : [];
    return {
      nature: owned.filter((sticker) =>
        isNatureSticker(sticker.stickerName, sticker.imageUrl),
      ),
      classic: owned.filter(
        (sticker) => !isNatureSticker(sticker.stickerName, sticker.imageUrl),
      ),
    };
  }, [stickers]);

  async function loadData() {
    const [entryData, stickerData] = await Promise.all([
      apiGet("/api/journal/scrapbook").catch(() => []),
      apiGet("/api/gamification/stickers/inventory").catch(() => []),
    ]);
    const normalizedEntries = Array.isArray(entryData) ? entryData : [];
    const normalizedStickers = Array.isArray(stickerData) ? stickerData : [];
    setEntries(normalizedEntries);
    setStickers(normalizedStickers);
    if (!activeEntryId && normalizedEntries.length > 0) {
      selectEntry(normalizedEntries[0]);
    }
  }

  useEffect(() => {
    loadData().catch(() => {});
  }, []);

  function selectEntry(entry) {
    setActiveEntryId(entry.id);
    setDraft({
      title: entry.title || "",
      content: entry.content || "",
      placedStickers: Array.isArray(entry.placedStickers)
        ? entry.placedStickers
        : [],
    });
    setImagePreview(entry.imageUrl || "");
    setImageFile(null);
  }

  function startNewPage() {
    setActiveEntryId(null);
    setDraft({ title: "", content: "", placedStickers: [] });
    setImageFile(null);
    setImagePreview("");
  }

  function appendSticker(stickerId) {
    setDraft((current) => ({
      ...current,
      placedStickers: [
        ...current.placedStickers,
        createStickerPlacement(stickerId, current.placedStickers.length),
      ],
    }));
  }

  function onStickerDragStart(event, stickerId) {
    event.dataTransfer.setData("application/json", JSON.stringify({ type: "new", stickerId }));
    event.dataTransfer.effectAllowed = "copy";
    setDraggingStickerId(stickerId);
  }

  function onPlacedStickerDragStart(event, index) {
    event.dataTransfer.setData("application/json", JSON.stringify({ type: "placed", index }));
    event.dataTransfer.effectAllowed = "move";
    setIsDraggingPlaced(true);
    setDraggingStickerId(`placed-${index}`);
  }

  function onPlacedStickerDragEnd() {
    setIsDraggingPlaced(false);
    setDraggingStickerId(null);
  }

  async function backgroundUpdateStickers(newPlacedStickers, currentDraft, entryId) {
    if (!entryId) return;
    const form = new FormData();
    form.append("title", currentDraft.title || "Untitled Memory");
    form.append("content", currentDraft.content || "");
    form.append("placedStickers", JSON.stringify(newPlacedStickers || []));
    
    try {
      await apiPutForm(`/api/journal/scrapbook/${entryId}`, form);
    } catch (err) {
      console.error("Failed to auto-save sticker position", err);
    }
  }

  function onCanvasDrop(event) {
    event.preventDefault();

    const rect = canvasRef.current?.getBoundingClientRect();
    if (!rect) {
      setDraggingStickerId(null);
      setIsDraggingPlaced(false);
      return;
    }

    const size = 28;
    const x = Math.max(
      0,
      Math.min(rect.width - size, event.clientX - rect.left - size / 2),
    );
    const y = Math.max(
      0,
      Math.min(rect.height - size, event.clientY - rect.top - size / 2),
    );

    let dragData;
    try {
      const textData = event.dataTransfer.getData("application/json");
      if (textData) {
        dragData = JSON.parse(textData);
      }
    } catch (err) {
      console.error("Failed to parse drag data", err);
    }

    // fallback for older code if dataTransfer is empty or different
    const placedIndexRaw = event.dataTransfer.getData("text/placed-sticker-index");
    const newStickerRaw = event.dataTransfer.getData("text/sticker-id");

    if (dragData?.type === "placed" || (placedIndexRaw !== null && placedIndexRaw !== "")) {
      const idx = dragData?.type === "placed" ? dragData.index : Number(placedIndexRaw);
      if (idx >= 0) {
        setDraft((current) => {
          const arr = [...current.placedStickers];
          if (arr[idx]) {
            arr[idx] = { ...arr[idx], x: Math.round(x), y: Math.round(y) };
          }
          backgroundUpdateStickers(arr, current, activeEntryId);
          return { ...current, placedStickers: arr };
        });
      }
      setDraggingStickerId(null);
      setIsDraggingPlaced(false);
      return;
    }

    if (dragData?.type === "new" || (newStickerRaw !== null && newStickerRaw !== "")) {
      const stickerId = dragData?.type === "new" ? dragData.stickerId : Number(newStickerRaw);
      if (stickerId != null && !isNaN(stickerId)) {
        setDraft((current) => {
          const newStickers = [
            ...current.placedStickers,
            { stickerId, x: Math.round(x), y: Math.round(y) },
          ];
          backgroundUpdateStickers(newStickers, current, activeEntryId);
          return { ...current, placedStickers: newStickers };
        });
      }
    }
    
    setDraggingStickerId(null);
    setIsDraggingPlaced(false);
  }

  function onCanvasDragOver(event) {
    event.preventDefault();
    event.dataTransfer.dropEffect = isDraggingPlaced ? "move" : "copy";
  }

  async function saveEntry(e) {
    e.preventDefault();
    setSaving(true);
    setMessage("");

    const form = new FormData();
    form.append("title", draft.title || "Untitled Memory");
    form.append("content", draft.content || "");
    form.append("placedStickers", JSON.stringify(draft.placedStickers || []));
    if (imageFile) {
      form.append("image", imageFile);
    }

    try {
      const saved = activeEntryId
        ? await apiPutForm(`/api/journal/scrapbook/${activeEntryId}`, form)
        : await apiPostForm("/api/journal/scrapbook", form);

      setMessage(activeEntryId ? "Diary page updated." : "Diary page saved.");
      await loadData();
      if (saved?.id) {
        const refreshed =
          (await apiGet(`/api/journal/scrapbook/${saved.id}`).catch(
            () => saved,
          )) || saved;
        selectEntry(refreshed);
      }
    } catch (err) {
      setMessage(err.message || "Unable to save diary page.");
    } finally {
      setSaving(false);
    }
  }

  async function deleteEntry(id) {
    try {
      await apiDelete(`/api/journal/scrapbook/${id}`);
      setMessage("Diary page deleted.");
      if (activeEntryId === id) {
        startNewPage();
      }
      await loadData();
    } catch (err) {
      setMessage(err.message || "Unable to delete entry.");
    }
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-6">
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => navigate("/")}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <ArrowLeft size={14} />
        </button>
        <BookHeart size={20} className="text-retro-accent" />
        <h1 className="font-pixel text-sm text-retro-text">Digital Diary</h1>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-[1.05fr_0.95fr] gap-4">
        <section className="pixel-border bg-retro-surface p-4">
          <div className="flex items-center justify-between gap-2 mb-4">
            <p className="font-pixel text-[8px] text-retro-muted">
              TODAY'S PAGE
            </p>
            <button
              onClick={startNewPage}
              className="pixel-border-sm px-2 py-1 bg-retro-card hover:bg-retro-input font-pixel text-[7px]"
            >
              New Page
            </button>
          </div>

          <form onSubmit={saveEntry} className="space-y-3">
            <input
              className="pixel-input w-full"
              placeholder="Entry title"
              value={draft.title}
              onChange={(e) =>
                setDraft((current) => ({ ...current, title: e.target.value }))
              }
              required
            />

            <div
              ref={canvasRef}
              onDrop={onCanvasDrop}
              onDragOver={onCanvasDragOver}
              className={`pixel-border-sm bg-[#fff9ef] min-h-[320px] relative overflow-hidden ${draggingStickerId !== null ? "ring-2 ring-retro-accent2" : ""}`}
            >
              <textarea
                className={`absolute inset-0 z-10 w-full h-full bg-transparent resize-none outline-none font-mono text-sm text-retro-text p-3 leading-7 ${(draggingStickerId !== null || isDraggingPlaced) ? "pointer-events-none" : ""}`}
                placeholder="Write today's memory..."
                value={draft.content}
                onChange={(e) =>
                  setDraft((current) => ({
                    ...current,
                    content: e.target.value,
                  }))
                }
              />

              {draft.placedStickers.map((placement, index) => {
                const sticker = stickers.find(
                  (item) => item.stickerId === placement.stickerId,
                );
                if (!sticker) return null;
                return (
                  <span
                    key={`${placement.stickerId}-${index}`}
                    draggable={true}
                    onDragStart={(e) => onPlacedStickerDragStart(e, index)}
                    onDragEnd={onPlacedStickerDragEnd}
                    className="absolute z-20 select-none text-2xl leading-none cursor-grab active:cursor-grabbing hover:scale-110 transition-transform"
                    style={{
                      left: `${placement.x}px`,
                      top: `${placement.y}px`,
                    }}
                    aria-label={sticker.stickerName}
                    role="img"
                  >
                    {getStickerIcon(sticker.stickerName, sticker.imageUrl)}
                  </span>
                );
              })}
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-[1fr_auto] gap-3 items-start">
              <label className="pixel-border-sm bg-retro-card hover:bg-retro-input px-3 py-2 font-pixel text-[7px] flex items-center justify-center gap-2 cursor-pointer">
                <ImagePlus size={12} /> Upload Memory
                <input
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={(e) => {
                    const file = e.target.files?.[0] || null;
                    setImageFile(file);
                    setImagePreview(
                      file
                        ? URL.createObjectURL(file)
                        : activeEntry?.imageUrl || "",
                    );
                  }}
                />
              </label>
              <button
                type="submit"
                disabled={saving}
                className="pixel-btn flex items-center justify-center gap-2 px-4"
              >
                <Save size={12} />{" "}
                {saving
                  ? "Saving"
                  : activeEntryId
                    ? "Update Page"
                    : "Save Page"}
              </button>
            </div>

            {imagePreview && (
              <div className="pixel-border-sm bg-retro-card p-2">
                <img
                  src={imagePreview}
                  alt="Diary memory"
                  className="w-full max-h-52 object-cover"
                />
              </div>
            )}

            {message && (
              <p className="font-pixel text-[7px] text-retro-muted">
                {message}
              </p>
            )}
          </form>
        </section>

        <section className="space-y-4">
          <div className="pixel-border bg-retro-surface p-4">
            <p className="font-pixel text-[8px] text-retro-muted mb-3 flex items-center gap-2">
              <Sticker size={12} /> STICKER BOX
            </p>
            {stickers.length === 0 ? (
              <p className="font-pixel text-[7px] text-retro-muted">
                Buy scrapbook stickers in the shop to decorate your pages.
              </p>
            ) : (
              <div className="space-y-4">
                {stickerGroups.nature.length > 0 ? (
                  <div>
                    <p className="font-pixel text-[7px] text-retro-accent2 mb-2">
                      NATURE THEME
                    </p>
                    <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                      {stickerGroups.nature.map((sticker) => (
                        <button
                          key={sticker.inventoryId}
                          onClick={() => appendSticker(sticker.stickerId)}
                          draggable
                          onDragStart={(event) =>
                            onStickerDragStart(event, sticker.stickerId)
                          }
                          onDragEnd={() => setDraggingStickerId(null)}
                          className="pixel-border-sm bg-retro-card hover:bg-retro-input p-2 text-center cursor-grab active:cursor-grabbing"
                        >
                          <div className="mb-2 min-h-[34px] flex items-center justify-center">
                            <span className="text-2xl leading-none">
                              {getStickerIcon(
                                sticker.stickerName,
                                sticker.imageUrl,
                              )}
                            </span>
                          </div>
                          <p className="font-pixel text-[6px] text-retro-text">
                            {sticker.stickerName}
                          </p>
                          <p className="font-pixel text-[6px] text-retro-muted">
                            x{sticker.quantity}
                          </p>
                        </button>
                      ))}
                    </div>
                  </div>
                ) : null}

                {stickerGroups.classic.length > 0 ? (
                  <div>
                    <p className="font-pixel text-[7px] text-retro-muted mb-2">
                      CLASSIC PACK
                    </p>
                    <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                      {stickerGroups.classic.map((sticker) => (
                        <button
                          key={sticker.inventoryId}
                          onClick={() => appendSticker(sticker.stickerId)}
                          draggable
                          onDragStart={(event) =>
                            onStickerDragStart(event, sticker.stickerId)
                          }
                          onDragEnd={() => setDraggingStickerId(null)}
                          className="pixel-border-sm bg-retro-card hover:bg-retro-input p-2 text-center cursor-grab active:cursor-grabbing"
                        >
                          <div className="mb-2 min-h-[34px] flex items-center justify-center">
                            <span className="text-2xl leading-none">
                              {getStickerIcon(
                                sticker.stickerName,
                                sticker.imageUrl,
                              )}
                            </span>
                          </div>
                          <p className="font-pixel text-[6px] text-retro-text">
                            {sticker.stickerName}
                          </p>
                          <p className="font-pixel text-[6px] text-retro-muted">
                            x{sticker.quantity}
                          </p>
                        </button>
                      ))}
                    </div>
                  </div>
                ) : null}
              </div>
            )}
          </div>

          <div className="pixel-border bg-retro-surface p-4">
            <div className="flex items-center justify-between gap-2 mb-3">
              <p className="font-pixel text-[8px] text-retro-muted">
                MEMORY GALLERY
              </p>
              <span className="font-pixel text-[7px] text-retro-muted">
                {entries.length} pages
              </span>
            </div>
            {entries.length === 0 ? (
              <p className="font-pixel text-[7px] text-retro-muted">
                Your scrapbook is empty. Save your first page.
              </p>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 max-h-[640px] overflow-y-auto pr-1">
                {entries.map((entry) => (
                  <article
                    key={entry.id}
                    className="pixel-border-sm bg-retro-card p-3"
                  >
                    <button
                      onClick={() => selectEntry(entry)}
                      className="w-full text-left"
                    >
                      {entry.imageUrl && (
                        <img
                          src={entry.imageUrl}
                          alt={entry.title}
                          className="w-full h-28 object-cover mb-2"
                        />
                      )}
                      <h3 className="font-pixel text-[8px] text-retro-text mb-1">
                        {entry.title}
                      </h3>
                      <p className="font-pixel text-[7px] text-retro-muted leading-relaxed line-clamp-4">
                        {entry.content || "No text added yet."}
                      </p>
                    </button>
                    <div className="flex items-center justify-between mt-3 gap-2">
                      <span className="font-pixel text-[6px] text-retro-muted">
                        {new Date(entry.createdAt).toLocaleDateString("en-US", {
                          month: "short",
                          day: "numeric",
                        })}
                      </span>
                      <button
                        onClick={() => deleteEntry(entry.id)}
                        className="pixel-border-sm px-2 py-1 bg-retro-danger/10 text-retro-danger hover:bg-retro-danger/20"
                      >
                        <Trash2 size={10} />
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
