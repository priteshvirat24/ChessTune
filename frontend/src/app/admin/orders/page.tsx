"use client";
import { useEffect, useState } from "react";
import api from "@/lib/api";

interface Order { id: number; userId: number; username: string; totalAmount: number; status: string; createdAt: string; items: { productTitle: string; quantity: number; price: number }[]; }

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);

  const load = () => api.get("/admin/orders").then((r) => setOrders(r.data));
  useEffect(() => { load(); }, []);

  const handleFulfill = async (id: number) => {
    await api.put(`/admin/orders/${id}/fulfill`);
    load();
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Order Management</h1>

      <div className="card overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-[var(--color-surface-hover)]">
            <tr className="text-left opacity-60"><th className="p-3">#</th><th className="p-3">User</th><th className="p-3">Items</th><th className="p-3 text-right">Total</th><th className="p-3 text-center">Status</th><th className="p-3 text-right">Actions</th></tr>
          </thead>
          <tbody>
            {orders.map((o) => (
              <tr key={o.id} className="border-t border-[var(--color-border)] hover:bg-[var(--color-surface-hover)]">
                <td className="p-3 font-mono opacity-50">{o.id}</td>
                <td className="p-3 font-semibold">{o.username}</td>
                <td className="p-3 opacity-50 text-xs">{o.items?.map((i) => i.productTitle).join(", ") || "—"}</td>
                <td className="p-3 text-right font-mono font-bold text-[var(--color-accent)]">${o.totalAmount}</td>
                <td className="p-3 text-center"><span className={`badge ${o.status === "FULFILLED" ? "badge-success" : "badge-warning"} text-[10px]`}>{o.status}</span></td>
                <td className="p-3 text-right">
                  {o.status === "PENDING" && (
                    <button onClick={() => handleFulfill(o.id)} className="btn-primary text-xs !py-1 !px-3">Fulfill</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {orders.length === 0 && <div className="p-8 text-center opacity-40">No orders yet</div>}
      </div>
    </div>
  );
}
