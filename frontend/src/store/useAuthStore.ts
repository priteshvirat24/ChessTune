import { create } from "zustand";
import api from "@/lib/api";

interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  division: string;
  contestRating: number;
  openingAccuracy: number;
  tacticalVision: number;
  endgameConversion: number;
  timeManagement: number;
  pendingUpsolveTasks: number;
}

interface AuthState {
  token: string | null;
  user: User | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  fetchProfile: () => Promise<void>;
  logout: () => void;
  hydrate: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token: null,
  user: null,
  loading: false,

  hydrate: () => {
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("chesstune_token");
      if (token) {
        set({ token });
        get().fetchProfile();
      }
    }
  },

  login: async (username, password) => {
    set({ loading: true });
    try {
      const res = await api.post("/auth/login", { username, password });
      const { token } = res.data;
      localStorage.setItem("chesstune_token", token);
      set({ token, loading: false });
      await get().fetchProfile();
    } catch (e: any) {
      set({ loading: false });
      throw new Error(e.response?.data?.message || "Login failed");
    }
  },

  register: async (username, email, password) => {
    set({ loading: true });
    try {
      const res = await api.post("/auth/register", { username, email, password });
      const { token } = res.data;
      localStorage.setItem("chesstune_token", token);
      set({ token, loading: false });
      await get().fetchProfile();
    } catch (e: any) {
      set({ loading: false });
      throw new Error(e.response?.data?.message || e.response?.data?.details ? JSON.stringify(e.response.data.details) : "Registration failed");
    }
  },

  fetchProfile: async () => {
    try {
      const res = await api.get("/auth/me");
      set({ user: res.data });
    } catch {
      set({ user: null, token: null });
      localStorage.removeItem("chesstune_token");
    }
  },

  logout: () => {
    localStorage.removeItem("chesstune_token");
    set({ token: null, user: null });
  },
}));
