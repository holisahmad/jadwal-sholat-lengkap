import { create } from "zustand";
import { persist } from "zustand/middleware";
import { CITIES, DEFAULT_CITY, type City } from "./cities";
import type { MethodId } from "./prayer";
import { TRACKABLE, type PrayerKey } from "./prayer";
import { type NotifSettings, DEFAULT_NOTIF } from "./notification";

export type DayLog = Partial<Record<PrayerKey, boolean>> & {
  dzikirPagi?: boolean;
  dzikirPetang?: boolean;
  bacaQuran?: boolean;
  dhuha?: boolean;
  rawatib?: boolean;
  tahajjud?: boolean;
};

type NurState = {
  // Settings
  cityId: string;
  method: MethodId;
  goldPrice: number;
  notif: NotifSettings;

  // Quran progress
  lastSurah: number;
  lastAyah: number;
  bookmarks: number[];

  // Ibadah tracking
  logs: Record<string, DayLog>;
  tasbihCount: number;
  tasbihTarget: number;

  // Actions
  setCity: (id: string) => void;
  setMethod: (m: MethodId) => void;
  setGoldPrice: (n: number) => void;
  setNotif: (n: NotifSettings) => void;
  setLastRead: (surah: number, ayah: number) => void;
  toggleBookmark: (n: number) => void;
  togglePrayer: (day: string, key: PrayerKey) => void;
  toggleExtra: (day: string, key: keyof DayLog) => void;
  setTasbihCount: (n: number) => void;
  setTasbihTarget: (n: number) => void;
  resetTasbih: () => void;
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
      notif: DEFAULT_NOTIF,
      lastSurah: 1,
      lastAyah: 1,
      bookmarks: [1, 36, 67, 112],
      logs: {},
      tasbihCount: 0,
      tasbihTarget: 33,
      setCity: (id) => set({ cityId: id }),
      setMethod: (m) => set({ method: m }),
      setGoldPrice: (n) => set({ goldPrice: n }),
      setNotif: (n) => set({ notif: n }),
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
      toggleExtra: (day, key) => {
        const logs = { ...get().logs };
        const row = { ...(logs[day] ?? {}) } as DayLog;
        (row as Record<string, boolean | undefined>)[key as string] = !(row as Record<string, boolean | undefined>)[key as string];
        logs[day] = row;
        set({ logs });
      },
      setTasbihCount: (n) => set({ tasbihCount: n }),
      setTasbihTarget: (n) => set({ tasbihTarget: n }),
      resetTasbih: () => set({ tasbihCount: 0 }),
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

/** Get last N day keys including today */
export function lastNDays(n: number, from = new Date()): string[] {
  const days: string[] = [];
  const d = new Date(from);
  for (let i = 0; i < n; i++) {
    days.unshift(todayKey(d));
    d.setDate(d.getDate() - 1);
  }
  return days;
}
