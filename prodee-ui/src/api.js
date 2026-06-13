const API_BASE = import.meta.env.VITE_API_URL || "";

export const customFetch = (endpoint, options) => fetch(API_BASE + endpoint, options);
