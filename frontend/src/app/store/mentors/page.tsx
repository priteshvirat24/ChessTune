"use client";
import { useEffect, useState } from "react";
import api from "@/lib/api";

interface Mentor { id: number; userId: number; username: string; bio: string; achievements: string; specialization: string; averageRating: number; openingAccuracy: number; tacticalVision: number; endgameConversion: number; timeManagement: number; }

function StatBar({ label, value }: { label: string; value: number | null }) {
  const v = value || 50;
  return (
    <div className="flex items-center gap-3">
      <span className="text-xs opacity-50 w-24 shrink-0">{label}</span>
      <div className="stat-bar flex-1">
        <div className="stat-bar-fill" style={{ width: `${v}%` }} />
      </div>
      <span className="text-xs font-mono w-8 text-right opacity-70">{v.toFixed(0)}</span>
    </div>
  );
}

export default function MentorsPage() {
  const [mentors, setMentors] = useState<Mentor[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get("/store/mentors").then((r) => setMentors(r.data)).finally(() => setLoading(false));
  }, []);

  return (
    <div className="max-w-6xl mx-auto px-6 py-12">
      <h1 className="text-3xl font-bold mb-2">Mentors</h1>
      <p className="text-sm opacity-60 mb-8">Learn from titled players with RPG-tracked expertise</p>

      {loading ? (
        <div className="text-center py-20 opacity-40">Loading mentors...</div>
      ) : (
        <div className="grid md:grid-cols-2 gap-6">
          {mentors.map((m) => (
            <div key={m.id} className="card p-6">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h3 className="text-lg font-bold">{m.username}</h3>
                  <span className="text-xs opacity-50">{m.specialization}</span>
                </div>
                {m.achievements && (
                  <span className="badge badge-warning text-[10px]">{m.achievements}</span>
                )}
              </div>
              {m.bio && <p className="text-sm opacity-50 mb-5 line-clamp-2">{m.bio}</p>}
              <div className="space-y-2">
                <StatBar label="Opening" value={m.openingAccuracy} />
                <StatBar label="Tactics" value={m.tacticalVision} />
                <StatBar label="Endgame" value={m.endgameConversion} />
                <StatBar label="Time Mgmt" value={m.timeManagement} />
              </div>
            </div>
          ))}
        </div>
      )}
      {!loading && mentors.length === 0 && (
        <div className="card p-12 text-center opacity-40">No mentors available yet</div>
      )}
    </div>
  );
}
