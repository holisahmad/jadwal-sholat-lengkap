import { createFileRoute, Link } from "@tanstack/react-router";
import { Bookmark, Search } from "lucide-react";
import { useMemo, useState } from "react";
import { SURAHS } from "@/lib/quran-meta";
import { useNur } from "@/lib/store";
import { cn } from "@/lib/utils";
import { createSeoMeta, SITE_URL } from "@/lib/seo";

export const Route = createFileRoute("/quran/")({
  head: () =>
    createSeoMeta({
      title: "Al-Quran Digital 30 Juz 114 Surah — Teks Arab & Terjemahan Indonesia",
      description:
        "Baca Al-Quran digital online 30 juz lengkap 114 surah (Al-Fatihah, Al-Baqarah, Yasin, Al-Kahfi, Al-Mulk, dll) dengan tulisan teks Arab, transliterasi latin, dan terjemahan bahasa Indonesia resmi Kemenag.",
      keywords: [
        "al quran digital",
        "baca quran online",
        "al quran 30 juz",
        "al quran terjemahan indonesia",
        "surah yasin",
        "surah al kahfi",
        "surah al waqiah",
        "surah al mulk",
        "juz amma",
        "kemenag",
      ],
      path: "/quran",
      jsonLd: [
        {
          "@context": "https://schema.org",
          "@type": "BreadcrumbList",
          itemListElement: [
            {
              "@type": "ListItem",
              position: 1,
              name: "Beranda",
              item: SITE_URL,
            },
            {
              "@type": "ListItem",
              position: 2,
              name: "Al-Quran Digital",
              item: `${SITE_URL}/quran`,
            },
          ],
        },
        {
          "@context": "https://schema.org",
          "@type": "CollectionPage",
          name: "Al-Quran Digital 30 Juz 114 Surah",
          description: "Daftar 114 Surah Al-Quran lengkap dengan teks Arab dan terjemahan Indonesia.",
          url: `${SITE_URL}/quran`,
          mainEntity: {
            "@type": "ItemList",
            numberOfItems: SURAHS.length,
            itemListElement: SURAHS.slice(0, 30).map((s, idx) => ({
              "@type": "ListItem",
              position: idx + 1,
              name: `Surah ${s.latin} (${s.meaning})`,
              url: `${SITE_URL}/quran/${s.number}`,
            })),
          },
        },
      ],
    }),
  component: QuranList,
});

function QuranList() {
  const [q, setQ] = useState("");
  const bookmarks = useNur((s) => s.bookmarks);
  const lastSurah = useNur((s) => s.lastSurah);
  const toggleBookmark = useNur((s) => s.toggleBookmark);

  const filtered = useMemo(() => {
    const s = q.trim().toLowerCase();
    if (!s) return SURAHS;
    return SURAHS.filter(
      (x) =>
        x.latin.toLowerCase().includes(s) ||
        x.meaning.toLowerCase().includes(s) ||
        String(x.number) === s,
    );
  }, [q]);

  return (
    <div className="flex flex-col gap-5">
      <header>
        <p className="text-xs uppercase tracking-[0.2em] text-muted">Mushaf</p>
        <h1 className="font-display mt-1 text-3xl tracking-tight">Al-Quran</h1>
        <p className="mt-2 text-sm text-muted">114 surah · Arab & terjemahan Indonesia</p>
      </header>

      <label className="flex h-12 items-center gap-2 rounded-lg border border-border bg-surface px-3">
        <Search className="size-4 text-muted" />
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Cari surah…"
          className="h-full w-full bg-transparent text-sm outline-none placeholder:text-faint"
        />
      </label>

      {bookmarks.length > 0 && !q ? (
        <div>
          <p className="mb-2 text-xs uppercase tracking-[0.16em] text-muted">Ditandai</p>
          <div className="flex gap-2 overflow-x-auto pb-1">
            {bookmarks.map((n) => {
              const s = SURAHS[n - 1];
              return (
                <Link
                  key={n}
                  to="/quran/$id"
                  params={{ id: String(n) }}
                  className="shrink-0 rounded-md bg-raised px-3 py-2 text-sm"
                >
                  {s.latin}
                </Link>
              );
            })}
          </div>
        </div>
      ) : null}

      <ul className="divide-y divide-border overflow-hidden rounded-xl border border-border bg-surface">
        {filtered.map((s) => {
          const marked = bookmarks.includes(s.number);
          const current = s.number === lastSurah;
          return (
            <li key={s.number} className="flex items-stretch">
              <Link
                to="/quran/$id"
                params={{ id: String(s.number) }}
                className={cn("flex min-h-16 flex-1 items-center gap-3 px-4 py-3", current && "bg-raised")}
              >
                <span className="flex size-9 items-center justify-center rounded-sm bg-bg text-xs tabular-nums text-muted">
                  {s.number}
                </span>
                <span className="min-w-0 flex-1">
                  <span className="block font-medium">{s.latin}</span>
                  <span className="block truncate text-xs text-muted">
                    {s.meaning} · {s.ayahs} ayat · {s.revelation}
                  </span>
                </span>
              </Link>
              <button
                type="button"
                aria-label={marked ? "Hapus tanda" : "Tandai"}
                onClick={() => toggleBookmark(s.number)}
                className="grid w-12 place-items-center text-muted hover:text-primary"
              >
                <Bookmark className="size-4" fill={marked ? "currentColor" : "none"} />
              </button>
            </li>
          );
        })}
      </ul>
      {filtered.length === 0 ? <p className="text-sm text-muted">Tidak ada surah yang cocok.</p> : null}
    </div>
  );
}
