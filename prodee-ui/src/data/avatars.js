/**
 * 8‑bit avatar sprites — simple inline SVG data URIs.
 * Each avatar is a small pixel-art character.
 */

const AVATARS = [
  {
    id: "knight",
    name: "Knight",
    color: "#e85d04",
    sprite: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="64" height="64">
        <rect x="5" y="1" width="6" height="3" fill="#888"/>
        <rect x="4" y="3" width="8" height="1" fill="#888"/>
        <rect x="5" y="4" width="6" height="4" fill="#fdd"/>
        <rect x="6" y="5" width="1" height="1" fill="#333"/>
        <rect x="9" y="5" width="1" height="1" fill="#333"/>
        <rect x="7" y="7" width="2" height="1" fill="#c66"/>
        <rect x="4" y="8" width="8" height="5" fill="#e85d04"/>
        <rect x="3" y="9" width="2" height="3" fill="#e85d04"/>
        <rect x="11" y="9" width="2" height="3" fill="#e85d04"/>
        <rect x="5" y="13" width="2" height="2" fill="#8B4513"/>
        <rect x="9" y="13" width="2" height="2" fill="#8B4513"/>
      </svg>
    `,
  },
  {
    id: "mage",
    name: "Mage",
    color: "#7c4dff",
    sprite: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="64" height="64">
        <rect x="6" y="0" width="4" height="2" fill="#7c4dff"/>
        <rect x="5" y="2" width="6" height="2" fill="#7c4dff"/>
        <rect x="5" y="4" width="6" height="4" fill="#fdd"/>
        <rect x="6" y="5" width="1" height="1" fill="#333"/>
        <rect x="9" y="5" width="1" height="1" fill="#333"/>
        <rect x="7" y="7" width="2" height="1" fill="#c66"/>
        <rect x="4" y="8" width="8" height="5" fill="#7c4dff"/>
        <rect x="3" y="9" width="2" height="3" fill="#9c7cff"/>
        <rect x="11" y="9" width="2" height="3" fill="#9c7cff"/>
        <rect x="5" y="13" width="2" height="2" fill="#5c3d99"/>
        <rect x="9" y="13" width="2" height="2" fill="#5c3d99"/>
        <rect x="12" y="6" width="1" height="5" fill="#8B4513"/>
        <rect x="11" y="4" width="3" height="2" fill="#ffeb3b"/>
      </svg>
    `,
  },
  {
    id: "ranger",
    name: "Ranger",
    color: "#4caf50",
    sprite: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="64" height="64">
        <rect x="5" y="2" width="6" height="2" fill="#4caf50"/>
        <rect x="4" y="3" width="8" height="1" fill="#388e3c"/>
        <rect x="5" y="4" width="6" height="4" fill="#fdd"/>
        <rect x="6" y="5" width="1" height="1" fill="#333"/>
        <rect x="9" y="5" width="1" height="1" fill="#333"/>
        <rect x="7" y="7" width="2" height="1" fill="#c66"/>
        <rect x="4" y="8" width="8" height="5" fill="#4caf50"/>
        <rect x="3" y="9" width="2" height="3" fill="#4caf50"/>
        <rect x="11" y="9" width="2" height="3" fill="#4caf50"/>
        <rect x="5" y="13" width="2" height="2" fill="#8B4513"/>
        <rect x="9" y="13" width="2" height="2" fill="#8B4513"/>
      </svg>
    `,
  },
  {
    id: "healer",
    name: "Healer",
    color: "#e91e63",
    sprite: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="64" height="64">
        <rect x="5" y="2" width="6" height="2" fill="#fff"/>
        <rect x="5" y="4" width="6" height="4" fill="#fdd"/>
        <rect x="6" y="5" width="1" height="1" fill="#333"/>
        <rect x="9" y="5" width="1" height="1" fill="#333"/>
        <rect x="7" y="7" width="2" height="1" fill="#c66"/>
        <rect x="4" y="8" width="8" height="5" fill="#fff"/>
        <rect x="7" y="9" width="2" height="1" fill="#e91e63"/>
        <rect x="6" y="10" width="4" height="1" fill="#e91e63"/>
        <rect x="7" y="11" width="2" height="1" fill="#e91e63"/>
        <rect x="3" y="9" width="2" height="3" fill="#fff"/>
        <rect x="11" y="9" width="2" height="3" fill="#fff"/>
        <rect x="5" y="13" width="2" height="2" fill="#e91e63"/>
        <rect x="9" y="13" width="2" height="2" fill="#e91e63"/>
      </svg>
    `,
  },
  {
    id: "rogue",
    name: "Rogue",
    color: "#37474f",
    sprite: `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="64" height="64">
        <rect x="5" y="2" width="6" height="2" fill="#37474f"/>
        <rect x="4" y="3" width="8" height="2" fill="#263238"/>
        <rect x="5" y="5" width="6" height="3" fill="#fdd"/>
        <rect x="6" y="5" width="1" height="1" fill="#333"/>
        <rect x="9" y="5" width="1" height="1" fill="#333"/>
        <rect x="5" y="6" width="6" height="1" fill="#37474f" opacity="0.5"/>
        <rect x="7" y="7" width="2" height="1" fill="#c66"/>
        <rect x="4" y="8" width="8" height="5" fill="#37474f"/>
        <rect x="3" y="9" width="2" height="3" fill="#455a64"/>
        <rect x="11" y="9" width="2" height="3" fill="#455a64"/>
        <rect x="5" y="13" width="2" height="2" fill="#263238"/>
        <rect x="9" y="13" width="2" height="2" fill="#263238"/>
      </svg>
    `,
  },
];

export default AVATARS;
