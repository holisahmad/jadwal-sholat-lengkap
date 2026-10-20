import { useEffect, useState } from "react";
import {
  AlertCircle,
  Bell,
  BellRing,
  Check,
  MapPin,
  Sparkles,
  Volume2,
  X,
} from "lucide-react";
import { CITIES } from "@/lib/cities";
import type { MethodId, PrayerKey } from "@/lib/prayer";
import { PRAYER_LABEL, TRACKABLE } from "@/lib/prayer";
import {
  canNotify,
  requestPermission,
  testNotification,
  type NotifOffset,
} from "@/lib/notification";
import { cityById, useNur } from "@/lib/store";
import { cn } from "@/lib/utils";

const METHODS: { id: MethodId; label: string; desc: string }[] = [
  {
    id: "singapore",
    label: "Kemenag RI / Muis Singapore",
    desc: "Metode resmi standar Kemenag & Asia Tenggara (Fajr 20°, Isha 18°)",
  },
  {
    id: "mwl",
    label: "Muslim World League (MWL)",
    desc: "Liga Dunia Islam (Fajr 18°, Isha 17°)",
  },
  {
    id: "egypt",
    label: "Egyptian General Authority",
    desc: "Otoritas Umum Mesir (Fajr 19.5°, Isha 17.5°)",
  },
];

const OFFSETS: { val: NotifOffset; label: string }[] = [
  { val: 0, label: "Tepat waktu" },
  { val: 5, label: "5 menit sebelum" },
  { val: 10, label: "10 menit sebelum" },
  { val: 15, label: "15 menit sebelum" },
];

