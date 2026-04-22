"use client";
import { useEffect, useState } from "react";
import dynamic from "next/dynamic";
import api from "@/lib/api";
import { useAuthStore } from "@/store/useAuthStore";

const Chessboard = dynamic(() => import("react-chessboard").then((m) => m.Chessboard), { ssr: false });

interface UpsolveTask { id: number; gameId: number; fenState: string; correctMove: string; userMistake: string; isSolved: boolean; }

export default function UpsolvePage() {
  const [tasks, setTasks] = useState<UpsolveTask[]>([]);
  const [selected, setSelected] = useState<UpsolveTask | null>(null);
  const [attempt, setAttempt] = useState("");
  const [feedback, setFeedback] = useState<"correct" | "wrong" | null>(null);
  const user = useAuthStore((s) => s.user);

  useEffect(() => {
    if (user) {
      api.get(`/users/${user.id}/upsolve-tasks`).then((r) => setTasks(r.data));
    }
  }, [user]);

  const pending = tasks.filter((t) => !t.isSolved);
  const solved = tasks.filter((t) => t.isSolved);

  const handleSolve = () => {
    if (!selected) return;
    if (attempt.toLowerCase().trim() === selected.correctMove.toLowerCase().trim()) {
      setFeedback("correct");
      setTasks((prev) => prev.map((t) => t.id === selected.id ? { ...t, isSolved: true } : t));
      setTimeout(() => { setSelected(null); setFeedback(null); setAttempt(""); }, 1500);
    } else {
      setFeedback("wrong");
    }
  };

  return (
    <div className="max-w-5xl mx-auto px-6 py-12">
      <h1 className="text-3xl font-bold mb-2">Upsolve Queue</h1>
      <p className="text-sm opacity-60 mb-8">
        Solve your blunders before registering for the next tournament.
        {pending.length > 0 && <span className="text-[var(--color-danger)] font-bold ml-2">{pending.length} pending</span>}
      </p>

      <div className="grid lg:grid-cols-[1fr_350px] gap-8">
        <div className="space-y-3">
          {pending.length === 0 && (
            <div className="card p-8 text-center">
              <div className="text-4xl mb-4">🎉</div>
              <h3 className="text-lg font-semibold mb-2">All caught up!</h3>
              <p className="text-sm opacity-50">No pending upsolve tasks. You&apos;re ready to compete.</p>
            </div>
          )}

          {pending.map((t) => (
            <div key={t.id} onClick={() => { setSelected(t); setFeedback(null); setAttempt(""); }}
              className={`card p-4 cursor-pointer ${selected?.id === t.id ? "border-[var(--color-accent)]" : ""}`}>
              <div className="flex items-center justify-between">
                <div>
                  <span className="badge badge-danger text-[10px] mr-2">Unsolved</span>
                  <span className="text-sm font-mono opacity-60">Game #{t.gameId}</span>
                </div>
                <span className="text-xs opacity-40">Your move: <code className="text-[var(--color-danger)]">{t.userMistake}</code></span>
              </div>
            </div>
          ))}

          {solved.length > 0 && (
            <>
              <h3 className="text-sm font-semibold opacity-40 mt-8 mb-2">Completed ({solved.length})</h3>
              {solved.map((t) => (
                <div key={t.id} className="card p-4 opacity-40">
                  <span className="badge badge-success text-[10px] mr-2">Solved</span>
                  <span className="text-sm font-mono">Game #{t.gameId}</span>
                </div>
              ))}
            </>
          )}
        </div>

        {selected && (
          <div className="space-y-4">
            <div className="rounded-xl overflow-hidden shadow-2xl">
              <Chessboard
                options={{
                  position: selected.fenState,
                  allowDragging: false,
                  darkSquareStyle: { backgroundColor: "#9a7b1a" },
                  lightSquareStyle: { backgroundColor: "#1a1a1a" },
                }}
              />
            </div>

            <div className="card p-4 space-y-3">
              <p className="text-sm opacity-60">
                You played <code className="text-[var(--color-danger)] font-bold">{selected.userMistake}</code>.
                What was the correct move?
              </p>
              <input
                type="text" value={attempt}
                onChange={(e) => { setAttempt(e.target.value); setFeedback(null); }}
                className="input-field font-mono" placeholder="e.g. e2e4 or Nf3"
              />
              <button onClick={handleSolve} className="btn-primary w-full text-sm">Submit Answer</button>
              {feedback === "correct" && <div className="text-center text-[var(--color-success)] font-bold">✓ Correct!</div>}
              {feedback === "wrong" && <div className="text-center text-[var(--color-danger)] text-sm">✗ Try again</div>}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
