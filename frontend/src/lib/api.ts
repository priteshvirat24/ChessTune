import axios from "axios";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_URL,
  headers: { "Content-Type": "application/json" },
});

// JWT interceptor — attach token to every request
api.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("chesstune_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

// Response interceptor — handle 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && typeof window !== "undefined") {
      localStorage.removeItem("chesstune_token");
      // Don't redirect if already on auth pages
      if (!window.location.pathname.startsWith("/login") &&
          !window.location.pathname.startsWith("/register")) {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default api;
