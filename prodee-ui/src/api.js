const API_BASE = import.meta.env.VITE_API_URL || import.meta.env.VITE_API_BASE_URL || "";

export const customFetch = (endpoint, options) => fetch(API_BASE + endpoint, options);
