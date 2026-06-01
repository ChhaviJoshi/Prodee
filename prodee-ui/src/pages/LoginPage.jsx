import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Swords, UserPlus, LogIn } from "lucide-react";

export default function LoginPage() {
  const [isRegister, setIsRegister] = useState(false);
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      if (isRegister) {
        await register(username, email, password);
      } else {
        await login(username, password);
      }
      navigate("/");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="w-full max-w-sm pixel-border bg-retro-surface p-6 animate-pixel-fade-in">
        {/* Header */}
        <div className="text-center mb-6">
          <Swords size={32} className="text-retro-accent mx-auto mb-3" />
          <h1 className="font-pixel text-sm text-retro-accent tracking-wider">
            PRODEE
          </h1>
          <p className="font-pixel text-[7px] text-retro-muted mt-2">
            The Gamified Life-OS
          </p>
        </div>

        {/* Tabs */}
        <div className="flex mb-5 gap-0">
          <button
            onClick={() => setIsRegister(false)}
            className={`flex-1 font-pixel text-[9px] py-2 border-2 border-retro-border transition-colors ${
              !isRegister
                ? "bg-retro-accent text-white"
                : "bg-retro-card text-retro-muted hover:bg-retro-input"
            }`}
          >
            <LogIn size={10} className="inline mr-1" /> Sign In
          </button>
          <button
            onClick={() => setIsRegister(true)}
            className={`flex-1 font-pixel text-[9px] py-2 border-2 border-l-0 border-retro-border transition-colors ${
              isRegister
                ? "bg-retro-accent text-white"
                : "bg-retro-card text-retro-muted hover:bg-retro-input"
            }`}
          >
            <UserPlus size={10} className="inline mr-1" /> Register
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-3">
          <div>
            <label className="font-pixel text-[8px] text-retro-muted block mb-1">
              Username
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="pixel-input w-full"
              placeholder="hero_name"
              required
            />
          </div>

          {isRegister && (
            <div>
              <label className="font-pixel text-[8px] text-retro-muted block mb-1">
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="pixel-input w-full"
                placeholder="hero@realm.com"
                required
              />
            </div>
          )}

          <div>
            <label className="font-pixel text-[8px] text-retro-muted block mb-1">
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="pixel-input w-full"
              placeholder="••••••••"
              required
              minLength={6}
            />
          </div>

          {error && (
            <div className="pixel-border-sm bg-retro-danger/10 text-retro-danger p-2 font-pixel text-[8px]">
              ✖ {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="pixel-btn w-full mt-2 disabled:opacity-50"
          >
            {loading
              ? "Loading..."
              : isRegister
                ? "⚔ Create Account"
                : "⚔ Enter Realm"}
          </button>
        </form>

        {/* Footer */}
        <p className="text-center font-pixel text-[7px] text-retro-muted mt-4">
          Press Start to begin your journey
        </p>
      </div>
    </div>
  );
}
