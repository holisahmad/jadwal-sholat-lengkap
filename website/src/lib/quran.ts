export type Ayah = { number: number; arab: string; id: string };
export type SurahText = {
  number: number;
  name: string;
  englishName: string;
  englishNameTranslation: string;
  revelationType: string;
  ayahs: Ayah[];
};

let cache: SurahText[] | null = null;
let pending: Promise<SurahText[]> | null = null;

function clean(s: string): string {
  return s.replace(/^\uFEFF/, "").trim();
}

export async function loadQuran(): Promise<SurahText[]> {
  if (cache) return cache;
  if (!pending) {
    pending = fetch("/data/quran.json")
      .then((r) => {
        if (!r.ok) throw new Error("Gagal memuat Al-Quran");
        return r.json() as Promise<{ surahs: SurahText[] }>;
      })
      .then((d) => {
        cache = d.surahs.map((s) => ({
          ...s,
          ayahs: s.ayahs.map((a) => ({ ...a, arab: clean(a.arab), id: clean(a.id) })),
        }));
        return cache;
      });
  }
  return pending;
}

export async function loadSurah(n: number): Promise<SurahText | undefined> {
  const all = await loadQuran();
  return all.find((s) => s.number === n);
}

export function dailyFeaturedIndex(): number {
  const start = new Date(new Date().getFullYear(), 0, 0);
  const diff = Date.now() - start.getTime();
  const day = Math.floor(diff / 86_400_000);
  return day;
}
