/**
 * Retro 8-bit landscape that sits at the bottom of every page.
 * Pure CSS pixel art — mountains, trees, ground.
 */
export default function RetroBackground() {
  return (
    <div
      className="fixed bottom-0 left-0 right-0 z-0 pointer-events-none select-none overflow-hidden"
      style={{ height: "120px" }}
    >
      {/* Sky gradient handled by parent bg */}

      {/* Mountains — far */}
      <div className="absolute bottom-8 left-0 right-0 flex justify-center gap-0">
        {[...Array(14)].map((_, i) => (
          <div
            key={`mtn-${i}`}
            className="bg-retro-mountain opacity-50"
            style={{
              width: `${40 + (i % 3) * 20}px`,
              height: `${30 + (i % 4) * 12}px`,
              clipPath: "polygon(50% 0%, 0% 100%, 100% 100%)",
              marginLeft: i > 0 ? "-10px" : "0",
            }}
          />
        ))}
      </div>

      {/* Mountains — near */}
      <div className="absolute bottom-6 left-0 right-0 flex justify-center gap-0">
        {[...Array(10)].map((_, i) => (
          <div
            key={`mtn2-${i}`}
            className="bg-retro-mountain opacity-70"
            style={{
              width: `${60 + (i % 3) * 25}px`,
              height: `${24 + (i % 3) * 10}px`,
              clipPath: "polygon(50% 0%, 0% 100%, 100% 100%)",
              marginLeft: i > 0 ? "-15px" : "0",
            }}
          />
        ))}
      </div>

      {/* Trees */}
      <div className="absolute bottom-4 left-0 right-0 flex justify-around items-end px-8">
        {[...Array(18)].map((_, i) => (
          <div
            key={`tree-${i}`}
            className="flex flex-col items-center"
            style={{ marginBottom: `${(i % 3) * 2}px` }}
          >
            {/* Canopy */}
            <div
              className="bg-retro-grass"
              style={{
                width: `${8 + (i % 3) * 4}px`,
                height: `${8 + (i % 3) * 4}px`,
                clipPath: "polygon(50% 0%, 0% 100%, 100% 100%)",
              }}
            />
            <div
              className="bg-retro-grass opacity-80"
              style={{
                width: `${12 + (i % 3) * 4}px`,
                height: `${6 + (i % 2) * 3}px`,
                clipPath: "polygon(50% 0%, 0% 100%, 100% 100%)",
                marginTop: "-2px",
              }}
            />
            {/* Trunk */}
            <div
              style={{ width: "3px", height: "6px", background: "#8B4513" }}
            />
          </div>
        ))}
      </div>

      {/* Ground */}
      <div className="absolute bottom-0 left-0 right-0 h-4 bg-retro-grass" />
      <div
        className="absolute bottom-0 left-0 right-0 h-1"
        style={{ background: "#3e6b3e" }}
      />
    </div>
  );
}
