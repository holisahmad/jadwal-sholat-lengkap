export const SITE_URL = "https://numos-beige.vercel.app";
export const SITE_NAME = "Jadwal Sholat Lengkap";
export const AUTHOR_NAME = "NUMOS Studio";
export const DEFAULT_OG_IMAGE = `${SITE_URL}/og.jpg`;

export interface SeoProps {
  title: string;
  description: string;
  keywords?: string[];
  path?: string;
  ogType?: "website" | "article";
  image?: string;
  jsonLd?: Record<string, unknown> | Array<Record<string, unknown>>;
}

export function createSeoMeta(props: SeoProps) {
  const fullTitle = props.title.includes(SITE_NAME)
    ? props.title
    : `${props.title} — ${SITE_NAME}`;
  const canonicalUrl = props.path
    ? `${SITE_URL}${props.path.startsWith("/") ? props.path : `/${props.path}`}`
    : SITE_URL;
  const imageUrl = props.image || DEFAULT_OG_IMAGE;

  const meta: Array<Record<string, string>> = [
    { title: fullTitle },
    { name: "description", content: props.description },
    { name: "robots", content: "index, follow, max-snippet:-1, max-image-preview:large, max-video-preview:-1" },
    { name: "author", content: AUTHOR_NAME },
    { name: "publisher", content: AUTHOR_NAME },
    { name: "language", content: "id" },
    { name: "geo.region", content: "ID" },
    { name: "geo.placename", content: "Indonesia" },

    // Open Graph
    { property: "og:site_name", content: SITE_NAME },
    { property: "og:title", content: fullTitle },
    { property: "og:description", content: props.description },
    { property: "og:url", content: canonicalUrl },
    { property: "og:type", content: props.ogType || "website" },
    { property: "og:locale", content: "id_ID" },
    { property: "og:image", content: imageUrl },
    { property: "og:image:width", content: "1200" },
    { property: "og:image:height", content: "630" },
    { property: "og:image:alt", content: fullTitle },

    // Twitter Card
    { name: "twitter:card", content: "summary_large_image" },
    { name: "twitter:title", content: fullTitle },
    { name: "twitter:description", content: props.description },
    { name: "twitter:image", content: imageUrl },
  ];

  if (props.keywords && props.keywords.length > 0) {
    meta.push({ name: "keywords", content: props.keywords.join(", ") });
  }

  const links: Array<Record<string, string>> = [
    { rel: "canonical", href: canonicalUrl },
  ];

  const scripts: Array<Record<string, unknown>> = [];
  if (props.jsonLd) {
    const schemas = Array.isArray(props.jsonLd) ? props.jsonLd : [props.jsonLd];
    for (const schema of schemas) {
      scripts.push({
        type: "application/ld+json",
        children: JSON.stringify(schema),
      });
    }
  }

  return { meta, links, scripts };
}
