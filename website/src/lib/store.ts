import { create } from "zustand";
import { persist } from "zustand/middleware";
import { CITIES, DEFAULT_CITY, type City } from "./cities";
import type { MethodId } from "./prayer";
import { TRACKABLE, type PrayerKey } from "./prayer";

export type DayLog = Partial<Record<PrayerKey, boolean>>;

type NurState = {
  cityId: string;
  method: MethodId;
  goldPrice: number;
  lastSurah: number;
  lastAyah: number;
  bookmarks: number[];
  logs: Record<string, DayLog>;
  setCity: (id: string) => void;
  setMethod: (m: MethodId) => void;
  setGoldPrice: (n: number) => void;
  setLastRead: (surah: number, ayah: number) => void;
  toggleBookmark: (n: number) => void;
  togglePrayer: (day: string, key: PrayerKey) => void;
};

export function todayKey(d = new Date()): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

export function cityById(id: string): City {
  return CITIES.find((c) => c.id === id) ?? DEFAULT_CITY;
}

export const useNur = create<NurState>()(
  persist(
    (set, get) => ({
      cityId: DEFAULT_CITY.id,
      method: "singapore",
      goldPrice: 1_450_000,
      lastSurah: 1,
      lastAyah: 1,
      bookmarks: [1, 36, 67, 112],
      logs: {},
      setCity: (id) => set({ cityId: id }),
      setMethod: (m) => set({ method: m }),
      setGoldPrice: (n) => set({ goldPrice: n }),
      setLastRead: (surah, ayah) => set({ lastSurah: surah, lastAyah: ayah }),
      toggleBookmark: (n) => {
        const cur = get().bookmarks;
        set({
          bookmarks: cur.includes(n) ? cur.filter((x) => x !== n) : [...cur, n].sort((a, b) => a - b),
        });
      },
      togglePrayer: (day, key) => {
        const logs = { ...get().logs };
        const row = { ...(logs[day] ?? {}) };
        row[key] = !row[key];
        logs[day] = row;
        set({ logs });
      },
    }),
    { name: "nur-ibadah" },
  ),
);

export function streakCount(logs: Record<string, DayLog>, from = new Date()): number {
  let n = 0;
  const d = new Date(from);
  for (let i = 0; i < 60; i++) {
    const key = todayKey(d);
    const row = logs[key];
    const complete = TRACKABLE.every((p) => row?.[p]);
    if (!complete) {
      if (i === 0) {
        d.setDate(d.getDate() - 1);
        continue;
      }
      break;
    }
    n += 1;
    d.setDate(d.getDate() - 1);
  }
  return n;
}
