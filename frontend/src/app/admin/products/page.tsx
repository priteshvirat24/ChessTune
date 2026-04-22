"use client";
import { useEffect, useState } from "react";
import api from "@/lib/api";

interface Product { id: number; title: string; description: string; price: number; productType: string; active: boolean; mentors: any[]; }

export default function AdminProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ title: "", description: "", price: 0, productType: "INDIVIDUAL_MENTOR", linkedTournamentId: "" });

  const load = () => api.get("/store/products").then((r) => setProducts(r.data));
  useEffect(() => { load(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    await api.post("/admin/products", {
      ...form,
      linkedTournamentId: form.linkedTournamentId ? +form.linkedTournamentId : null,
    });
    setShowForm(false);
    setForm({ title: "", description: "", price: 0, productType: "INDIVIDUAL_MENTOR", linkedTournamentId: "" });
    load();
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Product Management</h1>
        <button onClick={() => setShowForm(!showForm)} className="btn-primary text-sm">+ New Product</button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="card p-6 mb-6 grid md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs opacity-60 mb-1">Title</label>
            <input className="input-field" value={form.title} onChange={(e) => setForm({...form, title: e.target.value})} required />
          </div>
          <div>
            <label className="block text-xs opacity-60 mb-1">Type</label>
            <select className="input-field" value={form.productType} onChange={(e) => setForm({...form, productType: e.target.value})}>
              <option value="INDIVIDUAL_MENTOR">Individual Mentor</option>
              <option value="BUNDLE_PACKAGE">Bundle Package</option>
              <option value="TOURNAMENT_TICKET">Tournament Ticket</option>
              <option value="PHYSICAL_GOOD">Physical Good</option>
            </select>
          </div>
          <div>
            <label className="block text-xs opacity-60 mb-1">Price ($)</label>
            <input type="number" step="0.01" className="input-field" value={form.price} onChange={(e) => setForm({...form, price: +e.target.value})} required />
          </div>
          <div>
            <label className="block text-xs opacity-60 mb-1">Linked Tournament ID</label>
            <input className="input-field" value={form.linkedTournamentId} onChange={(e) => setForm({...form, linkedTournamentId: e.target.value})} placeholder="Optional" />
          </div>
          <div className="md:col-span-2">
            <label className="block text-xs opacity-60 mb-1">Description</label>
            <textarea className="input-field min-h-[80px]" value={form.description} onChange={(e) => setForm({...form, description: e.target.value})} />
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
            <tr className="text-left opacity-60"><th className="p-3">Title</th><th className="p-3">Type</th><th className="p-3 text-right">Price</th><th className="p-3 text-center">Mentors</th><th className="p-3 text-center">Active</th></tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id} className="border-t border-[var(--color-border)] hover:bg-[var(--color-surface-hover)]">
                <td className="p-3 font-semibold">{p.title}</td>
                <td className="p-3"><span className="badge badge-div2 text-[10px]">{p.productType.replace("_"," ")}</span></td>
                <td className="p-3 text-right font-mono text-[var(--color-accent)]">${p.price}</td>
                <td className="p-3 text-center opacity-50">{p.mentors?.length || 0}</td>
                <td className="p-3 text-center">{p.active ? "✅" : "❌"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
