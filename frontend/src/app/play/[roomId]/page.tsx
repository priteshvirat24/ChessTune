"use client";
import { useEffect, useState, useCallback } from "react";
import { useParams } from "next/navigation";
import dynamic from "next/dynamic";
import { Chess } from "chess.js";
import { useGameStore } from "@/store/useGameStore";
import { useAuthStore } from "@/store/useAuthStore";
import { createStompClient } from "@/lib/websocket";
import type { Client } from "@stomp/stompjs";

const Chessboard = dynamic(() => import("react-chessboard").then((m) => m.Chessboard), { ssr: false });

function formatTime(ms: number) {
  const totalSec = Math.max(0, Math.floor(ms / 1000));
  const min = Math.floor(totalSec / 60);
  const sec = totalSec % 60;
  return `${min}:${sec.toString().padStart(2, "0")}`;
}

export default function GameRoomPage() {
  const params = useParams();
  const roomId = params.roomId as string;
  const { fen, whiteTimeMs, blackTimeMs, isWhiteTurn, status, myColor, updateState, setRoom } = useGameStore();
  const token = useAuthStore((s) => s.token);
  const [game, setGame] = useState(new Chess());
  const [client, setClient] = useState<Client | null>(null);
  const [result, setResult] = useState<string | null>(null);

  useEffect(() => {
    if (!token || !roomId) return;
    const stompClient = createStompClient(token);

    stompClient.onConnect = () => {
      stompClient.subscribe(`/topic/game/${roomId}`, (msg) => {
        const data = JSON.parse(msg.body);
        updateState(data);
        const g = new Chess(data.fen);
        setGame(g);
      });

      stompClient.subscribe(`/topic/game/${roomId}/end`, (msg) => {
        const data = JSON.parse(msg.body);
        setResult(data.result);
      });
    };

    stompClient.activate();
    setClient(stompClient);
    if (!myColor) setRoom(roomId, "WHITE");

    return () => { stompClient.deactivate(); };
  }, [token, roomId]);

  const onDrop = useCallback(({ sourceSquare, targetSquare }: { piece: any; sourceSquare: string; targetSquare: string | null }) => {
    if (!targetSquare) return false;
    const isMyTurn = (isWhiteTurn && myColor === "WHITE") || (!isWhiteTurn && myColor === "BLACK");
    if (!isMyTurn || status !== "ACTIVE") return false;

    const gameCopy = new Chess(fen);
    try {
      const move = gameCopy.move({ from: sourceSquare, to: targetSquare, promotion: "q" });
      if (!move) return false;

      client?.publish({
        destination: `/app/game/${roomId}/move`,
        body: JSON.stringify({ from: sourceSquare, to: targetSquare, promotion: "q", fen: gameCopy.fen() }),
      });

      setGame(gameCopy);
      return true;
    } catch {
      return false;
    }
  }, [fen, isWhiteTurn, myColor, status, client, roomId]);

  const handleResign = () => {
    client?.publish({ destination: `/app/game/${roomId}/resign`, body: "{}" });
  };

  return (
    <div className="max-w-6xl mx-auto px-6 py-8">
      <div className="grid lg:grid-cols-[1fr_320px] gap-8">
        {/* Board */}
        <div>
          {/* Opponent clock */}
          <div className={`card p-3 mb-3 flex items-center justify-between ${(!isWhiteTurn && myColor === "WHITE") || (isWhiteTurn && myColor === "BLACK") ? "border-[var(--color-accent)]" : ""}`}>
            <span className="font-semibold text-sm opacity-70">
              {myColor === "WHITE" ? "♚ Black" : "♔ White"}
            </span>
            <span className={`font-mono text-lg font-bold ${myColor === "WHITE" ? (blackTimeMs < 30000 ? "text-[var(--color-danger)]" : "") : (whiteTimeMs < 30000 ? "text-[var(--color-danger)]" : "")}`}>
              {formatTime(myColor === "WHITE" ? blackTimeMs : whiteTimeMs)}
            </span>
          </div>

          <div className="rounded-xl overflow-hidden shadow-2xl">
            <Chessboard
              options={{
                position: fen,
                onPieceDrop: onDrop,
                boardOrientation: myColor === "BLACK" ? "black" as const : "white" as const,
                boardStyle: { borderRadius: "12px" },
                darkSquareStyle: { backgroundColor: "#9a7b1a" },
                lightSquareStyle: { backgroundColor: "#1a1a1a" },
              }}
            />
          </div>

          {/* My clock */}
          <div className={`card p-3 mt-3 flex items-center justify-between ${(isWhiteTurn && myColor === "WHITE") || (!isWhiteTurn && myColor === "BLACK") ? "border-[var(--color-accent)] pulse-glow" : ""}`}>
            <span className="font-semibold text-sm">
              {myColor === "WHITE" ? "♔ White (You)" : "♚ Black (You)"}
            </span>
            <span className={`font-mono text-lg font-bold ${myColor === "WHITE" ? (whiteTimeMs < 30000 ? "text-[var(--color-danger)]" : "") : (blackTimeMs < 30000 ? "text-[var(--color-danger)]" : "")}`}>
              {formatTime(myColor === "WHITE" ? whiteTimeMs : blackTimeMs)}
            </span>
          </div>
        </div>

        {/* Sidebar */}
        <div className="space-y-4">
          {result && (
            <div className="card p-6 text-center border-[var(--color-accent)]">
              <div className="text-2xl font-bold mb-2">
                {result === "WHITE_WIN" ? "White Wins!" : result === "BLACK_WIN" ? "Black Wins!" : "Draw!"}
              </div>
              <div className="text-sm opacity-60">
                {result === "WHITE_WIN" ? "1–0" : result === "BLACK_WIN" ? "0–1" : "½–½"}
              </div>
            </div>
          )}

          <div className="card p-4">
            <h3 className="font-semibold mb-3 text-sm opacity-70">Move History</h3>
            <div className="max-h-80 overflow-y-auto space-y-1 font-mono text-xs">
              {game.history().length === 0 ? (
                <div className="opacity-30 text-center py-4">No moves yet</div>
              ) : (
                game.history().reduce<string[][]>((acc, move, i) => {
                  if (i % 2 === 0) acc.push([move]);
                  else acc[acc.length - 1].push(move);
                  return acc;
                }, []).map((pair, i) => (
                  <div key={i} className="flex gap-2 p-1 rounded hover:bg-[var(--color-surface-hover)]">
                    <span className="opacity-40 w-6">{i + 1}.</span>
                    <span className="w-16">{pair[0]}</span>
                    <span className="w-16 opacity-70">{pair[1] || ""}</span>
                  </div>
                ))
              )}
            </div>
          </div>

          {!result && status === "ACTIVE" && (
            <button onClick={handleResign} className="btn-danger w-full text-sm">
              Resign
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
