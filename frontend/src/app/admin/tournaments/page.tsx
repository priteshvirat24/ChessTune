"use client";
import { useEffect, useState } from "react";
import api from "@/lib/api";

interface Tournament { id: number; title: string; startTime: string; status: string; divisionAllowed: string|null; totalRounds: number; timeControl: string; participantCount: number; currentRound: number; }

export default function AdminTournamentsPage() {
  const [tournaments, setTournaments] = useState<Tournament[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ title: "", startTime: "", divisionAllowed: "", totalRounds: 5, timeControl: "5+3" });

  const load = () => api.get("/tournaments").then((r) => setTournaments(r.data));
  useEffect(() => { load(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    await api.post("/admin/tournaments", {
      ...form,
      divisionAllowed: form.divisionAllowed || null,
      startTime: new Date(form.startTime).toISOString(),
    });
    setShowForm(false);
    setForm({ title: "", startTime: "", divisionAllowed: "", totalRounds: 5, timeControl: "5+3" });
    load();
  };

  const handlePair = async (id: number) => {
    try {
      await api.post(`/admin/tournaments/${id}/pair`);
      alert("Pairings generated!");
      load();
    } catch (e: any) { alert(e.response?.data?.message || "Failed"); }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Tournament Management</h1>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary text-sm">+ New Tournament</button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="card p-6 mb-6 grid md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs opacity-60 mb-1">Title</label>
            <input className="input-field" value={form.title} onChange={(e) => setForm({...form, title: e.target.value})} required />
          </div>
          <div>
            <label className="block text-xs opacity-60 mb-1">Start Time</label>
            <input type="datetime-local" className="input-field" value={form.startTime} onChange={(e) => setForm({...form, startTime: e.target.value})} required />
          </div>
          <div>
            <label className="block text-xs opacity-60 mb-1">Division Restriction</label>
            <select className="input-field" value={form.divisionAllowed} onChange={(e) => setForm({...form, divisionAllowed: e.target.value})}>
              <option value="">Open (All Divisions)</option>
              <option value="DIV_1">Div 1 Only</option>
              <option value="DIV_2">Div 2 Only</option>
              <option value="DIV_3">Div 3 Only</option>
            </select>
          </div>
          <div className="flex gap-4">
            <div className="flex-1">
              <label className="block text-xs opacity-60 mb-1">Rounds</label>
              <input type="number" className="input-field" min={1} max={15} value={form.totalRounds} onChange={(e) => setForm({...form, totalRounds: +e.target.value})} />
            </div>
            <div className="flex-1">
              <label className="block text-xs opacity-60 mb-1">Time Control</label>
              <input className="input-field" value={form.timeControl} onChange={(e) => setForm({...form, timeControl: e.target.value})} placeholder="5+3" />
            </div>
          </div>
          <div className="md:col-span-2 flex gap-3">
            <button type="submit" className="btn-primary text-sm">Create</button>
            <button type="button" onClick={() => setShowForm(false)} className="btn-secondary text-sm">Cancel</button>
          </div>
        </form>
      )}

      <div className="card overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-[var(--color-surface-hover)]">
            <tr className="text-left opacity-60"><th className="p-3">Title</th><th className="p-3">Status</th><th className="p-3">Division</th><th className="p-3 text-center">Players</th><th className="p-3 text-center">Round</th><th className="p-3 text-right">Actions</th></tr>
          </thead>
          <tbody>
            {tournaments.map((t) => (
              <tr key={t.id} className="border-t border-[var(--color-border)] hover:bg-[var(--color-surface-hover)]">
                <td className="p-3 font-semibold">{t.title}</td>
                <td className="p-3"><span className={`badge ${t.status === "ACTIVE" ? "badge-success" : t.status === "COMPLETED" ? "badge-warning" : "badge-div3"} text-[10px]`}>{t.status}</span></td>
                <td className="p-3 opacity-50">{t.divisionAllowed?.replace("_"," ") || "Open"}</td>
                <td className="p-3 text-center">{t.participantCount}</td>
                <td className="p-3 text-center">{t.currentRound}/{t.totalRounds}</td>
                <td className="p-3 text-right">
                  {(t.status === "UPCOMING" || t.status === "ACTIVE") && (
                    <button onClick={() => handlePair(t.id)} className="btn-secondary text-xs !py-1 !px-3">
                      {t.currentRound === 0 ? "Start" : "Next Round"}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
