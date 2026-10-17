const HIJRI_MONTHS = [
  "Muharram",
  "Safar",
  "Rabiul Awal",
  "Rabiul Akhir",
  "Jumadil Awal",
  "Jumadil Akhir",
  "Rajab",
  "Sya'ban",
  "Ramadan",
  "Syawal",
  "Zulkaidah",
  "Zulhijah",
];

export function formatHijri(date = new Date()): string {
  try {
    const fmt = new Intl.DateTimeFormat("id-ID-u-ca-islamic", {
      day: "numeric",
      month: "long",
      year: "numeric",
    });
    return fmt.format(date).replace(" AH", " H").replace(" Era Hijriah", " H");
  } catch {
    return formatHijriFallback(date);
  }
}

export function hijriParts(date = new Date()): { day: number; month: string; year: number } {
  try {
    const parts = new Intl.DateTimeFormat("en-u-ca-islamic", {
      day: "numeric",
      month: "numeric",
      year: "numeric",
    }).formatToParts(date);
    const day = Number(parts.find((p) => p.type === "day")?.value ?? 1);
    const monthIndex = Number(parts.find((p) => p.type === "month")?.value ?? 1) - 1;
    const year = Number(parts.find((p) => p.type === "year")?.value ?? 1447);
    return { day, month: HIJRI_MONTHS[monthIndex] ?? HIJRI_MONTHS[0], year };
  } catch {
    return { day: 1, month: HIJRI_MONTHS[0], year: 1447 };
  }
}

export function formatGregorianId(date = new Date()): string {
  return new Intl.DateTimeFormat("id-ID", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(date);
}

function formatHijriFallback(date: Date): string {
  const { day, month, year } = hijriParts(date);
  return `${day} ${month} ${year} H`;
}
