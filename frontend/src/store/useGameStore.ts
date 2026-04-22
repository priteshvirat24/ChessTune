import { create } from "zustand";
import { Chess } from "chess.js";

interface GameState {
  roomId: string | null;
  fen: string;
  moves: string[];
  whiteTimeMs: number;
  blackTimeMs: number;
  isWhiteTurn: boolean;
  status: string;
  myColor: "WHITE" | "BLACK" | null;
  setRoom: (roomId: string, color: "WHITE" | "BLACK") => void;
  updateState: (data: any) => void;
  reset: () => void;
}

const INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

export const useGameStore = create<GameState>((set) => ({
  roomId: null,
  fen: INITIAL_FEN,
  moves: [],
  whiteTimeMs: 300000,
  blackTimeMs: 300000,
  isWhiteTurn: true,
  status: "WAITING",
  myColor: null,

  setRoom: (roomId, color) => set({ roomId, myColor: color, status: "ACTIVE" }),

  updateState: (data) =>
    set({
      fen: data.fen || INITIAL_FEN,
      moves: data.moves || [],
      whiteTimeMs: data.whiteTimeMs,
      blackTimeMs: data.blackTimeMs,
      isWhiteTurn: data.isWhiteTurn,
      status: data.status || "ACTIVE",
    }),

  reset: () =>
    set({
      roomId: null,
      fen: INITIAL_FEN,
      moves: [],
      whiteTimeMs: 300000,
      blackTimeMs: 300000,
      isWhiteTurn: true,
      status: "WAITING",
      myColor: null,
    }),
}));
