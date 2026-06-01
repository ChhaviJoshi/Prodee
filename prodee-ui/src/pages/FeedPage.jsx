import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Newspaper, ArrowLeft, ExternalLink, RefreshCw } from "lucide-react";
import { apiGet } from "../utils/api";

export default function FeedPage() {
  const navigate = useNavigate();
  const [articles, setArticles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadFeed();
  }, []);

  async function loadFeed() {
    setLoading(true);
    try {
      const data = await apiGet("/api/articles/my-feed");
      setArticles(Array.isArray(data) ? data : []);
    } catch {
      // Fallback demo
      setArticles([
        {
          id: 1,
          title: "10 Tips for Productive Coding",
          url: "https://dev.to",
          tags: "productivity",
          author: "DevGuru",
        },
        {
          id: 2,
          title: "Building Habits as a Developer",
          url: "https://dev.to",
          tags: "habits,coding",
          author: "CodeMonk",
        },
        {
          id: 3,
          title: "React Best Practices 2026",
          url: "https://dev.to",
          tags: "react,javascript",
          author: "ReactFan",
        },
        {
          id: 4,
          title: "How to Master DSA",
          url: "https://dev.to",
          tags: "dsa,algorithms",
          author: "AlgoNerd",
        },
        {
          id: 5,
          title: "Sleep and Productivity",
          url: "https://dev.to",
          tags: "health,productivity",
          author: "WellDev",
        },
      ]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-6">
      {/* Header */}
      <div className="flex items-center gap-3 mb-5">
        <button
          onClick={() => navigate("/")}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <ArrowLeft size={14} />
        </button>
        <Newspaper size={20} className="text-retro-accent2" />
        <h1 className="font-pixel text-sm text-retro-text">Smart Feed</h1>
        <div className="flex-1" />
        <button
          onClick={loadFeed}
          className="pixel-border-sm p-1.5 bg-retro-card hover:bg-retro-input"
        >
          <RefreshCw size={12} />
        </button>
      </div>

      <p className="font-pixel text-[8px] text-retro-muted mb-4">
        Articles curated from your active task & habit tags
      </p>

      {loading ? (
        <p className="font-pixel text-[9px] text-retro-muted text-center py-12">
          Loading articles...
        </p>
      ) : articles.length === 0 ? (
        <div className="pixel-border bg-retro-card p-8 text-center">
          <p className="font-pixel text-[9px] text-retro-muted">
            No articles yet — add tasks/habits with tags!
          </p>
        </div>
      ) : (
        <div className="space-y-3 stagger-children">
          {articles.map((a) => (
            <a
              key={a.id}
              href={a.url}
              target="_blank"
              rel="noopener noreferrer"
              className="pixel-border bg-retro-card p-4 flex items-start gap-3 pixel-card-hover block"
            >
              <div className="w-8 h-8 bg-retro-accent2 flex items-center justify-center text-white pixel-border-sm flex-shrink-0">
                <Newspaper size={14} />
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="font-pixel text-[9px] text-retro-text leading-relaxed mb-1">
                  {a.title}
                </h3>
                {a.author && (
                  <p className="font-pixel text-[7px] text-retro-muted mb-1">
                    by {a.author}
                  </p>
                )}
                {a.tags && (
                  <div className="flex flex-wrap gap-1">
                    {a.tags.split(",").map((tag) => (
                      <span
                        key={tag}
                        className="inline-block px-2 py-0.5 bg-retro-accent2/15 text-retro-accent2 font-pixel text-[6px] border border-retro-accent2/30"
                      >
                        #{tag.trim()}
                      </span>
                    ))}
                  </div>
                )}
              </div>
              <ExternalLink
                size={12}
                className="text-retro-muted flex-shrink-0 mt-1"
              />
            </a>
          ))}
        </div>
      )}
    </div>
  );
}
