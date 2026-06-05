import { createContext, useContext, useEffect, useState, useRef } from "react";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() =>
    localStorage.getItem("prodee-token"),
  );
  const [loading, setLoading] = useState(true);
  const fetchingRef = useRef(false);

  // Only fetch profile on initial mount (page refresh with saved token)
  useEffect(() => {
    const saved = localStorage.getItem("prodee-token");
    if (saved) {
      fetchProfile(saved);
    } else {
      setLoading(false);
    }
  }, []);

  async function fetchProfile(jwt) {
    if (fetchingRef.current) return;
    fetchingRef.current = true;
    try {
      const res = await fetch("/api/auth/profile", {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (res.ok) {
        const data = await res.json();
        setUser(data.data ?? data);
      } else {
        // Token expired or invalid — clear it
        localStorage.removeItem("prodee-token");
        setToken(null);
        setUser(null);
      }
    } catch {
      // Backend may be down — keep token, set stub user
      setUser({ username: "Player", level: 1, xp: 0, coins: 0 });
    } finally {
      setLoading(false);
      fetchingRef.current = false;
    }
  }

  async function login(username, password) {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || "Login failed");
    }
    const data = await res.json();
    const jwt = data.data?.token ?? data.token;
    localStorage.setItem("prodee-token", jwt);

    // Fetch full profile before resolving login
    setLoading(true);
    try {
      const profileRes = await fetch("/api/auth/profile", {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      if (profileRes.ok) {
        const profileData = await profileRes.json();
        setUser(profileData.data ?? profileData);
      } else {
        // Profile endpoint unreachable — use login response data
        setUser({
          id: data.data?.userId,
          username: data.data?.username ?? username,
          level: 1,
          xp: 0,
          coins: 0,
        });
      }
    } catch {
      setUser({
        id: data.data?.userId,
        username: data.data?.username ?? username,
        level: 1,
        xp: 0,
        coins: 0,
      });
    }
    setToken(jwt);
    setLoading(false);
  }

  async function register(username, email, password) {
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, email, password }),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || "Registration failed");
    }
    // Auto-login after register
    await login(username, password);
  }

  function logout() {
    localStorage.removeItem("prodee-token");
    setToken(null);
    setUser(null);
  }

  async function refreshProfile() {
    if (token) await fetchProfile(token);
  }

  return (
    <AuthContext.Provider
      value={{ user, token, loading, login, register, logout, setUser, refreshProfile }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
