"use client";
import { useEffect, useState } from "react";
import api from "@/lib/api";

interface Mentor { id: number; userId: number; username: string; bio: string; achievements: string; specialization: string; }

export default function AdminMentorsPage() {
  const [mentors, setMentors] = useState<Mentor[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ userId: "", bio: "", achievements: "", specialization: "" });

  const load = () => api.get("/admin/mentors").then((r) => setMentors(r.data));
  useEffect(() => { load(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    await api.post("/admin/mentors", { ...form, userId: +form.userId });
    setShowForm(false);
    setForm({ userId: "", bio: "", achievements: "", specialization: "" });
    load();
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Mentor Management</h1>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary text-sm">+ New Mentor</button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="card p-6 mb-6 grid md:grid-cols-2 gap-4">
          <div><label className="block text-xs opacity-60 mb-1">User ID</label>
            <input type="number" className="input-field" value={form.userId} onChange={(e) => setForm({...form, userId: e.target.value})} required /></div>
          <div><label className="block text-xs opacity-60 mb-1">Specialization</label>
            <input className="input-field" value={form.specialization} onChange={(e) => setForm({...form, specialization: e.target.value})} placeholder="e.g. Endgames" /></div>
          <div><label className="block text-xs opacity-60 mb-1">Achievements</label>
            <input className="input-field" value={form.achievements} onChange={(e) => setForm({...form, achievements: e.target.value})} placeholder="e.g. FIDE 2200" /></div>
          <div><label className="block text-xs opacity-60 mb-1">Bio</label>
            <textarea className="input-field min-h-[60px]" value={form.bio} onChange={(e) => setForm({...form, bio: e.target.value})} /></div>
          <div className="md:col-span-2 flex gap-3">
            <button type="submit" className="btn-primary text-sm">Create</button>
            <button type="button" onClick={() => setShowForm(false)} className="btn-secondary text-sm">Cancel</button>
          </div>
        </form>
      )}

      <div className="card overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-[var(--color-surface-hover)]">
            <tr className="text-left opacity-60"><th className="p-3">Username</th><th className="p-3">Specialization</th><th className="p-3">Achievements</th><th className="p-3">Bio</th></tr>
          </thead>
          <tbody>
            {mentors.map((m) => (
              <tr key={m.id} className="border-t border-[var(--color-border)] hover:bg-[var(--color-surface-hover)]">
                <td className="p-3 font-semibold">{m.username}</td>
                <td className="p-3 opacity-60">{m.specialization || "—"}</td>
                <td className="p-3"><span className="badge badge-warning text-[10px]">{m.achievements || "—"}</span></td>
                <td className="p-3 opacity-40 max-w-[200px] truncate">{m.bio || "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
