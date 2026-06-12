/** Authorized fetch helper — attaches JWT from localStorage */
export async function apiFetch(path, options = {}) {
  const token = localStorage.getItem("prodee-token");
  const isFormData = options.body instanceof FormData;
  const headers = {
    ...options.headers,
  };
  if (!isFormData && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  const baseUrl = import.meta.env.VITE_API_BASE_URL || "";
  const url = path.startsWith("http") ? path : `${baseUrl}${path}`;
  const res = await fetch(url, { ...options, headers });
  if (res.status === 401) {
    localStorage.removeItem("prodee-token");
    window.location.href = "/login";
    throw new Error("Session expired");
  }
  return res;
}

export async function apiGet(path) {
  const res = await apiFetch(path);
  if (!res.ok) throw new Error("Request failed");
  const json = await res.json();
  return json.data ?? json;
}

export async function apiPost(path, body) {
  const res = await apiFetch(path, {
    method: "POST",
    body: JSON.stringify(body),
  });
  const json = await res.json();
  if (!res.ok) throw new Error(json.message || "Request failed");
  return json.data ?? json;
}

export async function apiPut(path, body) {
  const res = await apiFetch(path, {
    method: "PUT",
    body: JSON.stringify(body),
  });
  const json = await res.json();
  if (!res.ok) throw new Error(json.message || "Request failed");
  return json.data ?? json;
}

export async function apiDelete(path) {
  const res = await apiFetch(path, { method: "DELETE" });
  if (!res.ok) throw new Error("Delete failed");
  return true;
}

export async function apiPostForm(path, formData) {
  const res = await apiFetch(path, {
    method: "POST",
    body: formData,
  });
  const json = await res.json();
  if (!res.ok) throw new Error(json.message || "Request failed");
  return json.data ?? json;
}

export async function apiPutForm(path, formData) {
  const res = await apiFetch(path, {
    method: "PUT",
    body: formData,
  });
  const json = await res.json();
  if (!res.ok) throw new Error(json.message || "Request failed");
  return json.data ?? json;
}
