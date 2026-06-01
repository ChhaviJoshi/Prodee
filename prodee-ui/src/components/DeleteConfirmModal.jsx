import { AlertTriangle } from "lucide-react";

export default function DeleteConfirmModal({
  open,
  templateName,
  errorMessage,
  onConfirm,
  onCancel,
  busy = false,
}) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[80] bg-black/55 flex items-center justify-center px-4">
      <div className="w-full max-w-sm pixel-border bg-retro-surface p-5 animate-pixel-fade-in">
        <div className="flex items-center gap-2 mb-3">
          <AlertTriangle size={16} className="text-retro-danger" />
          <h2 className="font-pixel text-[9px] text-retro-text">
            Delete Template
          </h2>
        </div>
        <p className="font-pixel text-[8px] text-retro-muted leading-relaxed mb-4">
          Are you sure you want to delete this template?
          {templateName ? ` ${templateName}` : ""}
        </p>
        {errorMessage ? (
          <p className="font-pixel text-[7px] text-retro-danger mb-3 leading-relaxed">
            {errorMessage}
          </p>
        ) : null}
        <div className="flex gap-2">
          <button
            onClick={onCancel}
            disabled={busy}
            className="flex-1 pixel-border-sm py-2 bg-retro-card hover:bg-retro-input font-pixel text-[8px]"
          >
            No
          </button>
          <button
            onClick={onConfirm}
            disabled={busy}
            className="flex-1 pixel-border-sm py-2 bg-retro-danger text-white hover:opacity-90 font-pixel text-[8px]"
          >
            {busy ? "Deleting" : "Yes"}
          </button>
        </div>
      </div>
    </div>
  );
}
