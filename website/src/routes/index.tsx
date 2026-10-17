import { createFileRoute, Link } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { BookOpen, Check, ChevronRight } from "lucide-react";
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
import { cityById, streakCount, todayKey, useNur } from "@/lib/store";
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

function Home() {
  const cityId = useNur((s) => s.cityId);
  const method = useNur((s) => s.method);
  const logs = useNur((s) => s.logs);
  const lastSurah = useNur((s) => s.lastSurah);
  const lastAyah = useNur((s) => s.lastAyah);
  const togglePrayer = useNur((s) => s.togglePrayer);
  const city = cityById(cityId);
  const [now, setNow] = useState(() => new Date());

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

  return (
    <div className="flex flex-col gap-5">
      <header className="pt-2">
        <p className="text-xs uppercase tracking-[0.2em] text-muted">Nur</p>
        <h1 className="font-display mt-1 text-3xl leading-tight tracking-tight">Pendamping ibadah</h1>
        <p className="mt-2 text-sm text-muted">
          {formatGregorianId(now)}
          <span className="mx-2 text-faint">·</span>
          {formatHijri(now)}
        </p>
      </header>

      <section className="rounded-xl bg-surface px-5 py-6 shadow-[var(--shadow-soft)]">
        <p className="text-xs uppercase tracking-[0.16em] text-muted">Berikutnya · {city.name}</p>
        <p className="font-display mt-2 text-4xl tracking-tight">{PRAYER_LABEL[next.key]}</p>
        <p className="mt-1 text-lg tabular-nums text-primary">{formatTime(next.at, city.tz)}</p>
        <p className="mt-3 text-sm text-muted">
          Tersisa <span className="tabular-nums text-fg">{countdownLabel(next.at, now)}</span>
        </p>
      </section>

      <section>
        <div className="mb-3 flex items-end justify-between">
          <h2 className="text-sm font-medium">Sholat hari ini</h2>
          <p className="text-xs text-muted">
            {done}/{TRACKABLE.length}
            {streak > 0 ? ` · streak ${streak} hari` : ""}
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
                    "flex h-20 w-full flex-col items-center justify-center gap-1 rounded-lg border text-center",
                    on ? "border-primary bg-raised text-primary" : "border-border bg-surface text-muted",
                  )}
                >
                  {on ? (
                    <Check className="size-4" />
                  ) : (
                    <span className="h-4 text-xs tabular-nums">{formatTime(map[key], city.tz)}</span>
                  )}
                  <span className="text-xs">{PRAYER_LABEL[key]}</span>
                </button>
              </li>
            );
          })}
        </ul>
      </section>

      <section className="rounded-xl bg-surface p-5">
        <p className="text-xs uppercase tracking-[0.16em] text-muted">Ayat hari ini</p>
        <p dir="rtl" lang="ar" className="font-arab mt-4 text-right text-2xl leading-loose">
          {feat.arab}
        </p>
        <p className="mt-3 text-sm leading-relaxed text-muted">{feat.id}</p>
        <p className="mt-3 text-xs text-faint">
          QS. {featMeta.latin} {feat.surah}:{feat.ayah}
        </p>
      </section>

      <Link
        to="/quran/$id"
        params={{ id: String(lastSurah) }}
        className="flex items-center gap-3 rounded-xl border border-border bg-surface p-4"
      >
        <div className="flex size-11 items-center justify-center rounded-md bg-raised text-primary">
          <BookOpen className="size-5" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="text-xs text-muted">Lanjutkan baca</p>
          <p className="truncate font-medium">
            {lastMeta?.latin ?? "Al-Fatihah"} · ayat {lastAyah}
          </p>
        </div>
        <ChevronRight className="size-4 text-faint" />
      </Link>

      <div className="grid grid-cols-2 gap-3">
        <Link to="/sholat" className="rounded-lg bg-raised px-4 py-4 text-sm">
          Kiblat & jadwal
        </Link>
        <Link to="/doa" className="rounded-lg bg-raised px-4 py-4 text-sm">
          Kumpulan doa
        </Link>
      </div>

      <p className="pb-2 text-center text-xs text-faint">
        Waktu sholat dihitung di perangkat. Bukan pengganti keputusan ulama setempat.
      </p>
    </div>
  );
}
