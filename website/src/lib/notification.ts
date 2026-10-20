import type { PrayerKey } from "./prayer";
import { PRAYER_LABEL, buildTimes, timesMap } from "./prayer";
import type { City } from "./cities";
import type { MethodId } from "./prayer";

export type NotifOffset = 0 | 5 | 10 | 15;

export type NotifSettings = {
  enabled: boolean;
  prayers: Partial<Record<PrayerKey, boolean>>;
  offsetMin: NotifOffset;
};

export const DEFAULT_NOTIF: NotifSettings = {
  enabled: false,
  prayers: { fajr: true, dhuhr: true, asr: true, maghrib: true, isha: true },
  offsetMin: 0,
};

/** Request browser notification permission. Returns true if granted. */
export async function requestPermission(): Promise<boolean> {
  if (!("Notification" in window)) return false;
  if (Notification.permission === "granted") return true;
  if (Notification.permission === "denied") return false;
  const result = await Notification.requestPermission();
  return result === "granted";
}

export function canNotify(): boolean {
  return "Notification" in window && Notification.permission === "granted";
}

// Store active timeouts so we can cancel them on reschedule
const _timers: ReturnType<typeof setTimeout>[] = [];

export function clearScheduled(): void {
  _timers.forEach(clearTimeout);
  _timers.length = 0;
}

/**
 * Schedule prayer-time notifications for the rest of today + tomorrow's fajr.
 * Call this once on app mount and whenever settings change.
 */
export function scheduleNotifications(
  city: City,
  method: MethodId,
  settings: NotifSettings,
): void {
  clearScheduled();
  if (!settings.enabled || !canNotify()) return;

  const now = new Date();
  const TRACKABLE_NOTIF: PrayerKey[] = ["fajr", "dhuhr", "asr", "maghrib", "isha"];

  // Today's times
  const pt = buildTimes(city, now, method);
  const map = timesMap(city, pt, now);

  // Tomorrow fajr
  const tomorrow = new Date(now.getTime() + 86_400_000);
  const ptTomorrow = buildTimes(city, tomorrow, method);
  const mapTomorrow = timesMap(city, ptTomorrow, tomorrow);

  const allSlots: { key: PrayerKey; at: Date }[] = [
    ...TRACKABLE_NOTIF.map((key) => ({ key, at: map[key] })),
    { key: "fajr" as PrayerKey, at: mapTomorrow.fajr },
  ];

  const offsetMs = (settings.offsetMin ?? 0) * 60_000;

  for (const { key, at } of allSlots) {
    if (!settings.prayers[key]) continue;
    const fireAt = new Date(at.getTime() - offsetMs);
    const delay = fireAt.getTime() - now.getTime();
    if (delay < 0) continue; // already passed

    const label = PRAYER_LABEL[key];
    const timeStr = at.toLocaleTimeString("id-ID", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
      timeZone: city.tz,
    });
    const title =
      settings.offsetMin > 0
        ? `${settings.offsetMin} menit lagi — ${label}`
        : `Waktu ${label} — ${timeStr}`;
    const body =
      settings.offsetMin > 0
        ? `Waktu ${label} pukul ${timeStr}. Bersiaplah untuk sholat.`
        : `Assalamualaikum. Waktu sholat ${label} telah tiba.`;

    const t = setTimeout(() => {
      if (!canNotify()) return;
      try {
        playChime();
        new Notification(title, {
          body,
          icon: "/favicon.svg",
          badge: "/favicon.svg",
          tag: `prayer-${key}`,
          renotify: true,
          silent: false,
        });
      } catch {
        // some browsers restrict new Notification() without SW
      }
    }, delay);

    _timers.push(t);
  }
}

/** Soft melodic Islamic tone using Web Audio API */
export function playChime(): void {
  if (typeof window === "undefined") return;
  try {
    const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
    if (!AudioCtx) return;
    const ctx = new AudioCtx();
    const now = ctx.currentTime;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = "sine";
    osc.frequency.setValueAtTime(329.63, now); // E4
    osc.frequency.setValueAtTime(392.0, now + 0.22); // G4
    osc.frequency.setValueAtTime(440.0, now + 0.48); // A4

    gain.gain.setValueAtTime(0.001, now);
    gain.gain.linearRampToValueAtTime(0.2, now + 0.05);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 1.2);

    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start(now);
    osc.stop(now + 1.2);
  } catch {
    // audio context blocked or not available
  }
}

/** Test notification immediately to verify permissions and browser support */
export function testNotification(): boolean {
  if (!canNotify()) return false;
  try {
    playChime();
    new Notification("🔔 Notifikasi Waktu Sholat Aktif", {
      body: "Alhamdulillah! Pengingat sholat siap mengingatkan Anda tepat waktu.",
      icon: "/favicon.svg",
      tag: "test-prayer",
    });
    return true;
  } catch {
    return false;
  }
}
