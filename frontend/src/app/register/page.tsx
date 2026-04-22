"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuthStore } from "@/store/useAuthStore";

export default function RegisterPage() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const { register, loading } = useAuthStore();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      await register(username, email, password);
      router.push("/tournaments");
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <div className="min-h-[85vh] flex items-center justify-center px-6">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold mb-2">Enter the Arena</h1>
          <p className="opacity-60">Create your ChessTune account and start competing</p>
        </div>

        <form onSubmit={handleSubmit} className="card p-8 space-y-5">
          {error && (
            <div className="bg-[rgba(255,71,87,0.1)] border border-[var(--color-danger)] rounded-lg p-3 text-sm text-[var(--color-danger)]">
              {error}
            </div>
          )}

          <div>
            <label className="block text-sm font-medium mb-2 opacity-80">Username</label>
            <input type="text" value={username} onChange={(e) => setUsername(e.target.value)}
              className="input-field" placeholder="Choose a username" required minLength={3} maxLength={30} id="register-username" />
          </div>

          <div>
            <label className="block text-sm font-medium mb-2 opacity-80">Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)}
              className="input-field" placeholder="you@email.com" required id="register-email" />
          </div>

          <div>
            <label className="block text-sm font-medium mb-2 opacity-80">Password</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)}
              className="input-field" placeholder="Min 6 characters" required minLength={6} id="register-password" />
          </div>

          <button type="submit" disabled={loading} className="btn-primary w-full" id="register-submit">
            {loading ? "Creating account..." : "Create Account"}
          </button>

          <p className="text-center text-sm opacity-60">
            Already have an account?{" "}
            <Link href="/login" className="text-[var(--color-accent)] hover:underline">Sign in</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
