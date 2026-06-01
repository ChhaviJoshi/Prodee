function norm(value) {
  return String(value || "").toLowerCase();
}

export function getItemIcon(itemName, category) {
  const name = norm(itemName);
  const cat = norm(category);

  if (name.includes("sword")) return "⚔️";
  if (name.includes("shield")) return "🛡️";
  if (name.includes("crown")) return "👑";
  if (name.includes("cape")) return "🎗️";
  if (name.includes("dragon")) return "🐉";
  if (name.includes("helmet")) return "🪖";
  if (name.includes("staff")) return "🪄";
  if (name.includes("wing")) return "🪽";
  if (name.includes("amulet")) return "📿";
  if (name.includes("boot")) return "👢";
  if (name.includes("potion")) return "🧪";
  if (name.includes("token") || cat.includes("token")) return "🎟️";
  if (name.includes("theme") || cat.includes("theme")) return "🧩";

  if (cat.includes("avatar")) return "🛡️";
  if (cat.includes("potion")) return "🧪";
  if (cat.includes("theme")) return "🧩";
  if (cat.includes("token")) return "🎟️";

  return "🎒";
}

export function isNatureSticker(stickerName, imageUrl) {
  const value = `${norm(stickerName)} ${norm(imageUrl)}`;
  return (
    value.includes("nature") ||
    value.includes("pinecone") ||
    value.includes("sapling") ||
    value.includes("oak") ||
    value.includes("bonsai") ||
    value.includes("branch") ||
    value.includes("blossom")
  );
}

export function getStickerIcon(stickerName, imageUrl) {
  const value = `${norm(stickerName)} ${norm(imageUrl)}`;

  if (value.includes("pinecone")) return "🌰";
  if (value.includes("sapling")) return "🌱";
  if (value.includes("oak")) return "🌳";
  if (value.includes("bonsai")) return "🪴";
  if (value.includes("branch")) return "🌿";
  if (value.includes("blossom")) return "🌸";
  if (value.includes("heart")) return "💖";
  if (value.includes("spark")) return "✨";
  if (value.includes("star")) return "⭐";

  return "✨";
}
