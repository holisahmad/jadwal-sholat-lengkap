import { createFileRoute, Link } from "@tanstack/react-router";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useEffect, useState } from "react";
import { SURAHS } from "@/lib/quran-meta";
import { loadSurah, type SurahText } from "@/lib/quran";
import { useNur } from "@/lib/store";
import { createSeoMeta, SITE_URL } from "@/lib/seo";

export const Route = createFileRoute("/quran/$id")({
  head: ({ params }) => {
    const n = Math.min(114, Math.max(1, Number(params.id) || 1));
    const meta = SURAHS[n - 1] ?? SURAHS[0];
    return createSeoMeta({
      title: `Surah ${meta.latin} (${meta.meaning}) — Teks Arab & Terjemahan Ayat 1-${meta.ayahs}`,
      description: `Baca Surah ${meta.latin} (${meta.meaning}) surah ke-${meta.number} lengkap ${meta.ayahs} ayat. Teks Arab, transliterasi latin, dan terjemahan bahasa Indonesia resmi Kemenag. Golongan surah ${meta.revelation}.`,
      keywords: [
        `surah ${meta.latin.toLowerCase()}`,
        `surat ${meta.latin.toLowerCase()}`,
        `baca surah ${meta.latin.toLowerCase()}`,
        `arti surah ${meta.latin.toLowerCase()}`,
        `terjemahan ${meta.latin.toLowerCase()}`,
        `surah ke ${meta.number}`,
        "al quran digital",
      ],
      path: `/quran/${n}`,
      ogType: "article",
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
              name: "Al-Quran",
              item: `${SITE_URL}/quran`,
            },
            {
              "@type": "ListItem",
              position: 3,
              name: `Surah ${meta.latin}`,
              item: `${SITE_URL}/quran/${n}`,
            },
          ],
        },
        {
          "@context": "https://schema.org",
          "@type": "Chapter",
          name: `Surah ${meta.latin}`,
          alternateName: meta.meaning,
          position: meta.number,
          numberOfPages: meta.ayahs,
          inLanguage: ["ar", "id"],
          isPartOf: {
            "@type": "Book",
            name: "Al-Quran Al-Karim",
            inLanguage: "ar",
          },
        },
      ],
    });
  },
  component: Reader,
});

function Reader() {
  const { id } = Route.useParams();
  const n = Math.min(114, Math.max(1, Number(id) || 1));
  const meta = SURAHS[n - 1];
  const setLastRead = useNur((s) => s.setLastRead);
  const [surah, setSurah] = useState<SurahText | null>(null);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    let alive = true;
    setSurah(null);
    setErr(null);
    loadSurah(n)
      .then((s) => {
        if (!alive) return;
        if (!s) setErr("Surah tidak ditemukan.");
        else {
          setSurah(s);
          setLastRead(n, 1);
        }
      })
      .catch(() => {
        if (alive) setErr("Gagal memuat teks. Periksa koneksi lalu coba lagi.");
      });
    return () => {
      alive = false;
    };
  }, [n, setLastRead]);

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center gap-3">
        <Link to="/quran" className="grid size-11 place-items-center rounded-md bg-surface text-muted">
          <ChevronLeft className="size-5" />
        </Link>
        <div className="min-w-0 flex-1">
          <p className="text-xs text-muted">Surah {n}</p>
          <h1 className="font-display truncate text-2xl tracking-tight">{meta.latin}</h1>
        </div>
      </div>

      <p dir="rtl" lang="ar" className="font-arab text-right text-xl text-primary">
        {surah?.name ?? ""}
      </p>
      <p className="text-sm text-muted">
        {meta.meaning} · {meta.ayahs} ayat · {meta.revelation}
      </p>

      {n !== 9 ? (
        <p dir="rtl" lang="ar" className="font-arab rounded-lg bg-surface py-4 text-center text-2xl">
          بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ
        </p>
      ) : null}

      {err ? <p className="text-sm text-danger">{err}</p> : null}
      {!surah && !err ? <p className="text-sm text-muted">Memuat ayat…</p> : null}

      <ol className="flex flex-col gap-6">
        {surah?.ayahs.map((a) => (
          <li key={a.number} className="border-b border-border pb-5">
            <p dir="rtl" lang="ar" className="font-arab text-right text-[1.65rem] leading-[2.15]">
              {a.arab}
              <span className="mx-2 inline-flex size-6 items-center justify-center rounded-full border border-border align-middle text-[11px] tabular-nums text-muted">
                {a.number}
              </span>
            </p>
            <p className="mt-3 text-sm leading-relaxed text-fg/80">{a.id}</p>
          </li>
        ))}
      </ol>

      <div className="flex gap-3">
        {n > 1 ? (
          <Link
            to="/quran/$id"
            params={{ id: String(n - 1) }}
            className="flex h-11 flex-1 items-center justify-center gap-1 rounded-md bg-raised text-sm"
          >
            <ChevronLeft className="size-4" />
            {SURAHS[n - 2].latin}
          </Link>
        ) : (
          <span className="flex-1" />
        )}
        {n < 114 ? (
          <Link
            to="/quran/$id"
            params={{ id: String(n + 1) }}
            className="flex h-11 flex-1 items-center justify-center gap-1 rounded-md bg-raised text-sm"
          >
            {SURAHS[n].latin}
            <ChevronRight className="size-4" />
          </Link>
        ) : null}
      </div>
    </div>
  );
}
