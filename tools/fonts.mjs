// Converts the site's woff2 fonts to TTF for res/font (Android cannot load woff2).
// Run from tools/ after `npm install`: node fonts.mjs
import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import wawoff2 from "wawoff2";

const here = dirname(fileURLToPath(import.meta.url));
const SITE = resolve(process.env.SITE ?? "C:/Users/admin/sda-church-nextjs/quiz-qwik");
const OUT = resolve(here, "../app/src/main/res/font");
mkdirSync(OUT, { recursive: true });

// site file → Android resource name (lowercase, letters/digits/underscore only)
const FONTS = {
  "fraunces-latin.woff2": "fraunces",
  "fraunces-latin-ext.woff2": "fraunces_ext",
  "fraunces-italic-latin.woff2": "fraunces_italic",
  "fraunces-italic-latin-ext.woff2": "fraunces_italic_ext",
  "inter-latin.woff2": "inter",
  "inter-latin-ext.woff2": "inter_ext",
  "dm-sans-latin.woff2": "dm_sans",
  "dm-sans-latin-ext.woff2": "dm_sans_ext",
  "playfair-display-latin.woff2": "playfair",
  "playfair-display-latin-ext.woff2": "playfair_ext",
};

for (const [src, name] of Object.entries(FONTS)) {
  const woff2 = readFileSync(join(SITE, "src/fonts", src));
  const ttf = Buffer.from(await wawoff2.decompress(woff2));
  writeFileSync(join(OUT, `${name}.ttf`), ttf);
  console.log(`${name}.ttf  ${(ttf.length / 1024).toFixed(0)} KB`);
}
