import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import {
  Bell,
  BellRing,
  BookOpen,
  Calendar,
  Check,
  ChevronRight,
  Flame,
  RotateCcw,
  Sparkles,
} from "lucide-react";
import { formatGregorianId, formatHijri } from "@/lib/hijri";
import {
  TRACKABLE,
  PRAYER_LABEL,
  buildTimes,
  countdownLabel,
  formatTime,
  nextPrayer,
  timesMap,
} from "@/lib/prayer";
import { FEATURED_VERSES } from "@/lib/featured-verses";
import { SURAHS } from "@/lib/quran-meta";
import { dailyFeaturedIndex } from "@/lib/quran";
import {
  cityById,
  lastNDays,
  streakCount,
  todayKey,
  useNur,
  type DayLog,
} from "@/lib/store";
import {
  canNotify,
  requestPermission,
  testNotification,
} from "@/lib/notification";
import { cn } from "@/lib/utils";
import { createSeoMeta, SITE_URL } from "@/lib/seo";

export const Route = createFileRoute("/")({
  head: () =>
    createSeoMeta({
      title: "Jadwal Sholat Lengkap — Waktu Sholat Hari Ini Akurat & Arah Kiblat",
      description:
        "Jadwal sholat lengkap & waktu sholat hari ini otomatis seluruh kota di Indonesia akurat Kemenag (Subuh, Dzuhur, Ashar, Maghrib, Isya). Disertai countdown sholat berikutnya, kompas kiblat, Al-Quran digital 30 juz, dan kumpulan doa harian.",
      keywords: [
        "jadwal sholat",
        "waktu sholat hari ini",
        "jadwal sholat lengkap",
        "jadwal imsakiyah",
        "arah kiblat online",
        "kompas kiblat",
        "al quran digital",
        "doa sehari-hari",
        "kemenag",
        "numos studio",
      ],
      path: "/",
      jsonLd: {
        "@context": "https://schema.org",
        "@type": "WebSite",
        name: "Jadwal Sholat Lengkap",
        url: SITE_URL,
        inLanguage: "id-ID",
        potentialAction: {
          "@type": "SearchAction",
          target: `${SITE_URL}/quran?q={search_term_string}`,
          "query-input": "required name=search_term_string",
        },
      },
    }),
  component: Home,
});

const EXTRA_IBADAH: { key: keyof DayLog; label: string; icon: string }[] = [
  { key: "dzikirPagi", label: "Dzikir Pagi", icon: "🌅" },
  { key: "dzikirPetang", label: "Dzikir Petang", icon: "🌇" },
  { key: "dhuha", label: "Sholat Dhuha", icon: "☀️" },
  { key: "tahajjud", label: "Sholat Tahajjud", icon: "🌙" },
  { key: "rawatib", label: "Sholat Rawatib", icon: "🕌" },
  { key: "bacaQuran", label: "Tilawah Quran", icon: "📖" },
];

const TASBIH_PRESETS = [
  { label: "Subhanallah", arab: "سُبْحَانَ اللَّهِ", target: 33 },
  { label: "Alhamdulillah", arab: "الْحَمْدُ لِلَّهِ", target: 33 },
  { label: "Allahu Akbar", arab: "اللَّهُ أَكْبَرُ", target: 33 },
  { label: "Astaghfirullah", arab: "أَسْتَغْفِرُ اللَّهَ", target: 100 },
];

function dayNameShort(dateStr: string): string {
  const d = new Date(dateStr + "T00:00:00");
  const names = ["Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"];
  return names[d.getDay()];
}

