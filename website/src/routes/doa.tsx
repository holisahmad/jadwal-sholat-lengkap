import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { DOAS, DOA_CATEGORIES } from "@/lib/doa-data";
import { cn } from "@/lib/utils";
import { createSeoMeta, SITE_URL } from "@/lib/seo";

export const Route = createFileRoute("/doa")({
  head: () =>
    createSeoMeta({
      title: "Kumpulan Doa Sehari-hari Lengkap — Arab, Latin & Terjemahan",
      description:
        "Kumpulan doa sehari-hari islami terlengkap: doa pagi & petang, doa sesudah sholat, doa wudhu, doa sebelum makan dan tidur, doa safar bepergian, dan doa keluarga beserta tulisan teks Arab, latin, dan artinya.",
      keywords: [
        "kumpulan doa sehari-hari",
        "doa sehari hari",
        "doa mustajab",
        "doa pagi petang",
        "doa qunut",
        "doa selamat",
        "doa sebelum tidur",
        "doa kedua orang tua",
        "doa bepergian",
      ],
      path: "/doa",
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
              name: "Kumpulan Doa Harian",
              item: `${SITE_URL}/doa`,
            },
          ],
        },
        {
          "@context": "https://schema.org",
          "@type": "ItemList",
          name: "Kumpulan Doa Sehari-hari",
          numberOfItems: DOAS.length,
          itemListElement: DOAS.map((d, idx) => ({
            "@type": "ListItem",
            position: idx + 1,
            name: d.title,
            description: `${d.category} — ${d.id.slice(0, 80)}...`,
          })),
        },
      ],
    }),
  component: DoaPage,
});

function DoaPage() {
  const [cat, setCat] = useState<string>("Semua");
  const [open, setOpen] = useState<string | null>(DOAS[0].id);

  const list = useMemo(
    () => (cat === "Semua" ? DOAS : DOAS.filter((d) => d.category === cat)),
    [cat],
  );

  return (
    <div className="flex flex-col gap-5">
      <header>
        <p className="text-xs uppercase tracking-[0.2em] text-muted">Wirid</p>
        <h1 className="font-display mt-1 text-3xl tracking-tight">Doa harian</h1>
      </header>

      <div className="flex gap-2 overflow-x-auto pb-1">
        {["Semua", ...DOA_CATEGORIES].map((c) => (
          <button
            key={c}
            type="button"
            onClick={() => setCat(c)}
            className={cn(
              "h-9 shrink-0 rounded-full px-3 text-sm",
              cat === c ? "bg-primary text-primary-fg" : "bg-raised text-muted",
            )}
          >
            {c}
          </button>
        ))}
      </div>

      <ul className="flex flex-col gap-3">
        {list.map((d) => {
          const isOpen = open === d.id;
          return (
            <li key={d.id} className="overflow-hidden rounded-xl border border-border bg-surface">
              <button
                type="button"
                onClick={() => setOpen(isOpen ? null : d.id)}
                className="flex min-h-14 w-full items-center justify-between px-4 py-3 text-left"
              >
                <span>
                  <span className="block font-medium">{d.title}</span>
                  <span className="block text-xs text-muted">{d.category}</span>
                </span>
                <span className="text-xs text-faint">{isOpen ? "Tutup" : "Buka"}</span>
              </button>
              {isOpen ? (
                <div className="border-t border-border px-4 py-4">
                  <p dir="rtl" lang="ar" className="font-arab text-right text-2xl leading-loose">
                    {d.arab}
                  </p>
                  <p className="mt-3 text-sm italic text-muted">{d.latin}</p>
                  <p className="mt-2 text-sm leading-relaxed">{d.arti}</p>
                </div>
              ) : null}
            </li>
          );
        })}
      </ul>
    </div>
  );
}
