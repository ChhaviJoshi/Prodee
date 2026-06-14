import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Swords, UserPlus, LogIn, KeyRound } from "lucide-react";
import { GoogleLogin } from "@react-oauth/google";

export default function LoginPage() {
  const [mode, setMode] = useState("login"); // login | register | forgot | reset
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [msg, setMsg] = useState("");
  const [loading, setLoading] = useState(false);
  const { login, register, googleLogin, forgotPassword, resetPassword } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setMsg("");
    setLoading(true);
    try {
      if (mode === "register") {
        await register(username, email, password);
        navigate("/");
      } else if (mode === "login") {
        await login(username, password);
        navigate("/");
      } else if (mode === "forgot") {
        await forgotPassword(email);
        setMsg("Reset code sent! Check your email.");
        setMode("reset");
      } else if (mode === "reset") {
        await resetPassword(email, code, password);
        setMsg("Password reset successfully! You can now log in.");
        setMode("login");
        setPassword("");
        setCode("");
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  const handleGoogleSuccess = async (credentialResponse) => {
    setError("");
    setLoading(true);
    try {
      await googleLogin(credentialResponse.credential);
      navigate("/");
    } catch (err) {
      setError(err.message);
      setLoading(false);
    }
  };

  const handleGoogleError = () => {
    setError("Google Login Failed");
  };

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

        {/* Tabs for Login/Register */}
        {(mode === "login" || mode === "register") && (
          <div className="flex mb-5 gap-0">
            <button
              onClick={() => { setMode("login"); setError(""); setMsg(""); }}
              className={`flex-1 font-pixel text-[9px] py-2 border-2 border-retro-border transition-colors ${
                mode === "login"
                  ? "bg-retro-accent text-white"
                  : "bg-retro-card text-retro-muted hover:bg-retro-input"
              }`}
            >
              <LogIn size={10} className="inline mr-1" /> Sign In
            </button>
            <button
              onClick={() => { setMode("register"); setError(""); setMsg(""); }}
              className={`flex-1 font-pixel text-[9px] py-2 border-2 border-l-0 border-retro-border transition-colors ${
                mode === "register"
                  ? "bg-retro-accent text-white"
                  : "bg-retro-card text-retro-muted hover:bg-retro-input"
              }`}
            >
              <UserPlus size={10} className="inline mr-1" /> Register
            </button>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-3">
          {(mode === "login" || mode === "register") && (
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
          )}

          {(mode === "register" || mode === "forgot" || mode === "reset") && (
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
                disabled={mode === "reset"}
              />
            </div>
          )}

          {(mode === "login" || mode === "register") && (
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
          )}

          {mode === "reset" && (
            <>
              <div>
                <label className="font-pixel text-[8px] text-retro-muted block mb-1">
                  Reset Code
                </label>
                <input
                  type="text"
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  className="pixel-input w-full"
                  placeholder="123456"
                  required
                />
              </div>
              <div>
                <label className="font-pixel text-[8px] text-retro-muted block mb-1">
                  New Password
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
            </>
          )}

          {mode === "login" && (
            <div className="text-right">
              <button 
                type="button" 
                onClick={() => { setMode("forgot"); setError(""); setMsg(""); }}
                className="font-pixel text-[7px] text-retro-muted hover:text-retro-accent transition-colors"
              >
                Forgot Password?
              </button>
            </div>
          )}

          {error && (
            <div className="pixel-border-sm bg-retro-danger/10 text-retro-danger p-2 font-pixel text-[8px]">
              ✖ {error}
            </div>
          )}

          {msg && (
            <div className="pixel-border-sm bg-green-500/10 text-green-500 p-2 font-pixel text-[8px]">
              ✓ {msg}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="pixel-btn w-full mt-2 disabled:opacity-50"
          >
            {loading
              ? "Loading..."
              : mode === "register"
                ? "⚔ Create Account"
                : mode === "forgot"
                  ? "Send Reset Code"
                  : mode === "reset"
                    ? "Reset Password"
                    : "⚔ Enter Realm"}
          </button>
        </form>

        {/* OAuth and Extra Links */}
        {(mode === "login" || mode === "register") && (
          <div className="mt-4 pt-4 border-t-2 border-retro-border text-center">
            <p className="font-pixel text-[8px] text-retro-muted mb-3">Or continue with</p>
            <div className="flex justify-center">
              <GoogleLogin
                onSuccess={handleGoogleSuccess}
                onError={handleGoogleError}
                useOneTap
                theme="filled_black"
                shape="rectangular"
              />
            </div>
          </div>
        )}

        {(mode === "forgot" || mode === "reset") && (
          <div className="text-center mt-4 pt-4 border-t-2 border-retro-border">
            <button
              onClick={() => { setMode("login"); setError(""); setMsg(""); }}
              className="font-pixel text-[8px] text-retro-muted hover:text-retro-accent transition-colors"
            >
              Back to Login
            </button>
          </div>
        )}

        {/* Footer */}
        <p className="text-center font-pixel text-[7px] text-retro-muted mt-6">
          Press Start to begin your journey
        </p>
      </div>
    </div>
  );
}