function Home() {
  const cityId = useNur((s) => s.cityId);
  const method = useNur((s) => s.method);
  const logs = useNur((s) => s.logs);
  const lastSurah = useNur((s) => s.lastSurah);
  const lastAyah = useNur((s) => s.lastAyah);
  const togglePrayer = useNur((s) => s.togglePrayer);
  const toggleExtra = useNur((s) => s.toggleExtra);
  const notif = useNur((s) => s.notif);
  const setNotif = useNur((s) => s.setNotif);
  const tasbihCount = useNur((s) => s.tasbihCount);
  const tasbihTarget = useNur((s) => s.tasbihTarget);
  const setTasbihCount = useNur((s) => s.setTasbihCount);
  const setTasbihTarget = useNur((s) => s.setTasbihTarget);
  const resetTasbih = useNur((s) => s.resetTasbih);

  const city = cityById(cityId);
  const [now, setNow] = useState(() => new Date());
  const [selectedPreset, setSelectedPreset] = useState(0);

  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(t);
  }, []);

  const feat = FEATURED_VERSES[dailyFeaturedIndex() % FEATURED_VERSES.length];
  const featMeta = SURAHS[feat.surah - 1];

  const pt = useMemo(() => buildTimes(city, now, method), [city, now, method]);
  const map = timesMap(city, pt, now);
  const next = nextPrayer(city, method, now);
  const day = todayKey(now);
  const log = logs[day] ?? {};
  const done = TRACKABLE.filter((k) => log[k]).length;
  const streak = streakCount(logs, now);
  const lastMeta = SURAHS[lastSurah - 1];

  const past7 = useMemo(() => lastNDays(7, now), [now]);

  const handleTasbihTap = () => {
    if (typeof window !== "undefined" && "vibrate" in navigator) {
      navigator.vibrate?.(18);
    }
    const nextVal = tasbihCount + 1;
    setTasbihCount(nextVal);
    if (nextVal >= tasbihTarget) {
      if (typeof window !== "undefined" && "vibrate" in navigator) {
        navigator.vibrate?.([40, 40, 80]);
      }
    }
  };

  const handlePresetChange = (idx: number) => {
    setSelectedPreset(idx);
    setTasbihTarget(TASBIH_PRESETS[idx].target);
    setTasbihCount(0);
  };

  return (
    <div className="flex flex-col gap-5">
      {/* Date Header */}
      <header className="pt-1">
        <div className="flex items-center justify-between">
          <p className="text-xs uppercase tracking-[0.2em] text-muted">Jadwal Sholat & Ibadah</p>
          {streak > 0 && (
            <div className="flex items-center gap-1 rounded-full bg-amber-500/15 px-2.5 py-0.5 text-[11px] font-semibold text-amber-300">
              <Flame className="size-3.5 fill-amber-400 text-amber-400" />
              <span>{streak} Hari Streak</span>
            </div>
          )}
        </div>
        <h1 className="font-display mt-1 text-3xl leading-tight tracking-tight">Pendamping Ibadah</h1>
        <p className="mt-2 text-sm text-muted">
          {formatGregorianId(now)}
          <span className="mx-2 text-faint">·</span>
          {formatHijri(now)}
        </p>
      </header>

      {/* Quick Notification Enable Banner */}
      {!notif.enabled ? (
        <div className="flex items-center justify-between gap-3 rounded-xl border border-primary/30 bg-primary/10 p-3.5 shadow-sm">
          <div className="flex items-center gap-2.5">
            <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary/20 text-primary">
              <BellRing className="size-4.5" />
            </div>
            <div>
              <p className="text-xs font-semibold text-fg">Pengingat Waktu Sholat</p>
              <p className="text-[11px] text-muted">Dapatkan notifikasi saat masuk waktu sholat</p>
            </div>
          </div>
          <button
            type="button"
            onClick={async () => {
              const granted = await requestPermission();
              if (granted) {
                setNotif({ ...notif, enabled: true });
                testNotification();
              }
            }}
            className="shrink-0 rounded-lg bg-primary px-3 py-1.5 text-xs font-medium text-primary-fg hover:opacity-90 transition-opacity"
          >
            Aktifkan
          </button>
        </div>
      ) : (
        <div className="flex items-center justify-between rounded-lg bg-raised/40 px-3 py-2 text-xs text-muted">
          <span className="flex items-center gap-2 text-primary font-medium">
            <span className="size-2 rounded-full bg-primary animate-pulse" />
            Notifikasi waktu sholat aktif ({notif.offsetMin === 0 ? "tepat waktu" : `${notif.offsetMin}m sebelumnya`})
          </span>
          <span className="text-[11px] text-faint">⚙️ Di header</span>
        </div>
      )}

      {/* Hero Next Prayer Card */}
      <section className="rounded-xl bg-surface px-5 py-6 shadow-[var(--shadow-soft)] border border-border/50">
        <div className="flex items-center justify-between">
          <p className="text-xs uppercase tracking-[0.16em] text-muted">Berikutnya · {city.name}</p>
          <span className="rounded-full bg-primary/15 px-2.5 py-0.5 text-[11px] font-medium text-primary">
            {formatTime(next.at, city.tz)}
          </span>
        </div>
        <p className="font-display mt-2 text-4xl tracking-tight text-fg">{PRAYER_LABEL[next.key]}</p>
        <p className="mt-3 text-sm text-muted">
          Tersisa <span className="tabular-nums font-semibold text-primary">{countdownLabel(next.at, now)}</span>
        </p>
      </section>

      {/* Daily Prayer Tracking (5 Waktu) */}
      <section className="rounded-xl border border-border/60 bg-surface p-4">
        <div className="mb-3 flex items-end justify-between">
          <div>
            <h2 className="text-sm font-semibold text-fg">Sholat Wajib 5 Waktu</h2>
            <p className="text-xs text-muted">Tandai sholat yang telah Anda kerjakan</p>
          </div>
          <p className="text-xs font-medium text-primary tabular-nums">
            {done}/{TRACKABLE.length} Selesai
          </p>
        </div>
        <ul className="grid grid-cols-5 gap-2">
          {TRACKABLE.map((key) => {
            const on = Boolean(log[key]);
            return (
              <li key={key}>
                <button
                  type="button"
                  onClick={() => togglePrayer(day, key)}
                  className={cn(
                    "flex h-20 w-full flex-col items-center justify-center gap-1 rounded-lg border text-center transition-all",
                    on
                      ? "border-primary bg-primary/15 text-primary shadow-sm"
                      : "border-border bg-raised/40 text-muted hover:border-border/80 hover:text-fg",
                  )}
                >
                  {on ? (
                    <div className="flex size-5 items-center justify-center rounded-full bg-primary text-primary-fg">
                      <Check className="size-3.5 stroke-[3]" />
                    </div>
                  ) : (
                    <span className="h-5 text-xs tabular-nums text-muted">{formatTime(map[key], city.tz)}</span>
                  )}
                  <span className="text-xs font-medium">{PRAYER_LABEL[key]}</span>
                </button>
              </li>
            );
          })}
        </ul>
      </section>

      {/* 7-Day History Mini Strip */}
      <section className="rounded-xl border border-border/60 bg-surface p-4">
        <div className="mb-3 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Calendar className="size-4 text-primary" />
            <h2 className="text-sm font-semibold text-fg">Riwayat 7 Hari Terakhir</h2>
          </div>
          <span className="text-xs text-muted">Konsistensi ibadah</span>
        </div>
        <div className="grid grid-cols-7 gap-1.5">
          {past7.map((dKey) => {
            const dLog = logs[dKey] ?? {};
            const dDone = TRACKABLE.filter((p) => dLog[p]).length;
            const isToday = dKey === day;
            return (
              <div
                key={dKey}
                className={cn(
                  "flex flex-col items-center justify-center rounded-lg border py-2.5 px-1 text-center transition-colors",
                  isToday
                    ? "border-primary/60 bg-primary/10"
                    : "border-border/40 bg-raised/20",
                )}
              >
                <span className="text-[10px] text-muted font-medium">
                  {dayNameShort(dKey)}
                </span>
                <span
                  className={cn(
                    "mt-1 text-xs font-bold tabular-nums",
                    dDone === 5
                      ? "text-primary"
                      : dDone > 0
                        ? "text-fg"
                        : "text-faint",
                  )}
                >
                  {dDone}/5
                </span>
                {/* Visual bar */}
                <div className="mt-1.5 h-1 w-full max-w-[28px] overflow-hidden rounded-full bg-border">
                  <div
                    className={cn(
                      "h-full rounded-full transition-all",
                      dDone === 5 ? "bg-primary" : "bg-primary/50",
                    )}
                    style={{ width: `${(dDone / 5) * 100}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* Sunnah & Amalan Harian Tracking */}
      <section className="rounded-xl border border-border/60 bg-surface p-4">
        <div className="mb-3 flex items-end justify-between">
          <div>
            <h2 className="text-sm font-semibold text-fg">Amalan & Dzikir Harian</h2>
            <p className="text-xs text-muted">Sunnah harian untuk menambah pahala</p>
          </div>
          <span className="text-xs text-primary font-medium">
            {EXTRA_IBADAH.filter((x) => Boolean(log[x.key])).length}/{EXTRA_IBADAH.length}
          </span>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
          {EXTRA_IBADAH.map((item) => {
            const active = Boolean(log[item.key]);
            return (
              <button
                key={item.key}
                type="button"
                onClick={() => toggleExtra(day, item.key)}
                className={cn(
                  "flex items-center justify-between rounded-lg border p-3 text-left transition-all",
                  active
                    ? "border-primary/60 bg-primary/15 text-fg shadow-sm"
                    : "border-border bg-raised/30 text-muted hover:border-border/80 hover:text-fg",
                )}
              >
                <div className="flex items-center gap-2 min-w-0">
                  <span className="text-base">{item.icon}</span>
                  <span className="truncate text-xs font-medium">{item.label}</span>
                </div>
                <div
                  className={cn(
                    "flex size-4.5 shrink-0 items-center justify-center rounded-md border transition-colors",
                    active
                      ? "border-primary bg-primary text-primary-fg"
                      : "border-border bg-bg/50",
                  )}
                >
                  {active && <Check className="size-3 stroke-[3]" />}
                </div>
              </button>
            );
          })}
        </div>
      </section>

      {/* Tasbih Digital Mini Widget */}
      <section className="rounded-xl border border-border/60 bg-surface p-5">
        <div className="mb-3 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sparkles className="size-4 text-primary" />
            <h2 className="text-sm font-semibold text-fg">Tasbih Digital</h2>
          </div>
          <button
            type="button"
            onClick={resetTasbih}
            className="flex items-center gap-1 rounded-md px-2 py-1 text-xs text-muted hover:bg-raised hover:text-fg transition-colors"
          >
            <RotateCcw className="size-3" />
            <span>Reset</span>
          </button>
        </div>

        {/* Preset selector */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-1.5 mb-4">
          {TASBIH_PRESETS.map((p, idx) => (
            <button
              key={p.label}
              type="button"
              onClick={() => handlePresetChange(idx)}
              className={cn(
                "rounded-lg border px-2.5 py-1.5 text-xs text-center transition-colors truncate",
                selectedPreset === idx
                  ? "border-primary bg-primary/15 text-primary font-medium"
                  : "border-border bg-raised/30 text-muted hover:text-fg",
              )}
            >
              {p.label}
            </button>
          ))}
        </div>

        {/* Big Tap Area */}
        <div
          onClick={handleTasbihTap}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => {
            if (e.key === " " || e.key === "Enter") {
              e.preventDefault();
              handleTasbihTap();
            }
          }}
          className="group relative flex flex-col items-center justify-center rounded-2xl border border-border/80 bg-raised/50 py-8 px-4 cursor-pointer hover:border-primary/40 active:scale-[0.99] transition-all select-none"
        >
          <p dir="rtl" lang="ar" className="font-arab text-2xl text-fg/90 mb-2">
            {TASBIH_PRESETS[selectedPreset].arab}
          </p>
          <p className="font-display text-5xl font-bold tracking-tight text-primary tabular-nums">
            {tasbihCount}
          </p>
          <p className="mt-1 text-xs text-muted">
            Target: <span className="font-semibold text-fg">{tasbihTarget}</span> · Ketuk layar untuk berdzikir
          </p>

          {/* Progress bar */}
          <div className="mt-4 h-1.5 w-48 overflow-hidden rounded-full bg-border/60">
            <div
              className="h-full rounded-full bg-primary transition-all duration-150"
              style={{
                width: `${Math.min(100, (tasbihCount / Math.max(1, tasbihTarget)) * 100)}%`,
              }}
            />
          </div>
        </div>
      </section>

      {/* Featured Verse */}
      <section className="rounded-xl border border-border/60 bg-surface p-5">
        <p className="text-xs uppercase tracking-[0.16em] text-muted">Ayat Hari Ini</p>
        <p dir="rtl" lang="ar" className="font-arab mt-4 text-right text-2xl leading-loose">
          {feat.arab}
        </p>
        <p className="mt-3 text-sm leading-relaxed text-muted">{feat.id}</p>
        <p className="mt-3 text-xs text-faint">
          QS. {featMeta.latin} {feat.surah}:{feat.ayah}
        </p>
      </section>

      {/* Continue Reading */}
      <Link
        to="/quran/$id"
        params={{ id: String(lastSurah) }}
        className="flex items-center gap-3 rounded-xl border border-border bg-surface p-4 hover:border-primary/40 transition-colors"
      >
        <div className="flex size-11 items-center justify-center rounded-md bg-raised text-primary">
          <BookOpen className="size-5" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="text-xs text-muted">Lanjutkan baca</p>
          <p className="truncate font-medium text-fg">
            {lastMeta?.latin ?? "Al-Fatihah"} · ayat {lastAyah}
          </p>
        </div>
        <ChevronRight className="size-4 text-faint" />
      </Link>

      {/* Navigation shortcuts */}
      <div className="grid grid-cols-2 gap-3">
        <Link to="/sholat" className="rounded-xl border border-border/60 bg-raised px-4 py-4 text-sm font-medium text-fg hover:border-primary/40 transition-colors">
          🧭 Kiblat & Jadwal Bulanan
        </Link>
        <Link to="/doa" className="rounded-xl border border-border/60 bg-raised px-4 py-4 text-sm font-medium text-fg hover:border-primary/40 transition-colors">
          🤲 Kumpulan Doa Lengkap
        </Link>
      </div>

      <p className="pb-2 text-center text-xs text-faint">
        Waktu sholat dihitung di perangkat. Bukan pengganti keputusan ulama setempat.
      </p>
    </div>
  );
}
