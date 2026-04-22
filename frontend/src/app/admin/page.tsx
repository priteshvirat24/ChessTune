"use client";
import { useEffect, useState } from "react";
import api from "@/lib/api";

export default function AdminDashboard() {
  const [stats, setStats] = useState({ users: 0, tournaments: 0, products: 0, orders: 0 });

  useEffect(() => {
    Promise.all([
      api.get("/admin/users").then((r) => r.data.length).catch(() => 0),
      api.get("/tournaments").then((r) => r.data.length).catch(() => 0),
      api.get("/store/products").then((r) => r.data.length).catch(() => 0),
      api.get("/admin/orders").then((r) => r.data.length).catch(() => 0),
    ]).then(([users, tournaments, products, orders]) =>
      setStats({ users, tournaments, products, orders })
    );
  }, []);

  const cards = [
    { label: "Total Users", value: stats.users, icon: "👥", color: "var(--color-accent)" },
    { label: "Tournaments", value: stats.tournaments, icon: "⚔️", color: "var(--color-div1)" },
    { label: "Products", value: stats.products, icon: "🛒", color: "var(--color-success)" },
    { label: "Orders", value: stats.orders, icon: "📦", color: "var(--color-warning)" },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Dashboard</h1>
      <div className="grid md:grid-cols-4 gap-4 mb-8">
        {cards.map((c) => (
          <div key={c.label} className="card p-5">
            <div className="flex items-center justify-between mb-3">
              <span className="text-2xl">{c.icon}</span>
              <span className="text-3xl font-bold" style={{ color: c.color }}>{c.value}</span>
            </div>
            <span className="text-xs opacity-50">{c.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
