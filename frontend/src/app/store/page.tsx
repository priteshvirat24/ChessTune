"use client";
import { useEffect, useState } from "react";
import api from "@/lib/api";

interface Product { id: number; title: string; description: string; price: number; productType: string; mentors: any[]; }

const typeBadge: Record<string, { label: string; cls: string }> = {
  INDIVIDUAL_MENTOR: { label: "Mentor", cls: "badge-div2" },
  BUNDLE_PACKAGE: { label: "Bundle", cls: "badge-success" },
  TOURNAMENT_TICKET: { label: "Ticket", cls: "badge-warning" },
  PHYSICAL_GOOD: { label: "Physical", cls: "badge-div3" },
};

export default function StorePage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [filter, setFilter] = useState<string>("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const url = filter ? `/store/products?type=${filter}` : "/store/products";
    api.get(url).then((r) => setProducts(r.data)).finally(() => setLoading(false));
  }, [filter]);

  const handleBuy = async (productId: number) => {
    try {
      await api.post("/store/checkout", { items: [{ productId, quantity: 1 }] });
      alert("Purchase successful!");
    } catch (e: any) {
      alert(e.response?.data?.message || "Checkout failed. Please login first.");
    }
  };

  return (
    <div className="max-w-6xl mx-auto px-6 py-12">
      <h1 className="text-3xl font-bold mb-2">Store</h1>
      <p className="text-sm opacity-60 mb-8">Mentorship packages, tournament tickets, and more</p>

      {/* Filters */}
      <div className="flex flex-wrap gap-2 mb-8">
        {[{ key: "", label: "All" }, { key: "INDIVIDUAL_MENTOR", label: "Mentors" }, { key: "BUNDLE_PACKAGE", label: "Bundles" }, { key: "TOURNAMENT_TICKET", label: "Tickets" }, { key: "PHYSICAL_GOOD", label: "Physical" }]
          .map((f) => (
            <button key={f.key} onClick={() => { setFilter(f.key); setLoading(true); }}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${filter === f.key ? "bg-[var(--color-accent)] text-white" : "bg-[var(--color-surface)] border border-[var(--color-border)] opacity-70 hover:opacity-100"}`}>
              {f.label}
            </button>
          ))}
      </div>

      {loading ? (
        <div className="text-center py-20 opacity-40">Loading products...</div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {products.map((p) => (
            <div key={p.id} className="card p-6 flex flex-col">
              <div className="flex items-center justify-between mb-3">
                <span className={`badge ${typeBadge[p.productType]?.cls || ""}`}>
                  {typeBadge[p.productType]?.label || p.productType}
                </span>
                <span className="text-xl font-bold text-[var(--color-accent)]">${p.price}</span>
              </div>
              <h3 className="text-lg font-semibold mb-2">{p.title}</h3>
              <p className="text-sm opacity-50 flex-1 mb-4 line-clamp-3">{p.description}</p>
              {p.mentors?.length > 0 && (
                <div className="text-xs opacity-40 mb-3">
                  {p.mentors.length} mentor{p.mentors.length > 1 ? "s" : ""} included
                </div>
              )}
              <button onClick={() => handleBuy(p.id)} className="btn-primary w-full text-sm">
                Purchase
              </button>
            </div>
          ))}
        </div>
      )}
      {!loading && products.length === 0 && (
        <div className="card p-12 text-center opacity-40">No products available</div>
      )}
    </div>
  );
}
