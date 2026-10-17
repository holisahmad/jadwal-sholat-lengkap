import {
  CalculationMethod,
  Coordinates,
  Madhab,
  PrayerTimes,
  Qibla,
} from "adhan";
import type { City } from "./cities";

export const PRAYER_KEYS = ["fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha"] as const;
export type PrayerKey = (typeof PRAYER_KEYS)[number];

export const PRAYER_LABEL: Record<PrayerKey, string> = {
  fajr: "Subuh",
  sunrise: "Terbit",
  dhuhr: "Dzuhur",
  asr: "Ashar",
  maghrib: "Maghrib",
  isha: "Isya",
};

export const TRACKABLE: PrayerKey[] = ["fajr", "dhuhr", "asr", "maghrib", "isha"];

export type MethodId = "singapore" | "mwl" | "egypt";

function zoneOffsetMs(timeZone: string, date: Date): number {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone,
    hour12: false,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  }).formatToParts(date);
  const get = (t: Intl.DateTimeFormatPartTypes) =>
    Number(parts.find((p) => p.type === t)?.value ?? "0");
  let hour = get("hour");
  if (hour === 24) hour = 0;
  const asUtc = Date.UTC(get("year"), get("month") - 1, get("day"), hour, get("minute"), get("second"));
  return asUtc - date.getTime();
}

function shiftForZone(city: City, date: Date): Date {
  const zone = zoneOffsetMs(city.tz, date);
  const local = -date.getTimezoneOffset() * 60_000;
  return new Date(date.getTime() + zone - local);
}

function unshiftFromZone(city: City, date: Date, shiftedClock: Date): Date {
  const zone = zoneOffsetMs(city.tz, date);
  const local = -date.getTimezoneOffset() * 60_000;
  return new Date(shiftedClock.getTime() - (zone - local));
}

export function buildTimes(city: City, date: Date, method: MethodId): PrayerTimes {
  const coords = new Coordinates(city.lat, city.lng);
  const params =
    method === "mwl"
      ? CalculationMethod.MuslimWorldLeague()
      : method === "egypt"
        ? CalculationMethod.Egyptian()
        : CalculationMethod.Singapore();
  params.madhab = Madhab.Shafi;
  return new PrayerTimes(coords, shiftForZone(city, date), params);
}

export function timesMap(city: City, pt: PrayerTimes, now: Date): Record<PrayerKey, Date> {
  return {
    fajr: unshiftFromZone(city, now, pt.fajr),
    sunrise: unshiftFromZone(city, now, pt.sunrise),
    dhuhr: unshiftFromZone(city, now, pt.dhuhr),
    asr: unshiftFromZone(city, now, pt.asr),
    maghrib: unshiftFromZone(city, now, pt.maghrib),
    isha: unshiftFromZone(city, now, pt.isha),
  };
}

export function formatTime(d: Date, timeZone: string): string {
  return new Intl.DateTimeFormat("id-ID", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
    timeZone,
  }).format(d);
}

export function nextPrayer(
  city: City,
  method: MethodId,
  now = new Date(),
): { key: PrayerKey; at: Date } {
  const pt = buildTimes(city, now, method);
  const map = timesMap(city, pt, now);
  for (const key of PRAYER_KEYS) {
    if (map[key].getTime() > now.getTime()) return { key, at: map[key] };
  }
  const tomorrow = new Date(now.getTime() + 36_000_000);
  const next = timesMap(city, buildTimes(city, tomorrow, method), tomorrow);
  return { key: "fajr", at: next.fajr };
}

export function countdownLabel(target: Date, now = new Date()): string {
  let ms = target.getTime() - now.getTime();
  if (ms < 0) ms = 0;
  const h = Math.floor(ms / 3_600_000);
  const m = Math.floor((ms % 3_600_000) / 60_000);
  const s = Math.floor((ms % 60_000) / 1000);
  if (h > 0) return `${h}j ${String(m).padStart(2, "0")}m`;
  return `${m}m ${String(s).padStart(2, "0")}d`;
}

export function qiblaBearing(city: City): number {
  return Qibla(new Coordinates(city.lat, city.lng));
}
