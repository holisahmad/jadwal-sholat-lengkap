import { useEffect, useState } from "react";
import { qiblaBearing } from "@/lib/prayer";
import type { City } from "@/lib/cities";

export function QiblaCompass({ city }: { city: City }) {
  const qibla = qiblaBearing(city);
  const [heading, setHeading] = useState<number | null>(null);

  useEffect(() => {
    const handler = (e: DeviceOrientationEvent) => {
      const webkit = e as DeviceOrientationEvent & { webkitCompassHeading?: number };
      let h: number | null = null;
      if (typeof webkit.webkitCompassHeading === "number") h = webkit.webkitCompassHeading;
      else if (e.alpha != null) h = (360 - e.alpha) % 360;
      if (h != null) setHeading(h);
    };
    window.addEventListener("deviceorientation", handler);
    return () => window.removeEventListener("deviceorientation", handler);
  }, []);

  const needle = heading == null ? qibla : (qibla - heading + 360) % 360;

  return (
    <div className="flex flex-col items-center gap-4 rounded-xl bg-surface p-6 shadow-[var(--shadow-soft)]">
      <p className="text-xs uppercase tracking-[0.18em] text-muted">Arah kiblat</p>
      <div className="relative size-52">
        <div className="absolute inset-0 rounded-full border border-border bg-raised" />
        <div className="absolute inset-6 rounded-full border border-border/70" />
        {["U", "T", "S", "B"].map((label, i) => (
          <span
            key={label}
            className="absolute left-1/2 top-1/2 text-xs text-muted"
            style={{
              transform: `rotate(${i * 90}deg) translateY(-6.3rem) rotate(${-i * 90}deg) translateX(-50%)`,
            }}
          >
            {label}
          </span>
        ))}
        <div
          className="absolute left-1/2 top-1/2 h-[5.4rem] w-1 origin-bottom rounded-full bg-primary"
          style={{ transform: `translate(-50%, -100%) rotate(${needle}deg)` }}
        />
        <div className="absolute left-1/2 top-1/2 size-3 -translate-x-1/2 -translate-y-1/2 rounded-full bg-fg" />
      </div>
      <p className="text-sm text-muted">
        {Math.round(qibla)}° dari utara
        {heading == null ? " · putar perangkat untuk kompas hidup" : ` · heading ${Math.round(heading)}°`}
      </p>
    </div>
  );
}