export function SettingsModal({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) {
  const cityId = useNur((s) => s.cityId);
  const setCity = useNur((s) => s.setCity);
  const method = useNur((s) => s.method);
  const setMethod = useNur((s) => s.setMethod);
  const goldPrice = useNur((s) => s.goldPrice);
  const setGoldPrice = useNur((s) => s.setGoldPrice);
  const notif = useNur((s) => s.notif);
  const setNotif = useNur((s) => s.setNotif);

  const [activeTab, setActiveTab] = useState<"notif" | "location" | "zakat">("notif");
  const [citySearch, setCitySearch] = useState("");
  const [testSent, setTestSent] = useState(false);
  const [permStatus, setPermStatus] = useState<string>("default");

  useEffect(() => {
    if (typeof window !== "undefined" && "Notification" in window) {
      setPermStatus(Notification.permission);
    }
  }, [open]);

  if (!open) return null;

  const currentCity = cityById(cityId);

  const handleRequestPermission = async () => {
    const granted = await requestPermission();
    setPermStatus(granted ? "granted" : "denied");
    if (granted) {
      setNotif({ ...notif, enabled: true });
    }
  };

  const handleTogglePrayer = (key: PrayerKey) => {
    setNotif({
      ...notif,
      prayers: {
        ...notif.prayers,
        [key]: !notif.prayers[key],
      },
    });
  };

  const handleTestNotification = () => {
    const ok = testNotification();
    if (ok) {
      setTestSent(true);
      setTimeout(() => setTestSent(false), 3000);
    } else {
      handleRequestPermission();
    }
  };

  const filteredCities = CITIES.filter((c) =>
    c.name.toLowerCase().includes(citySearch.toLowerCase()),
  );

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/60 backdrop-blur-sm p-0 sm:p-4 animate-in fade-in duration-200">
      <div
        className="w-full max-w-lg rounded-t-2xl sm:rounded-2xl border border-border bg-surface shadow-2xl flex flex-col max-h-[90dvh]"
        role="dialog"
        aria-modal="true"
      >
        {/* Header */}
        <div className="flex items-center justify-between border-b border-border px-5 py-4">
          <div className="flex items-center gap-2">
            <div className="flex size-8 items-center justify-center rounded-lg bg-raised text-primary">
              <Sparkles className="size-4" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-fg">Pengaturan</h2>
              <p className="text-xs text-muted">Jadwal sholat & preferensi</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="flex size-8 items-center justify-center rounded-lg text-muted hover:bg-raised hover:text-fg transition-colors"
            aria-label="Tutup"
          >
            <X className="size-5" />
          </button>
        </div>

        {/* Tab switcher */}
        <div className="flex border-b border-border bg-bg/40 px-3 pt-2">
          <button
            type="button"
            onClick={() => setActiveTab("notif")}
            className={cn(
              "flex flex-1 items-center justify-center gap-1.5 pb-2.5 pt-1 text-xs font-medium border-b-2 transition-colors",
              activeTab === "notif"
                ? "border-primary text-primary"
                : "border-transparent text-muted hover:text-fg",
            )}
          >
            <Bell className="size-3.5" />
            Notifikasi
          </button>
          <button
            type="button"
            onClick={() => setActiveTab("location")}
            className={cn(
              "flex flex-1 items-center justify-center gap-1.5 pb-2.5 pt-1 text-xs font-medium border-b-2 transition-colors",
              activeTab === "location"
                ? "border-primary text-primary"
                : "border-transparent text-muted hover:text-fg",
            )}
          >
            <MapPin className="size-3.5" />
            Kota & Metode
          </button>
          <button
            type="button"
            onClick={() => setActiveTab("zakat")}
            className={cn(
              "flex flex-1 items-center justify-center gap-1.5 pb-2.5 pt-1 text-xs font-medium border-b-2 transition-colors",
              activeTab === "zakat"
                ? "border-primary text-primary"
                : "border-transparent text-muted hover:text-fg",
            )}
          >
            <span className="text-xs">💰</span>
            Zakat
          </button>
        </div>

        {/* Body content */}
        <div className="overflow-y-auto p-5 space-y-5 flex-1">
          {activeTab === "notif" && (
            <div className="space-y-5">
              {/* Main Toggle */}
              <div className="flex items-center justify-between rounded-xl border border-border bg-raised/50 p-4">
                <div className="space-y-0.5">
                  <p className="text-sm font-medium text-fg">Notifikasi Pengingat Adzan</p>
                  <p className="text-xs text-muted">
                    Kirimkan alert di browser saat masuk waktu sholat
                  </p>
                </div>
                <button
                  type="button"
                  onClick={async () => {
                    if (!notif.enabled) {
                      if (!canNotify()) {
                        await handleRequestPermission();
                      } else {
                        setNotif({ ...notif, enabled: true });
                      }
                    } else {
                      setNotif({ ...notif, enabled: false });
                    }
                  }}
                  className={cn(
                    "relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors",
                    notif.enabled ? "bg-primary" : "bg-faint/40",
                  )}
                  aria-pressed={notif.enabled}
                >
                  <span
                    className={cn(
                      "inline-block size-5 transform rounded-full bg-white transition-transform shadow-sm",
                      notif.enabled ? "translate-x-5" : "translate-x-0.5",
                    )}
                  />
                </button>
              </div>

              {/* Permission Banner if not granted */}
              {permStatus !== "granted" && (
                <div className="flex items-start gap-3 rounded-xl border border-amber-500/30 bg-amber-500/10 p-3.5 text-xs text-amber-200">
                  <AlertCircle className="size-4 shrink-0 mt-0.5 text-amber-400" />
                  <div className="flex-1 space-y-1.5">
                    <p className="font-medium text-amber-300">Izin Browser Diperlukan</p>
                    <p className="text-amber-200/80 leading-relaxed">
                      Browser memerlukan izin untuk menampilkan notifikasi pengingat sholat.
                    </p>
                    <button
                      type="button"
                      onClick={handleRequestPermission}
                      className="inline-flex items-center gap-1.5 rounded-lg bg-amber-500/20 px-3 py-1.5 text-xs font-medium text-amber-300 hover:bg-amber-500/30 transition-colors"
                    >
                      <BellRing className="size-3.5" />
                      Aktifkan Izin Notifikasi
                    </button>
                  </div>
                </div>
              )}

              {/* Waktu Pengingat (Offset) */}
              <div className="space-y-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted">
                  Waktu Pemberitahuan
                </label>
                <div className="grid grid-cols-2 gap-2">
                  {OFFSETS.map((item) => (
                    <button
                      key={item.val}
                      type="button"
                      onClick={() => setNotif({ ...notif, offsetMin: item.val })}
                      className={cn(
                        "flex items-center justify-between rounded-lg border p-3 text-xs text-left transition-colors",
                        notif.offsetMin === item.val
                          ? "border-primary bg-primary/10 text-primary font-medium"
                          : "border-border bg-raised/30 text-muted hover:border-border/80",
                      )}
                    >
                      <span>{item.label}</span>
                      {notif.offsetMin === item.val && <Check className="size-3.5 text-primary" />}
                    </button>
                  ))}
                </div>
              </div>

              {/* Pilihan Waktu Sholat */}
              <div className="space-y-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted">
                  Pilih Sholat yang Diingatkan
                </label>
                <div className="grid grid-cols-5 gap-2">
                  {TRACKABLE.map((key) => {
                    const active = notif.prayers[key] !== false;
                    return (
                      <button
                        key={key}
                        type="button"
                        onClick={() => handleTogglePrayer(key)}
                        className={cn(
                          "flex flex-col items-center justify-center gap-1.5 rounded-lg border py-3 text-xs transition-colors",
                          active
                            ? "border-primary bg-primary/10 text-primary font-medium"
                            : "border-border bg-raised/30 text-muted hover:border-border/80",
                        )}
                      >
                        <div
                          className={cn(
                            "flex size-4 items-center justify-center rounded",
                            active ? "bg-primary text-primary-fg" : "border border-border",
                          )}
                        >
                          {active && <Check className="size-3 stroke-[3]" />}
                        </div>
                        <span>{PRAYER_LABEL[key]}</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Test Button & Audio Info */}
              <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-2 border-t border-border">
                <div className="flex items-center gap-2 text-xs text-muted">
                  <Volume2 className="size-4 text-primary" />
                  <span>Disertai nada santun Web Audio</span>
                </div>
                <button
                  type="button"
                  onClick={handleTestNotification}
                  className="inline-flex items-center justify-center gap-2 rounded-lg border border-border bg-raised px-4 py-2 text-xs font-medium text-fg hover:border-primary/50 hover:text-primary transition-colors"
                >
                  <BellRing className="size-3.5" />
                  {testSent ? "Notifikasi Terkirim! ✅" : "Uji Coba Notifikasi"}
                </button>
              </div>
            </div>
          )}

          {activeTab === "location" && (
            <div className="space-y-5">
              {/* Kota Sekarang */}
              <div className="space-y-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted">
                  Kota / Wilayah Saat Ini
                </label>
                <div className="rounded-xl border border-primary/30 bg-primary/5 p-3 flex items-center justify-between">
                  <div className="flex items-center gap-2.5">
                    <MapPin className="size-4 text-primary" />
                    <div>
                      <p className="text-sm font-semibold text-fg">{currentCity.name}</p>
                      <p className="text-[11px] text-muted">
                        Zona waktu {currentCity.tz} · Lat {currentCity.lat}, Lng {currentCity.lng}
                      </p>
                    </div>
                  </div>
                  <span className="rounded-full bg-primary/20 px-2 py-0.5 text-[10px] font-medium text-primary">
                    Aktif
                  </span>
                </div>
              </div>

              {/* Search & Pilih Kota */}
              <div className="space-y-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted">
                  Ganti Kota
                </label>
                <input
                  type="search"
                  placeholder="Ketik nama kota (misal: Bandung, Surabaya)..."
                  value={citySearch}
                  onChange={(e) => setCitySearch(e.target.value)}
                  className="w-full rounded-lg border border-border bg-raised px-3.5 py-2 text-xs text-fg placeholder:text-muted focus:border-primary focus:outline-none"
                />
                <div className="grid grid-cols-2 gap-1.5 max-h-48 overflow-y-auto pr-1">
                  {filteredCities.map((c) => (
                    <button
                      key={c.id}
                      type="button"
                      onClick={() => setCity(c.id)}
                      className={cn(
                        "flex items-center justify-between rounded-lg border px-3 py-2 text-xs text-left transition-colors",
                        c.id === cityId
                          ? "border-primary bg-primary/10 text-primary font-medium"
                          : "border-border/60 bg-raised/30 text-muted hover:border-border hover:text-fg",
                      )}
                    >
                      <span className="truncate">{c.name}</span>
                      {c.id === cityId && <Check className="size-3 text-primary shrink-0" />}
                    </button>
                  ))}
                </div>
              </div>

              {/* Metode Perhitungan */}
              <div className="space-y-2 pt-2 border-t border-border">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted">
                  Metode Perhitungan Waktu
                </label>
                <div className="space-y-2">
                  {METHODS.map((m) => (
                    <button
                      key={m.id}
                      type="button"
                      onClick={() => setMethod(m.id)}
                      className={cn(
                        "w-full rounded-xl border p-3 text-left transition-colors",
                        method === m.id
                          ? "border-primary bg-primary/10"
                          : "border-border bg-raised/30 hover:border-border/80",
                      )}
                    >
                      <div className="flex items-center justify-between">
                        <p
                          className={cn(
                            "text-xs font-semibold",
                            method === m.id ? "text-primary" : "text-fg",
                          )}
                        >
                          {m.label}
                        </p>
                        {method === m.id && <Check className="size-3.5 text-primary" />}
                      </div>
                      <p className="mt-1 text-[11px] text-muted leading-relaxed">{m.desc}</p>
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}

          {activeTab === "zakat" && (
            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted">
                  Harga Emas per Gram (IDR)
                </label>
                <p className="text-xs text-muted">
                  Digunakan untuk menghitung nisab zakat mal (setara 85 gram emas murni).
                </p>
                <div className="relative mt-2">
                  <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-xs text-muted">
                    Rp
                  </span>
                  <input
                    type="number"
                    value={goldPrice}
                    onChange={(e) => setGoldPrice(Number(e.target.value) || 0)}
                    className="w-full rounded-lg border border-border bg-raised pl-10 pr-3 py-2 text-sm text-fg focus:border-primary focus:outline-none tabular-nums"
                  />
                </div>
                <div className="mt-3 rounded-lg border border-border/60 bg-raised/40 p-3 text-xs space-y-1">
                  <div className="flex justify-between text-muted">
                    <span>Nisab 85g Emas:</span>
                    <span className="font-semibold text-fg tabular-nums">
                      Rp {(85 * goldPrice).toLocaleString("id-ID")}
                    </span>
                  </div>
                  <div className="flex justify-between text-muted">
                    <span>Kadar Zakat:</span>
                    <span className="font-semibold text-primary">2.5% per tahun</span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="border-t border-border bg-bg/50 px-5 py-3 flex items-center justify-end">
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg bg-primary px-5 py-2 text-xs font-semibold text-primary-fg hover:opacity-90 transition-opacity"
          >
            Selesai
          </button>
        </div>
      </div>
    </div>
  );
}
