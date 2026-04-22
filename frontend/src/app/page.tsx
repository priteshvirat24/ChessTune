import Link from "next/link";
import Image from "next/image";

export default function HomePage() {
  return (
    <div className="relative overflow-hidden">
      {/* Hero */}
      <section className="relative min-h-[85vh] flex items-center justify-center px-6">
        {/* Background glow */}
        <div className="absolute top-1/3 left-1/2 -translate-x-1/2 w-[500px] h-[500px] bg-[var(--color-accent)] opacity-[0.04] rounded-full blur-[150px]" />

        <div className="relative text-center max-w-4xl mx-auto">
          {/* Premium Logo Showcase (Solar Eclipse Effect) */}
          <div className="relative mb-12 flex justify-center items-center">
            {/* Core intense light right behind the logo to create silhouette contrast */}
            <div className="absolute w-[90px] h-[90px] bg-white opacity-40 blur-[15px] rounded-full" />
            
            {/* Wider gold halo pulsing softly */}
            <div className="absolute w-[160px] h-[160px] bg-[var(--color-accent)] opacity-20 blur-[30px] rounded-full pulse-glow" />
            
            <Image
              src="/logo-transparent.png"
              alt="ChessTune Logo"
              width={140}
              height={140}
              className="relative z-10 object-contain drop-shadow-[0_10px_20px_rgba(0,0,0,0.8)] drop-shadow-[0_0_2px_rgba(255,255,255,0.3)]"
              priority
            />
          </div>

          <h1 className="text-5xl md:text-7xl font-bold leading-tight mb-6">
            The Ultimate{" "}
            <span className="gradient-text">Tournament</span>{" "}
            Arena
          </h1>
          <p className="text-lg md:text-xl opacity-50 max-w-2xl mx-auto mb-10 leading-relaxed">
            Create and organize professional Swiss-system tournaments. Real-time games, live leaderboards, and seamless pairings. Built for serious tournament organizers and players.
          </p>
          <div className="flex flex-wrap items-center justify-center gap-4">
            <Link href="/tournaments" className="btn-primary text-lg !py-3 !px-8">
              View Tournaments
            </Link>
            <Link href="/register" className="btn-secondary text-lg !py-3 !px-8">
              Join the Arena
            </Link>
          </div>
        </div>
      </section>

      {/* Feature Grid */}
      <section className="max-w-7xl mx-auto px-6 pb-24">
        <div className="grid md:grid-cols-3 gap-6">
          {[
            {
              icon: "⚔️",
              title: "Swiss-System Pairings",
              desc: "Professional Dutch-system tournament pairings that automatically match players of similar scores.",
            },
            {
              icon: "📈",
              title: "Automated Rating System",
              desc: "Performance-based rating tracking for all tournament participants.",
            },
            {
              icon: "🧩",
              title: "Mandatory Upsolving",
              desc: "Solve your blunders to improve before you can register for the next tournament.",
            },
            {
              icon: "🏆",
              title: "Live Leaderboards",
              desc: "Real-time standings powered by Redis. Watch rankings shift instantly.",
            },
            {
              icon: "🎓",
              title: "Mentor Marketplace",
              desc: "Browse mentors by specialization and unlock personalized training.",
            },
            {
              icon: "⏱️",
              title: "Real-Time Engine",
              desc: "WebSocket-powered game rooms with precision clocks and instant move broadcast.",
            },
          ].map((f, i) => (
            <div key={i} className="card p-6 group">
              <div className="text-3xl mb-4 group-hover:scale-110 transition-transform">{f.icon}</div>
              <h3 className="text-lg font-semibold mb-2">{f.title}</h3>
              <p className="text-sm opacity-50 leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
