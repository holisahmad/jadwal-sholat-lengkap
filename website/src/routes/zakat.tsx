import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { useNur } from "@/lib/store";
import { createSeoMeta, SITE_URL } from "@/lib/seo";

export const Route = createFileRoute("/zakat")({
  head: () =>
    createSeoMeta({
      title: "Kalkulator Zakat Maal & Penghasilan Online Akurat Sesuai Syariat",
      description:
        "Hitung kewajiban zakat maal (tabungan, simpanan, emas, perdagangan) dan zakat penghasilan secara online & akurat. Berdasarkan nisab 85 gram emas dan haul 1 tahun sesuai ketentuan syariat Islam.",
      keywords: [
        "kalkulator zakat",
        "hitung zakat online",
        "kalkulator zakat maal",
        "zakat penghasilan",
        "nisab zakat emas",
        "kalkulator zakat profesi",
        "syariat zakat",
        "zakat tabungan",
      ],
      path: "/zakat",
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
              name: "Kalkulator Zakat",
              item: `${SITE_URL}/zakat`,
            },
          ],
        },
        {
          "@context": "https://schema.org",
          "@type": "WebApplication",
          name: "Kalkulator Zakat Maal & Penghasilan",
          url: `${SITE_URL}/zakat`,
          applicationCategory: "FinanceApplication",
          operatingSystem: "All",
          description: "Kalkulator hisab zakat maal, emas, perak, dan perdagangan berdasarkan nisab 85 gram emas.",
        },
      ],
    }),
  component: ZakatPage,
});

const GOLD_NISAB_GRAM = 85;
const RATE = 0.025;

function idr(n: number): string {
  return new Intl.NumberFormat("id-ID", {
    style: "currency",
    currency: "IDR",
    maximumFractionDigits: 0,
  }).format(Math.max(0, Math.round(n)));
}

function ZakatPage() {
  const goldPrice = useNur((s) => s.goldPrice);
  const setGoldPrice = useNur((s) => s.setGoldPrice);
  const [cash, setCash] = useState(0);
  const [goldGram, setGoldGram] = useState(0);
  const [silver, setSilver] = useState(0);
  const [trade, setTrade] = useState(0);

  const nisab = goldPrice * GOLD_NISAB_GRAM;
  const wealth = cash + goldGram * goldPrice + silver + trade;
  const due = wealth >= nisab ? wealth * RATE : 0;
  const note = useMemo(() => {
    if (wealth <= 0) return "Isi harta yang sudah haul setahun.";
    if (wealth < nisab) return "Belum mencapai nisab (setara 85 gram emas).";
    return "Wajib zakat 2,5% dari harta produktif.";
  }, [wealth, nisab]);

  return (
    <div className="flex flex-col gap-5">
      <header>
        <p className="text-xs uppercase tracking-[0.2em] text-muted">Hisab</p>
        <h1 className="font-display mt-1 text-3xl tracking-tight">Zakat mal</h1>
        <p className="mt-2 text-sm text-muted">Kalkulator sederhana untuk tabungan, emas, dan dagangan.</p>
      </header>

      <section className="rounded-xl bg-surface p-5">
        <p className="text-xs uppercase tracking-[0.16em] text-muted">Perkiraan zakat</p>
        <p className="font-display mt-2 text-4xl tracking-tight">{idr(due)}</p>
        <p className="mt-2 text-sm text-muted">{note}</p>
        <p className="mt-3 text-xs text-faint">Nisab saat ini {idr(nisab)}</p>
      </section>

      <Field
        label="Harga emas per gram"
        value={goldPrice}
        onChange={setGoldPrice}
        hint="Perbarui sesuai harga toko hari ini"
      />
      <Field label="Uang & tabungan" value={cash} onChange={setCash} />
      <Field label="Emas (gram)" value={goldGram} onChange={setGoldGram} suffix="g" />
      <Field label="Perak (nilai rupiah)" value={silver} onChange={setSilver} />
      <Field label="Harta dagangan" value={trade} onChange={setTrade} />

      <p className="text-xs leading-relaxed text-faint">
        Ini alat bantu, bukan fatwa. Konsultasikan nisab, haul, dan jenis harta kepada lembaga amil atau
        ustaz yang Anda percayai.
      </p>
    </div>
  );
}

function Field({
  label,
  value,
  onChange,
  hint,
  suffix,
}: {
  label: string;
  value: number;
  onChange: (n: number) => void;
  hint?: string;
  suffix?: string;
}) {
  return (
    <label className="flex flex-col gap-2 text-sm">
      {label}
      <span className="relative">
        <input
          inputMode="decimal"
          value={value || ""}
          onChange={(e) => onChange(Number(e.target.value.replace(/[^\d.]/g, "")) || 0)}
          className="h-12 w-full rounded-md border border-border bg-surface px-3 tabular-nums outline-none focus:border-primary"
          placeholder="0"
        />
        {suffix ? (
          <span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-xs text-muted">
            {suffix}
          </span>
        ) : null}
      </span>
      {hint ? <span className="text-xs text-faint">{hint}</span> : null}
    </label>
  );
}
