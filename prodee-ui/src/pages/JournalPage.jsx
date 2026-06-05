import { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import {
  Grid3X3,
  ArrowLeft,
  Plus,
  Pencil,
  Trash2,
  Columns3,
  Palette,
  BookHeart,
} from "lucide-react";
import { apiGet, apiPost, apiPut, apiDelete } from "../utils/api";
import DeleteConfirmModal from "../components/DeleteConfirmModal";

function getDaysInYear(year) {
  return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0 ? 366 : 365;
}

function getDayOfYear(date) {
  const start = new Date(date.getFullYear(), 0, 0);
  return Math.floor((date - start) / (1000 * 60 * 60 * 24));
}

function dateFromDayOfYear(year, day) {
  const d = new Date(year, 0);
  d.setDate(day);
  return d;
}

function formatLocalDate(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

function parseLocalDate(dateStr) {
  if (Array.isArray(dateStr)) {
    return new Date(dateStr[0], dateStr[1] - 1, dateStr[2]);
  }
  const parts = String(dateStr).split(/[-/]/).map(Number);
  if (parts.length < 3) return null;
  const [y, m, d] = parts;
  if (!y || !m || !d) return null;
  return new Date(y, m - 1, d);
}

function safeParseColorMap(raw) {
  try {
    const parsed = JSON.parse(raw);
    return typeof parsed === "object" && parsed ? parsed : {};
  } catch {
    return {};
  }
}

const MONTHS = [
  "Jan",
  "Feb",
  "Mar",
  "Apr",
  "May",
  "Jun",
  "Jul",
  "Aug",
  "Sep",
  "Oct",
  "Nov",
  "Dec",
];
const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const DEFAULT_COLORS = {
  1: "#f44336",
  2: "#ff9800",
  3: "#ffc107",
  4: "#8bc34a",
  5: "#4caf50",
};

function serializeColorMap(colorMap) {
  return JSON.stringify(
    Object.keys(colorMap)
      .sort((a, b) => Number(a) - Number(b))
      .reduce((acc, key) => {
        acc[key] = colorMap[key];
        return acc;
      }, {}),
  );
}

export default function JournalPage() {
  const navigate = useNavigate();
  const year = new Date().getFullYear();
  const totalDays = getDaysInYear(year);
  const todayIdx = getDayOfYear(new Date()) - 1;
  const fallbackColor = "var(--color-retro-input)";

  const [templates, setTemplates] = useState([]);
  const [activeTemplateId, setActiveTemplateId] = useState(null);
  const [templatePixels, setTemplatePixels] = useState({});
  const [templateColorMaps, setTemplateColorMaps] = useState({});
  const [selectedIntensityByTemplate, setSelectedIntensityByTemplate] = useState({});
  const [activeDayIdxByTemplate, setActiveDayIdxByTemplate] = useState({});
  const [showAllTemplates, setShowAllTemplates] = useState(false);

  const [showNewTemplate, setShowNewTemplate] = useState(false);
  const [showEditTemplate, setShowEditTemplate] = useState(false);
  const [newTemplateName, setNewTemplateName] = useState("");
  const [newTemplateColors, setNewTemplateColors] = useState({
    ...DEFAULT_COLORS,
  });
  const [editTemplateName, setEditTemplateName] = useState("");
  const [editTemplateColors, setEditTemplateColors] = useState({
    ...DEFAULT_COLORS,
  });
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleteBusy, setDeleteBusy] = useState(false);
  const [deleteError, setDeleteError] = useState("");

  const activeTemplate =
    templates.find((template) => template.id === activeTemplateId) || null;

  async function loadTemplatesAndPixels() {
    const data = await apiGet("/api/journal/pixels/templates");
    const list = Array.isArray(data) ? data : [];
    console.log("[PixelJournal] templates payload", list);
    setTemplates(list);

    if (list.length === 0) {
      setActiveTemplateId(null);
      setTemplatePixels({});
      setTemplateColorMaps({});
      return;
    }

    setActiveTemplateId((prev) =>
      prev && list.some((template) => template.id === prev) ? prev : list[0].id,
    );

    const colorMaps = {};
    const selectedIntensities = {};
    list.forEach((template) => {
      const parsed = safeParseColorMap(template.colorMapping);
      console.log("[PixelJournal] parsed colorMapping", {
        templateId: template.id,
        templateName: template.name,
        raw: template.colorMapping,
        parsed,
      });
      colorMaps[template.id] = Object.keys(parsed).length
        ? parsed
        : { ...DEFAULT_COLORS };
      selectedIntensities[template.id] = null; // No default selection
    });
    setTemplateColorMaps(colorMaps);
    setSelectedIntensityByTemplate((current) => ({
      ...selectedIntensities,
      ...current,
    }));

    const entries = await Promise.all(
      list.map(async (template) => {
        try {
          const pixels = await apiGet(
            `/api/journal/pixels/template/${template.id}`,
          );
          console.log("[PixelJournal] pixels payload", {
            templateId: template.id,
            pixels,
          });
          const map = {};
          if (Array.isArray(pixels)) {
            pixels.forEach((pixel) => {
              const d = parseLocalDate(pixel.date);
              if (!d || d.getFullYear() !== year) return;
              map[getDayOfYear(d) - 1] = {
                intensity: pixel.intensity,
                colorHex: pixel.colorHex,
              };
            });
          }
          return [template.id, map];
        } catch {
          return [template.id, {}];
        }
      }),
    );

    setTemplatePixels(Object.fromEntries(entries));
  }

  useEffect(() => {
    loadTemplatesAndPixels().catch((error) => {
      console.log("[PixelJournal] load failed", error);
    });
  }, []);

  useEffect(() => {
    if (!activeTemplate) return;
    setEditTemplateName(activeTemplate.name);
    setEditTemplateColors(
      templateColorMaps[activeTemplate.id] || { ...DEFAULT_COLORS },
    );
  }, [activeTemplate, templateColorMaps]);

  const jan1Day = new Date(year, 0, 1).getDay();
  const totalSlots = totalDays + jan1Day;
  const cols = Math.ceil(totalSlots / 7);

  const monthLabels = useMemo(() => {
    const months = [];
    let lastMonth = -1;

    for (let col = 0; col < cols; col++) {
      const dayIdx = col * 7 - jan1Day;
      if (dayIdx < 0 || dayIdx >= totalDays) continue;
      const d = dateFromDayOfYear(year, dayIdx + 1);
      if (d.getMonth() !== lastMonth) {
        lastMonth = d.getMonth();
        months.push({ col, label: MONTHS[lastMonth] });
      }
    }

    return months;
  }, [cols, jan1Day, totalDays, year]);

  async function paintPixel(templateId, dayIdx, overrideIntensity) {
    const intensity = overrideIntensity ?? selectedIntensityByTemplate[templateId];
    if (intensity == null) return;

    const prev = templatePixels[templateId] || {};
    const selectedHex =
      templateColorMaps?.[templateId]?.[String(intensity)] || fallbackColor;

    const date = dateFromDayOfYear(year, dayIdx + 1);
    const dateStr = formatLocalDate(date);
    const payload = {
      templateId,
      date: dateStr,
      intensity,
    };

    console.log("Sending pixel payload:", payload);

    setTemplatePixels((curr) => ({
      ...curr,
      [templateId]: {
        ...(curr[templateId] || {}),
        [dayIdx]: { intensity, colorHex: selectedHex },
      },
    }));

    try {
      const response = await apiPost("/api/journal/pixels", payload);
      console.log("[PixelJournal] paint response", response);
      setTemplatePixels((curr) => ({
        ...curr,
        [templateId]: {
          ...(curr[templateId] || {}),
          [dayIdx]: {
            intensity: response?.intensity ?? intensity,
            colorHex: response?.colorHex || selectedHex,
          },
        },
      }));
    } catch (error) {
      console.error(
        "[PixelJournal] paint failed:",
        error?.message || "Unknown paint error",
        error,
      );
      setTemplatePixels((curr) => ({ ...curr, [templateId]: prev }));
    }
  }

  async function handleBlockClick(templateId, dayIdx) {
    setActiveDayIdxByTemplate((curr) => ({ ...curr, [templateId]: dayIdx }));
    const intensity = selectedIntensityByTemplate[templateId];
    if (intensity) {
      await paintPixel(templateId, dayIdx, intensity);
    }
  }

  async function handleColorClick(templateId, intensityKey) {
    const numericIntensity = Number(intensityKey);
    setSelectedIntensityByTemplate((current) => ({
      ...current,
      [templateId]: numericIntensity,
    }));

    const activeDayIdx = activeDayIdxByTemplate[templateId];
    if (activeDayIdx !== undefined) {
      await paintPixel(templateId, activeDayIdx, numericIntensity);
      setActiveDayIdxByTemplate((curr) => {
        const next = { ...curr };
        delete next[templateId];
        return next;
      });
    }
  }

  async function createTemplate(e) {
    e.preventDefault();
    try {
      await apiPost("/api/journal/pixels/templates", {
        name: newTemplateName,
        colorMapping: serializeColorMap(newTemplateColors),
      });
      setShowNewTemplate(false);
      setNewTemplateName("");
      setNewTemplateColors({ ...DEFAULT_COLORS });
      await loadTemplatesAndPixels();
    } catch (err) {
      alert(err.message);
    }
  }

  async function updateTemplate(e) {
    e.preventDefault();
    if (!activeTemplate) return;

    try {
      const updatedMap = { ...editTemplateColors };
      await apiPut(`/api/journal/pixels/templates/${activeTemplate.id}`, {
        name: editTemplateName,
        colorMapping: serializeColorMap(updatedMap),
      });

      setTemplateColorMaps((current) => ({
        ...current,
        [activeTemplate.id]: updatedMap,
      }));

      setTemplatePixels((current) => {
        const byDay = current[activeTemplate.id] || {};
        const remapped = Object.keys(byDay).reduce((acc, day) => {
          const pixelData = byDay[day];
          const intensity = Number(pixelData?.intensity || 0);
          acc[day] = {
            intensity,
            colorHex:
              updatedMap[String(intensity)] ||
              pixelData?.colorHex ||
              fallbackColor,
          };
          return acc;
        }, {});

        return {
          ...current,
          [activeTemplate.id]: remapped,
        };
      });

      setShowEditTemplate(false);
      await loadTemplatesAndPixels();
    } catch (err) {
      alert(err.message);
    }
  }

  async function confirmDeleteTemplate() {
    if (!deleteTarget) return;
    setDeleteBusy(true);
    setDeleteError("");
    try {
      await apiDelete(`/api/journal/pixels/templates/${deleteTarget.id}`);
      setTemplates((current) =>
        current.filter((template) => template.id !== deleteTarget.id),
      );
      setTemplatePixels((current) => {
        const next = { ...current };
        delete next[deleteTarget.id];
        return next;
      });
      setTemplateColorMaps((current) => {
        const next = { ...current };
        delete next[deleteTarget.id];
        return next;
      });
      setSelectedIntensityByTemplate((current) => {
        const next = { ...current };
        delete next[deleteTarget.id];
        return next;
      });
      if (activeTemplateId === deleteTarget.id) {
        const remaining = templates.filter(
          (template) => template.id !== deleteTarget.id,
        );
        setActiveTemplateId(remaining[0]?.id ?? null);
      }
      setShowEditTemplate(false);
      setDeleteTarget(null);
    } catch (err) {
      console.error(
        "[PixelJournal] delete template failed:",
        err?.message,
        err,
      );
      setDeleteError(
        err?.message ||
          "Could not delete this template right now. Please try again.",
      );
    } finally {
      setDeleteBusy(false);
    }
  }

  return (
    <div className="max-w-7xl mx-auto px-3 sm:px-4 py-6">
      <div className="flex items-center gap-3 mb-5">
        <button
          onClick={() => navigate("/")}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <ArrowLeft size={14} />
        </button>
        <Grid3X3 size={20} className="text-retro-xp" />
        <h1 className="font-pixel text-sm text-retro-text">
          Year in Pixels - {year}
        </h1>
        <button
          onClick={() => navigate("/scrapbook")}
          className="ml-auto pixel-border-sm px-3 py-1.5 bg-retro-card hover:bg-retro-input font-pixel text-[7px] flex items-center gap-1"
        >
          <BookHeart size={10} /> Diary
        </button>
      </div>

      <div className="flex flex-wrap gap-2 mb-4 items-center">
        {templates.map((template) => (
          <button
            key={template.id}
            onClick={() => setActiveTemplateId(template.id)}
            className={`pixel-border-sm px-3 py-1.5 font-pixel text-[7px] ${
              activeTemplateId === template.id
                ? "bg-retro-accent text-white"
                : "bg-retro-card text-retro-muted"
            }`}
          >
            {template.name}
          </button>
        ))}

        <button
          onClick={() => setShowAllTemplates((showing) => !showing)}
          className="pixel-border-sm px-3 py-1.5 font-pixel text-[7px] bg-retro-surface text-retro-muted hover:bg-retro-input flex items-center gap-1"
        >
          <Columns3 size={10} />{" "}
          {showAllTemplates ? "Single View" : "Compare All"}
        </button>

        <button
          onClick={() => setShowNewTemplate((showing) => !showing)}
          className="pixel-border-sm px-3 py-1.5 font-pixel text-[7px] bg-retro-surface text-retro-muted hover:bg-retro-input"
        >
          <Plus size={10} className="inline" /> Template
        </button>

        {activeTemplate && (
          <>
            <button
              onClick={() => setShowEditTemplate((showing) => !showing)}
              className="pixel-border-sm px-3 py-1.5 font-pixel text-[7px] bg-retro-surface text-retro-muted hover:bg-retro-input"
            >
              <Pencil size={10} className="inline" /> Edit
            </button>
            <button
              onClick={() => setDeleteTarget(activeTemplate)}
              className="pixel-border-sm px-3 py-1.5 font-pixel text-[7px] bg-retro-danger/10 text-retro-danger hover:bg-retro-danger/20"
            >
              <Trash2 size={10} className="inline" /> Delete
            </button>
          </>
        )}
      </div>

      {showNewTemplate && (
        <form
          onSubmit={createTemplate}
          className="pixel-border bg-retro-surface p-4 mb-4 space-y-3 animate-pixel-fade-in"
        >
          <input
            className="pixel-input w-full"
            placeholder="Template name"
            value={newTemplateName}
            onChange={(e) => setNewTemplateName(e.target.value)}
            required
          />
          <div className="grid grid-cols-2 sm:grid-cols-5 gap-2">
            {[1, 2, 3, 4, 5].map((level) => (
              <label
                key={level}
                className="font-pixel text-[7px] text-retro-muted flex flex-col gap-1"
              >
                Level {level}
                <input
                  type="color"
                  value={newTemplateColors[String(level)] || "#cccccc"}
                  onChange={(e) =>
                    setNewTemplateColors((prev) => ({
                      ...prev,
                      [String(level)]: e.target.value,
                    }))
                  }
                  className="h-9 w-full cursor-pointer"
                />
              </label>
            ))}
          </div>
          <button type="submit" className="pixel-btn w-full">
            Create Template
          </button>
        </form>
      )}

      {showEditTemplate && activeTemplate && (
        <form
          onSubmit={updateTemplate}
          className="pixel-border bg-retro-surface p-4 mb-4 space-y-3 animate-pixel-fade-in"
        >
          <input
            className="pixel-input w-full"
            placeholder="Template name"
            value={editTemplateName}
            onChange={(e) => setEditTemplateName(e.target.value)}
            required
          />
          <div className="grid grid-cols-2 sm:grid-cols-5 gap-2">
            {[1, 2, 3, 4, 5].map((level) => (
              <label
                key={level}
                className="font-pixel text-[7px] text-retro-muted flex flex-col gap-1"
              >
                Level {level}
                <input
                  type="color"
                  value={editTemplateColors[String(level)] || "#cccccc"}
                  onChange={(e) =>
                    setEditTemplateColors((prev) => ({
                      ...prev,
                      [String(level)]: e.target.value,
                    }))
                  }
                  className="h-9 w-full cursor-pointer"
                />
              </label>
            ))}
          </div>
          <button type="submit" className="pixel-btn w-full">
            Save Template
          </button>
        </form>
      )}

      {templates.length === 0 ? (
        <div className="pixel-border bg-retro-surface p-8 text-center">
          <p className="font-pixel text-[9px] text-retro-muted">
            Create a template to start tracking.
          </p>
        </div>
      ) : showAllTemplates ? (
        <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
          {templates.map((template) => (
            <TemplateGrid 
              key={template.id} 
              template={template}
              templateColorMaps={templateColorMaps}
              templatePixels={templatePixels}
              selectedIntensityByTemplate={selectedIntensityByTemplate}
              activeDayIdxByTemplate={activeDayIdxByTemplate}
              showAllTemplates={showAllTemplates}
              setActiveTemplateId={setActiveTemplateId}
              handleBlockClick={handleBlockClick}
              handleColorClick={handleColorClick}
              monthLabels={monthLabels}
              cols={cols}
              jan1Day={jan1Day}
              totalDays={totalDays}
              year={year}
              todayIdx={todayIdx}
              fallbackColor={fallbackColor}
            />
          ))}
        </div>
      ) : (
        activeTemplate && <TemplateGrid 
          template={activeTemplate}
          templateColorMaps={templateColorMaps}
          templatePixels={templatePixels}
          selectedIntensityByTemplate={selectedIntensityByTemplate}
          activeDayIdxByTemplate={activeDayIdxByTemplate}
          showAllTemplates={showAllTemplates}
          setActiveTemplateId={setActiveTemplateId}
          handleBlockClick={handleBlockClick}
          handleColorClick={handleColorClick}
          monthLabels={monthLabels}
          cols={cols}
          jan1Day={jan1Day}
          totalDays={totalDays}
          year={year}
          todayIdx={todayIdx}
          fallbackColor={fallbackColor}
        />
      )}

      <DeleteConfirmModal
        open={Boolean(deleteTarget)}
        templateName={deleteTarget?.name}
        errorMessage={deleteError}
        busy={deleteBusy}
        onCancel={() => {
          setDeleteTarget(null);
          setDeleteError("");
        }}
        onConfirm={confirmDeleteTemplate}
      />
    </div>
  );
}

function TemplateGrid({
  template,
  templateColorMaps,
  templatePixels,
  selectedIntensityByTemplate,
  activeDayIdxByTemplate,
  showAllTemplates,
  setActiveTemplateId,
  handleBlockClick,
  handleColorClick,
  monthLabels,
  cols,
  jan1Day,
  totalDays,
  year,
  todayIdx,
  fallbackColor,
}) {
  const colorMap = templateColorMaps[template.id] || { ...DEFAULT_COLORS };
  const intensityKeys = Object.keys(colorMap)
    .map(Number)
    .sort((a, b) => a - b);
  const pixels = templatePixels[template.id] || {};
  const selectedIntensity = selectedIntensityByTemplate[template.id];
  const activeDayIdx = activeDayIdxByTemplate[template.id];

  return (
    <div className="pixel-border bg-retro-surface p-3 sm:p-4">
      <div className="flex items-center justify-between gap-2 mb-3">
        <h2 className="font-pixel text-[8px] sm:text-[9px] text-retro-text">
          {template.name}
        </h2>
        {showAllTemplates && (
          <button
            onClick={() => setActiveTemplateId(template.id)}
            className="pixel-border-sm px-2 py-1 bg-retro-card hover:bg-retro-input font-pixel text-[7px]"
          >
            Edit
          </button>
        )}
      </div>

      <div className="flex items-center gap-2 mb-3 flex-wrap">
        <span className="font-pixel text-[7px] text-retro-muted flex items-center gap-1">
          <Palette size={10} /> Paint Level
        </span>
        {intensityKeys.map((key) => (
          <button
            key={key}
            onClick={() => handleColorClick(template.id, key)}
            className={`pixel-border-sm px-2 py-1 font-pixel text-[7px] ${
              selectedIntensity === key ? "ring-2 ring-retro-accent2" : ""
            }`}
            style={{ backgroundColor: colorMap[String(key)], color: "white" }}
          >
            {key}
          </button>
        ))}
      </div>

      <div className="overflow-x-auto pb-2">
        <div className="inline-block min-w-full w-full">
          <div className="relative mb-1 h-3" style={{ paddingLeft: "28px" }}>
            {monthLabels.map((month) => (
              <span
                key={`${template.id}-${month.col}`}
                className="font-pixel text-[6px] text-retro-muted absolute top-0 transform -translate-x-1/2"
                style={{ left: `calc(28px + ${month.col} * ((100% - 28px) / ${cols}) + ((100% - 28px) / ${cols}) / 2)` }}
              >
                {month.label}
              </span>
            ))}
          </div>

          <div className="flex mt-2 w-full">
            <div className="flex flex-col gap-[2px] mr-1 w-6 shrink-0">
              {WEEKDAYS.map((day, i) => (
                <div
                  key={`${template.id}-${day}`}
                  className="flex items-center justify-end flex-1"
                >
                  {i % 2 === 1 && (
                    <span className="font-pixel text-[5px] text-retro-muted leading-none">
                      {day}
                    </span>
                  )}
                </div>
              ))}
            </div>

            <div className="flex gap-[2px] flex-1 min-w-0">
              {Array.from({ length: cols }).map((_, col) => (
                <div
                  key={`${template.id}-${col}`}
                  className="flex flex-col gap-[2px] flex-1 min-w-0"
                >
                  {Array.from({ length: 7 }).map((_, row) => {
                    const dayIdx = col * 7 + row - jan1Day;
                    if (dayIdx < 0 || dayIdx >= totalDays) {
                      return (
                        <div
                          key={row}
                          className="w-full aspect-square"
                        />
                      );
                    }

                    const pixelData = pixels[dayIdx] ?? null;
                    const intensity = pixelData?.intensity ?? 0;
                    const color =
                      pixelData?.colorHex ||
                      (intensity > 0
                        ? colorMap[String(intensity)] || fallbackColor
                        : fallbackColor);
                    const date = dateFromDayOfYear(year, dayIdx + 1);
                    const isToday = dayIdx === todayIdx;
                    const tooltip =
                      intensity > 0
                        ? `${formatLocalDate(date)} - Level ${intensity}`
                        : formatLocalDate(date);

                    return (
                      <button
                        key={row}
                        onClick={() => handleBlockClick(template.id, dayIdx)}
                        className={`w-full aspect-square transition-transform hover:scale-125 ${
                          isToday ? "ring-1 ring-retro-accent" : ""
                        } ${
                          activeDayIdx === dayIdx
                            ? "ring-2 ring-retro-accent2 scale-110 z-10 relative shadow-md"
                            : ""
                        }`}
                        style={{
                          backgroundColor: color,
                          border: "1px solid var(--color-retro-border)",
                        }}
                        title={tooltip}
                      />
                    );
                  })}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
