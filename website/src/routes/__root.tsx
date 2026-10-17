import { createRootRoute, HeadContent, Outlet, Scripts } from "@tanstack/react-router";
import { AuthProvider } from "@/lib/auth/provider";
import { PreviewHostBridge } from "@/components/preview-host-bridge";
import { Shell } from "@/components/shell";
import appCss from "../styles.css?url";

const APP_NAME = "Jadwal Sholat Lengkap";
const SITE_URL = "https://numos-beige.vercel.app";

export const Route = createRootRoute({
  head: () => ({
    meta: [
      { charSet: "utf-8" },
      { name: "viewport", content: "width=device-width, initial-scale=1, viewport-fit=cover" },
      { title: `${APP_NAME} — Waktu Sholat, Arah Kiblat & Al-Quran Digital` },
      { name: "theme-color", content: "#0c1210" },
      {
        name: "description",
        content:
          "Jadwal sholat lengkap dan waktu sholat hari ini otomatis seluruh kota di Indonesia akurat Kemenag. Dilengkapi kompas arah kiblat online, Al-Quran digital 30 juz, doa harian, dan kalkulator zakat.",
      },
      { name: "author", content: "NUMOS Studio" },
      { name: "publisher", content: "NUMOS Studio" },
      { name: "robots", content: "index, follow, max-snippet:-1, max-image-preview:large, max-video-preview:-1" },
      { name: "language", content: "id" },
      { name: "geo.region", content: "ID" },
      { name: "geo.placename", content: "Indonesia" },

      // Open Graph
      { property: "og:site_name", content: APP_NAME },
      { property: "og:title", content: `${APP_NAME} — Waktu Sholat, Arah Kiblat & Al-Quran Digital` },
      {
        property: "og:description",
        content:
          "Jadwal sholat lengkap dan waktu sholat hari ini otomatis seluruh kota di Indonesia akurat Kemenag. Dilengkapi kompas arah kiblat online, Al-Quran digital 30 juz, doa harian, dan kalkulator zakat.",
      },
      { property: "og:type", content: "website" },
      { property: "og:url", content: SITE_URL },
      { property: "og:locale", content: "id_ID" },
      { property: "og:image", content: `${SITE_URL}/og.jpg` },
      { property: "og:image:width", content: "1200" },
      { property: "og:image:height", content: "630" },

      // Twitter Card
      { name: "twitter:card", content: "summary_large_image" },
      { name: "twitter:title", content: `${APP_NAME} — Waktu Sholat, Arah Kiblat & Al-Quran Digital` },
      {
        name: "twitter:description",
        content:
          "Jadwal sholat lengkap dan waktu sholat hari ini otomatis seluruh kota di Indonesia akurat Kemenag.",
      },
      { name: "twitter:image", content: `${SITE_URL}/og.jpg` },
    ],
    links: [
      { rel: "canonical", href: SITE_URL },
      { rel: "icon", type: "image/svg+xml", href: "/favicon.svg" },
      { rel: "stylesheet", href: appCss },
      { rel: "manifest", href: "/__grok/manifest.webmanifest" },
      { rel: "apple-touch-icon", href: "/__grok/icon-180.png" },
      { rel: "preconnect", href: "https://fonts.googleapis.com" },
      { rel: "preconnect", href: "https://fonts.gstatic.com", crossOrigin: "anonymous" },
      {
        rel: "stylesheet",
        href: "https://fonts.googleapis.com/css2?family=Figtree:wght@400;500;600&family=Fraunces:opsz,wght@9..144,500;9..144,600&family=Noto+Naskh+Arabic:wght@500;600&display=swap",
      },
    ],
    scripts: [
      {
        type: "application/ld+json",
        children: JSON.stringify({
          "@context": "https://schema.org",
          "@graph": [
            {
              "@type": "WebApplication",
              "@id": `${SITE_URL}/#app`,
              name: APP_NAME,
              url: SITE_URL,
              applicationCategory: "LifestyleApplication",
              operatingSystem: "All",
              inLanguage: "id-ID",
              description:
                "Aplikasi web jadwal sholat lengkap, waktu sholat hari ini, arah kiblat, Al-Quran digital 30 juz, kumpulan doa, dan kalkulator zakat.",
              offers: {
                "@type": "Offer",
                price: "0",
                priceCurrency: "IDR",
              },
              author: {
                "@type": "Organization",
                "@id": `${SITE_URL}/#organization`,
                name: "NUMOS Studio",
                url: SITE_URL,
              },
            },
            {
              "@type": "Organization",
              "@id": `${SITE_URL}/#organization`,
              name: "NUMOS Studio",
              url: SITE_URL,
              logo: `${SITE_URL}/favicon.svg`,
            },
          ],
        }),
      },
    ],
  }),
  component: Root,
});

function Root() {
  return (
    <html lang="id" suppressHydrationWarning>
      <head>
        <HeadContent />
      </head>
      <body>
        <PreviewHostBridge />
        <AuthProvider>
          <Shell>
            <Outlet />
          </Shell>
        </AuthProvider>
        <Scripts />
      </body>
    </html>
  );
}
