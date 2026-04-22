"use client";
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import api from "@/lib/api";

interface Participant { userId: number; username: string; score: number; tiebreakScore: number; rank: number; division: string; rating: number; }
interface TournamentDetail {
  id: number; title: string; startTime: string; status: string; divisionAllowed: string | null;
  totalRounds: number; currentRound: number; timeControl: string; participantCount: number;
  standings: Participant[];
}
interface Pairing { id: number; roundNumber: number; whiteUsername: string; blackUsername: string; result: string; }

const divBadge: Record<string, string> = { DIV_1: "badge-div1", DIV_2: "badge-div2", DIV_3: "badge-div3" };
const resultColor: Record<string, string> = { WHITE_WIN: "text-[var(--color-foreground)]", BLACK_WIN: "text-[var(--color-foreground)]", DRAW: "text-[var(--color-warning)]", IN_PROGRESS: "text-[var(--color-info)]" };

export default function TournamentDetailPage() {
  const params = useParams();
  const id = params.id;
  const [tournament, setTournament] = useState<TournamentDetail | null>(null);
  const [pairings, setPairings] = useState<Pairing[]>([]);
  const [activeTab, setActiveTab] = useState<"standings" | "rounds">("standings");
  const [selectedRound, setSelectedRound] = useState(1);

  useEffect(() => {
    api.get(`/tournaments/${id}`).then((r) => {
      setTournament(r.data);
      setSelectedRound(r.data.currentRound || 1);
    });
  }, [id]);

  useEffect(() => {
    if (tournament && selectedRound > 0) {
      api.get(`/tournaments/${id}/rounds/${selectedRound}`).then((r) => setPairings(r.data)).catch(() => setPairings([]));
    }
  }, [id, tournament, selectedRound]);

  if (!tournament) return <div className="text-center py-20 opacity-40">Loading...</div>;

  return (
    <div className="max-w-5xl mx-auto px-6 py-12">
      {/* Header */}
      <div className="mb-8">
        <div className="flex flex-wrap gap-2 mb-3">
          <span className={`badge ${tournament.status === "ACTIVE" ? "badge-success" : tournament.status === "COMPLETED" ? "badge-warning" : "badge-div3"}`}>
            {tournament.status}
          </span>
          {tournament.divisionAllowed && <span className={`badge ${divBadge[tournament.divisionAllowed] || ""}`}>{tournament.divisionAllowed.replace("_"," ")}</span>}
        </div>
        <h1 className="text-3xl font-bold mb-2">{tournament.title}</h1>
        <div className="flex gap-6 text-sm opacity-50">
          <span>⏱ {tournament.timeControl}</span>
          <span>🔄 Round {tournament.currentRound}/{tournament.totalRounds}</span>
          <span>👥 {tournament.participantCount} players</span>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 bg-[var(--color-surface)] rounded-lg p-1 w-fit">
        {(["standings", "rounds"] as const).map((tab) => (
          <button key={tab} onClick={() => setActiveTab(tab)}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-all ${activeTab === tab ? "bg-[var(--color-accent)] text-white" : "opacity-60 hover:opacity-100"}`}>
            {tab.charAt(0).toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {activeTab === "standings" ? (
        <div className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-[var(--color-surface-hover)]">
              <tr className="text-left opacity-60">
                <th className="p-3 w-12">#</th>
                <th className="p-3">Player</th>
                <th className="p-3 text-center">Division</th>
                <th className="p-3 text-center">Rating</th>
                <th className="p-3 text-center">Score</th>
                <th className="p-3 text-center">Tiebreak</th>
              </tr>
            </thead>
            <tbody>
              {(tournament.standings || []).map((p, i) => (
                <tr key={p.userId} className="border-t border-[var(--color-border)] hover:bg-[var(--color-surface-hover)] transition-colors">
                  <td className="p-3 font-bold">{i < 3 ? ["🥇","🥈","🥉"][i] : p.rank || i+1}</td>
                  <td className="p-3 font-semibold">{p.username}</td>
                  <td className="p-3 text-center"><span className={`badge ${divBadge[p.division]||""} text-[10px]`}>{p.division?.replace("_"," ")}</span></td>
                  <td className="p-3 text-center opacity-60">{p.rating}</td>
                  <td className="p-3 text-center font-bold text-[var(--color-accent)]">{p.score}</td>
                  <td className="p-3 text-center opacity-40">{p.tiebreakScore?.toFixed(1)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {(!tournament.standings || tournament.standings.length === 0) && (
            <div className="p-8 text-center opacity-40">No standings yet</div>
          )}
        </div>
      ) : (
        <div>
          <div className="flex gap-2 mb-4 flex-wrap">
            {Array.from({ length: tournament.totalRounds }, (_, i) => i + 1).map((r) => (
              <button key={r} onClick={() => setSelectedRound(r)}
                className={`px-3 py-1 rounded-md text-sm ${selectedRound === r ? "bg-[var(--color-accent)] text-white" : "bg-[var(--color-surface)] opacity-60 hover:opacity-100"}`}>
                Round {r}
              </button>
            ))}
          </div>
          <div className="card overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-[var(--color-surface-hover)]">
                <tr className="text-left opacity-60">
                  <th className="p-3">White</th>
                  <th className="p-3 text-center">Result</th>
                  <th className="p-3 text-right">Black</th>
                </tr>
              </thead>
              <tbody>
                {pairings.map((p) => (
                  <tr key={p.id} className="border-t border-[var(--color-border)]">
                    <td className={`p-3 font-semibold ${p.result === "WHITE_WIN" ? "text-[var(--color-success)]" : ""}`}>♔ {p.whiteUsername}</td>
                    <td className={`p-3 text-center font-bold ${resultColor[p.result] || ""}`}>
                      {p.result === "WHITE_WIN" ? "1–0" : p.result === "BLACK_WIN" ? "0–1" : p.result === "DRAW" ? "½–½" : "..."}
                    </td>
                    <td className={`p-3 text-right font-semibold ${p.result === "BLACK_WIN" ? "text-[var(--color-success)]" : ""}`}>{p.blackUsername} ♚</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {pairings.length === 0 && <div className="p-8 text-center opacity-40">No pairings for this round yet</div>}
          </div>
        </div>
      )}
    </div>
  );
}
