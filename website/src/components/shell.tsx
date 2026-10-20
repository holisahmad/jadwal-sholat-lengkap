import { Link, useRouterState } from "@tanstack/react-router";
import { BookOpen, Calculator, Clock, Home, Moon, Settings, Bell, MapPin } from "lucide-react";
import { useState } from "react";
import { cn } from "@/lib/utils";
import { cityById, useNur } from "@/lib/store";
import { SettingsModal } from "./settings-modal";
import { NotificationSync } from "./notification-sync";

const TABS = [
  { to: "/", label: "Beranda", icon: Home },
  { to: "/quran", label: "Quran", icon: BookOpen },
  { to: "/sholat", label: "Sholat", icon: Clock },
  { to: "/doa", label: "Doa", icon: Moon },
  { to: "/zakat", label: "Zakat", icon: Calculator },
] as const;

export function Shell({ children }: { children: React.ReactNode }) {
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  const [settingsOpen, setSettingsOpen] = useState(false);
  const cityId = useNur((s) => s.cityId);
  const notif = useNur((s) => s.notif);
  const city = cityById(cityId);

  return (
    <div className="mx-auto flex min-h-dvh w-full max-w-lg flex-col bg-bg">
      <NotificationSync />
      <SettingsModal open={settingsOpen} onClose={() => setSettingsOpen(false)} />

      {/* Top Header Bar */}
      <header className="sticky top-0 z-20 flex items-center justify-between border-b border-border/80 bg-surface/90 px-4 py-3 backdrop-blur-md">
        <Link to="/" className="flex items-center gap-2 font-medium text-sm text-fg tracking-tight">
          <span className="flex size-7 items-center justify-center rounded-lg bg-primary/20 text-primary">
            🌙
          </span>
          <span className="font-semibold text-fg">Jadwal Sholat</span>
        </Link>

        <div className="flex items-center gap-2">
          {/* City Quick Chip */}
          <button
            type="button"
            onClick={() => setSettingsOpen(true)}
            className="inline-flex items-center gap-1.5 rounded-full border border-border bg-raised px-2.5 py-1 text-xs text-muted hover:border-primary/50 hover:text-fg transition-colors"
          >
            <MapPin className="size-3 text-primary" />
            <span className="max-w-[100px] truncate">{city.name}</span>
          </button>

          {/* Settings Button */}
          <button
            type="button"
            onClick={() => setSettingsOpen(true)}
            className="relative flex size-8 items-center justify-center rounded-lg border border-border bg-raised text-muted hover:border-primary/50 hover:text-fg transition-colors"
            aria-label="Pengaturan"
          >
            <Settings className="size-4" />
            {notif.enabled && (
              <span className="absolute top-1.5 right-1.5 size-1.5 rounded-full bg-primary" />
            )}
          </button>
        </div>
      </header>

      <main className="flex-1 px-4 pb-28 pt-4">
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
