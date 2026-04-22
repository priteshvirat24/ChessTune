"use client";
import Link from "next/link";
import { usePathname } from "next/navigation";

const navItems = [
  { href: "/admin", label: "Dashboard", icon: "📊" },
  { href: "/admin/tournaments", label: "Tournaments", icon: "⚔️" },
  { href: "/admin/products", label: "Products", icon: "🛒" },
  { href: "/admin/mentors", label: "Mentors", icon: "🎓" },
  { href: "/admin/orders", label: "Orders", icon: "📦" },
];

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  return (
    <div className="flex min-h-[calc(100vh-64px)]">
      {/* Sidebar */}
      <aside className="w-56 shrink-0 bg-[var(--color-surface)] border-r border-[var(--color-border)] p-4">
        <h2 className="text-xs font-bold uppercase tracking-wider opacity-40 mb-4 px-3">Admin Panel</h2>
        <nav className="space-y-1">
          {navItems.map((item) => (
            <Link key={item.href} href={item.href}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all ${
                pathname === item.href
                  ? "bg-[var(--color-accent)] text-white font-semibold"
                  : "opacity-60 hover:opacity-100 hover:bg-[var(--color-surface-hover)]"
              }`}>
              <span>{item.icon}</span>
              {item.label}
            </Link>
          ))}
        </nav>
      </aside>

      {/* Content */}
      <div className="flex-1 p-8 overflow-auto">{children}</div>
    </div>
  );
}
