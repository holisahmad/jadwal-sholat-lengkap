import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { QiblaCompass } from "@/components/qibla-compass";
import { CITIES } from "@/lib/cities";
import {
  PRAYER_KEYS,
  PRAYER_LABEL,
  TRACKABLE,
  buildTimes,
  formatTime,
  timesMap,
  type MethodId,
} from "@/lib/prayer";
import { cityById, todayKey, useNur } from "@/lib/store";
import { cn } from "@/lib/utils";
import { createSeoMeta, SITE_URL } from "@/lib/seo";

export const Route = createFileRoute("/sholat")({
  head: () =>
    createSeoMeta({
      title: "Jadwal Sholat Hari Ini, Waktu Imsakiyah & Kompas Kiblat Akurat",
      description:
        "Jadwal sholat hari ini otomatis seluruh kota di Indonesia akurat hisab Kemenag (Subuh, Terbit, Dhuha, Dzuhur, Ashar, Maghrib, Isya, Imsak). Dilengkapi kompas penunjuk arah kiblat online presisi.",
      keywords: [
        "jadwal sholat hari ini",
        "jadwal sholat indonesia",
        "waktu sholat jakarta",
        "waktu sholat surabaya",
        "kompas kiblat online",
        "arah kiblat akurat",
        "jadwal imsakiyah",
        "waktu subuh hari ini",
        "waktu maghrib hari ini",
        "hisab kemenag",
      ],
      path: "/sholat",
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
              name: "Jadwal Sholat & Kiblat",
              item: `${SITE_URL}/sholat`,
            },
          ],
        },
        {
          "@context": "https://schema.org",
          "@type": "FAQPage",
          mainEntity: [
            {
              "@type": "Question",
              name: "Bagaimana cara menentukan arah kiblat online yang akurat?",
              acceptedAnswer: {
                "@type": "Answer",
                text: "Kompas kiblat kami menghitung koordinat geografis Ka'bah di Mekkah (21.4225° N, 39.8262° E) terhadap lokasi kota Anda menggunakan rumus trigonometri bola (spherical trigonometry), serta memanfaatkan sensor giroskop perangkat secara real-time.",
              },
            },
            {
              "@type": "Question",
              name: "Metode hisab apa yang digunakan untuk jadwal sholat di Indonesia?",
              acceptedAnswer: {
                "@type": "Answer",
                text: "Secara default aplikasi menggunakan hisab standar Kementerian Agama Republik Indonesia (Kemenag / Singapore Muis) dengan sudut Subuh 20° dan Isya 18°. Anda juga dapat memilih metode MWL (Muslim World League) atau Egyptian.",
              },
            },
            {
              "@type": "Question",
              name: "Kapan waktu imsak dimulai?",
              acceptedAnswer: {
                "@type": "Answer",
                text: "Waktu imsak di Indonesia umumnya ditetapkan 10 menit sebelum adzan Subuh berkumandang sebagai waktu kehati-hatian (ihtiyath) untuk menyudahi sahur.",
              },
            },
          ],
        },
      ],
    }),
  component: SholatPage,
});

const METHODS: { id: MethodId; label: string }[] = [
  { id: "singapore", label: "Kemenag / Singapore" },
  { id: "mwl", label: "Muslim World League" },
  { id: "egypt", label: "Egyptian" },
];

function SholatPage() {
  const cityId = useNur((s) => s.cityId);
  const method = useNur((s) => s.method);
  const setCity = useNur((s) => s.setCity);
  const setMethod = useNur((s) => s.setMethod);
  const logs = useNur((s) => s.logs);
  const togglePrayer = useNur((s) => s.togglePrayer);
  const [now] = useState(() => new Date());
  const city = cityById(cityId);
  const pt = useMemo(() => buildTimes(city, now, method), [city, now, method]);
  const map = timesMap(city, pt, now);
  const day = todayKey(now);
  const log = logs[day] ?? {};

  return (
    <div className="flex flex-col gap-5">
      <header>
        <p className="text-xs uppercase tracking-[0.2em] text-muted">Waktu</p>
        <h1 className="font-display mt-1 text-3xl tracking-tight">Sholat</h1>
      </header>

      <label className="flex flex-col gap-2 text-sm">
        Kota
        <select
          value={cityId}
          onChange={(e) => setCity(e.target.value)}
          className="h-12 rounded-md border border-border bg-surface px-3 text-fg"
        >
          {CITIES.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </label>

      <label className="flex flex-col gap-2 text-sm">
        Metode hisab
        <select
          value={method}
          onChange={(e) => setMethod(e.target.value as MethodId)}
          className="h-12 rounded-md border border-border bg-surface px-3 text-fg"
        >
          {METHODS.map((m) => (
            <option key={m.id} value={m.id}>
              {m.label}
            </option>
          ))}
        </select>
      </label>

      <ul className="overflow-hidden rounded-xl border border-border bg-surface">
        {PRAYER_KEYS.map((key) => {
          const track = TRACKABLE.includes(key);
          const on = Boolean(log[key]);
          return (
            <li key={key} className="flex items-center justify-between border-b border-border px-4 py-3 last:border-0">
              <div>
                <p className="font-medium">{PRAYER_LABEL[key]}</p>
                <p className="text-xs text-muted">{track ? "Wajib" : "Penanda"}</p>
              </div>
              <div className="flex items-center gap-3">
                <span className="tabular-nums text-lg">{formatTime(map[key], city.tz)}</span>
                {track ? (
                  <button
                    type="button"
                    onClick={() => togglePrayer(day, key)}
                    className={cn(
                      "h-9 min-w-16 rounded-sm px-3 text-xs",
                      on ? "bg-primary text-primary-fg" : "bg-raised text-muted",
                    )}
                  >
                    {on ? "Selesai" : "Tandai"}
                  </button>
                ) : null}
              </div>
            </li>
          );
        })}
      </ul>

      <QiblaCompass city={city} />

      <section className="rounded-xl border border-border bg-surface p-5">
        <h2 className="font-display text-lg tracking-tight">Tanya Jawab Seputar Waktu Sholat & Kiblat</h2>
        <div className="mt-4 flex flex-col gap-4 text-xs text-muted leading-relaxed">
          <div>
            <h3 className="font-medium text-fg">Bagaimana cara menentukan arah kiblat online yang akurat?</h3>
            <p className="mt-1">
              Kompas kiblat kami menghitung koordinat geografis Ka'bah di Mekkah (21.4225° N, 39.8262° E) terhadap lokasi kota Anda menggunakan rumus trigonometri bola (spherical trigonometry), serta memanfaatkan sensor giroskop perangkat Anda secara real-time.
            </p>
          </div>
          <div>
            <h3 className="font-medium text-fg">Metode hisab apa yang digunakan untuk jadwal sholat di Indonesia?</h3>
            <p className="mt-1">
              Secara default aplikasi menggunakan hisab standar Kementerian Agama Republik Indonesia (Kemenag / Singapore Muis) dengan sudut Subuh 20° dan Isya 18°. Anda juga dapat memilih metode MWL (Muslim World League) atau Egyptian.
            </p>
          </div>
          <div>
            <h3 className="font-medium text-fg">Kapan waktu imsak dimulai?</h3>
            <p className="mt-1">
              Waktu imsak di Indonesia umumnya ditetapkan 10 menit sebelum adzan Subuh berkumandang sebagai waktu kehati-hatian (ihtiyath) untuk menyudahi sahur.
            </p>
          </div>
        </div>
      </section>
    </div>
  );
}
