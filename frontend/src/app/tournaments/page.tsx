"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import api from "@/lib/api";
import { useAuthStore } from "@/store/useAuthStore";

interface Tournament {
  id: number;
  title: string;
  startTime: string;
  status: string;
  divisionAllowed: string | null;
  totalRounds: number;
  currentRound: number;
  timeControl: string;
  participantCount: number;
}

const divBadge: Record<string, string> = { DIV_1: "badge-div1", DIV_2: "badge-div2", DIV_3: "badge-div3" };
const statusBadge: Record<string, string> = { UPCOMING: "badge-div3", ACTIVE: "badge-success", COMPLETED: "badge-warning" };

export default function TournamentsPage() {
  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const [loading, setLoading] = useState(true);
  const user = useAuthStore((s) => s.user);

  useEffect(() => {
    api.get("/tournaments").then((r) => setTournaments(r.data)).finally(() => setLoading(false));
  }, []);

  const handleRegister = async (id: number) => {
    try {
      await api.post(`/tournaments/${id}/register`);
      alert("Registered successfully!");
      const r = await api.get("/tournaments");
      setTournaments(r.data);
    } catch (e: any) {
      alert(e.response?.data?.message || "Registration failed");
    }
  };

  const timeUntil = (iso: string) => {
    const diff = new Date(iso).getTime() - Date.now();
    if (diff <= 0) return "Started";
    const h = Math.floor(diff / 3600000);
    const m = Math.floor((diff % 3600000) / 60000);
    return h > 0 ? `${h}h ${m}m` : `${m}m`;
  };

  return (
    <div className="max-w-5xl mx-auto px-6 py-12">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold">Tournaments</h1>
          <p className="text-sm opacity-60 mt-1">Compete in divisional contests and climb the ladder</p>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-20 opacity-40">Loading tournaments...</div>
      ) : tournaments.length === 0 ? (
        <div className="card p-12 text-center opacity-40">No tournaments scheduled yet</div>
      ) : (
        <div className="grid gap-4">
          {tournaments.map((t) => (
            <div key={t.id} className="card p-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="flex-1">
                <div className="flex flex-wrap items-center gap-2 mb-2">
                  <span className={`badge ${statusBadge[t.status] || ""}`}>{t.status}</span>
                  {t.divisionAllowed && (
                    <span className={`badge ${divBadge[t.divisionAllowed] || ""}`}>
                      {t.divisionAllowed.replace("_", " ")} Only
                    </span>
                  )}
                  <span className="text-xs opacity-40">⏱ {t.timeControl}</span>
                </div>
                <Link href={`/tournaments/${t.id}`} className="text-lg font-semibold hover:text-[var(--color-accent)] transition-colors">
                  {t.title}
                </Link>
                <div className="flex gap-6 mt-2 text-xs opacity-50">
                  <span>📅 {timeUntil(t.startTime)}</span>
                  <span>🔄 {t.totalRounds} rounds</span>
                  <span>👥 {t.participantCount} players</span>
                </div>
              </div>

              {t.status === "UPCOMING" && user && (
                <button onClick={() => handleRegister(t.id)} className="btn-primary text-sm !py-2 !px-5">
                  Register
                </button>
              )}
              {t.status === "ACTIVE" && (
                <Link href={`/tournaments/${t.id}`} className="btn-secondary text-sm !py-2 !px-5">
                  Watch Live
                </Link>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
