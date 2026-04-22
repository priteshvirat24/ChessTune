"use client";
import Link from "next/link";
import Image from "next/image";
import { useAuthStore } from "@/store/useAuthStore";
import { useEffect, useState } from "react";

const divBadgeClass: Record<string, string> = {
  DIV_1: "badge-div1",
  DIV_2: "badge-div2",
  DIV_3: "badge-div3",
};

export default function Navbar() {
  const { user, logout, hydrate } = useAuthStore();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    hydrate();
    setMounted(true);
  }, [hydrate]);

  if (!mounted) return null;

  return (
    <nav className="glass sticky top-0 z-50 border-b border-[var(--color-border)]">
      <div className="mx-auto max-w-7xl px-6 flex items-center justify-between h-16">
        {/* Logo */}
        <Link href="/" className="flex items-center gap-2.5 logo-hover transition-all">
          <div className="relative flex items-center justify-center p-1.5 rounded-xl bg-white/5 border border-white/10 shadow-[0_0_15px_rgba(212,160,23,0.15)] backdrop-blur-md">
            <Image
              src="/logo-transparent.png"
              alt="ChessTune"
              width={34}
              height={34}
              className="object-contain drop-shadow-[0_2px_4px_rgba(0,0,0,0.5)]"
            />
          </div>
          <span className="text-xl font-bold gradient-text tracking-tight">ChessTune</span>
        </Link>

        {/* Nav Links */}
        <div className="hidden md:flex items-center gap-6 text-sm font-medium">
          <Link href="/tournaments" className="opacity-70 hover:opacity-100 hover:text-[var(--color-accent)] transition-all">
            Tournaments
          </Link>
          <Link href="/store" className="opacity-70 hover:opacity-100 hover:text-[var(--color-accent)] transition-all">
            Store
          </Link>
          <Link href="/store/mentors" className="opacity-70 hover:opacity-100 hover:text-[var(--color-accent)] transition-all">
            Mentors
          </Link>
          {user?.role === "ADMIN" && (
            <Link href="/admin" className="opacity-70 hover:opacity-100 text-[var(--color-warning)] transition-all">
              Admin
            </Link>
          )}
        </div>

        {/* Right Side */}
        <div className="flex items-center gap-4">
          {user ? (
            <>
              {user.pendingUpsolveTasks > 0 && (
                <Link href="/upsolve" className="badge badge-danger text-xs pulse-glow">
                  {user.pendingUpsolveTasks} tasks
                </Link>
              )}
              <div className="text-sm">
                <span className="font-semibold">{user.username}</span>
              </div>
              <button onClick={logout} className="btn-secondary text-xs !py-2 !px-3">
                Logout
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="btn-secondary text-sm !py-2 !px-4">
                Login
              </Link>
              <Link href="/register" className="btn-primary text-sm !py-2 !px-4">
                Sign Up
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
