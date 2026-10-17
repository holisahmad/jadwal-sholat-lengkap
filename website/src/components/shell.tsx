import { Link, useRouterState } from "@tanstack/react-router";
import { BookOpen, Calculator, Clock, Home, Moon } from "lucide-react";
import { cn } from "@/lib/utils";

const TABS = [
  { to: "/", label: "Beranda", icon: Home },
  { to: "/quran", label: "Quran", icon: BookOpen },
  { to: "/sholat", label: "Sholat", icon: Clock },
  { to: "/doa", label: "Doa", icon: Moon },
  { to: "/zakat", label: "Zakat", icon: Calculator },
] as const;

export function Shell({ children }: { children: React.ReactNode }) {
  const pathname = useRouterState({ select: (s) => s.location.pathname });

  return (
    <div className="mx-auto flex min-h-dvh w-full max-w-lg flex-col bg-bg">
      <main className="flex-1 px-4 pb-28 pt-[max(1rem,env(safe-area-inset-top))]">
        {children}
        <footer className="mt-12 border-t border-border pt-6 pb-2 text-center text-xs text-muted">
          <p className="font-medium text-fg">Jadwal Sholat Lengkap</p>
          <p className="mt-1 text-faint">
            Kalkulasi waktu sholat akurat Kemenag & kompas arah kiblat online.
          </p>
          <div className="mt-3 flex items-center justify-center gap-3 text-[11px] text-muted">
            <a
              href="https://numos-beige.vercel.app"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-primary transition-colors"
            >
              © 2026 NUMOS Studio
            </a>
            <span>·</span>
            <a
              href="https://numos-beige.vercel.app/policies/jadwal-sholat-lengkap.html"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-primary transition-colors"
            >
              Privacy Policy
            </a>
          </div>
        </footer>
      </main>
      <nav
        className="fixed inset-x-0 bottom-0 z-30 mx-auto max-w-lg border-t border-border bg-surface/95 backdrop-blur-md"
        style={{ paddingBottom: "max(0.5rem, env(safe-area-inset-bottom))" }}
      >
        <ul className="grid grid-cols-5 px-1 pt-1">
          {TABS.map((tab) => {
            const active =
              tab.to === "/"
                ? pathname === "/"
                : pathname === tab.to || pathname.startsWith(`${tab.to}/`);
            const Icon = tab.icon;
            return (
              <li key={tab.to}>
                <Link
                  to={tab.to}
                  className={cn(
                    "flex min-h-12 flex-col items-center justify-center gap-0.5 rounded-md text-xs tracking-wide",
                    active ? "text-primary" : "text-muted",
                  )}
                >
                  <Icon className="size-5" strokeWidth={active ? 2.2 : 1.7} />
                  {tab.label}
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>
    </div>
  );
}
