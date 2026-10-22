// Generates the 1200x630 Open Graph images in public/. Run: npm run og
import sharp from "sharp";
import { fileURLToPath } from "node:url";

const root = fileURLToPath(new URL("..", import.meta.url));
const W = 1200;
const H = 630;

const locales = {
  id: {
    out: "public/og.png",
    h1a: "Ubah notifikasi QRIS",
    h1b: "jadi webhook.",
    sub: ["Baca notifikasi pembayaran dari aplikasi merchant", "Android, kirim sebagai JSON ke backend Anda."],
  },
  en: {
    out: "public/og-en.png",
    h1a: "Turn QRIS notifications",
    h1b: "into webhooks.",
    sub: ["Reads payment notifications from merchant apps", "on Android and delivers them as JSON to your backend."],
  },
};

const esc = (s) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

function background(t) {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}">
  <defs>
    <radialGradient id="glow" cx="78%" cy="40%" r="60%">
      <stop offset="0" stop-color="#19b894" stop-opacity="0.35"/>
      <stop offset="1" stop-color="#19b894" stop-opacity="0"/>
    </radialGradient>
    <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
      <path d="M40 0H0V40" fill="none" stroke="#1c2e2a" stroke-width="1"/>
    </pattern>
  </defs>
  <rect width="${W}" height="${H}" fill="#07110f"/>
  <rect width="${W}" height="${H}" fill="url(#grid)" opacity="0.5"/>
  <rect width="${W}" height="${H}" fill="url(#glow)"/>
  <g transform="translate(72 64) scale(0.875)">
    <rect width="64" height="64" rx="14" fill="#0d9477"/>
    <path fill="#fff" d="M14 14h14v14H14zm22 0h14v14H36zM14 36h14v14H14zm22 0h6v6h-6zm8 8h6v6h-6zm-8 0h6v6h-6zm8-8h6v6h-6z"/>
  </g>
  <g font-family="Inter" fill="#e7f1ee">
    <text x="140" y="104" font-size="30" font-weight="700">QRIS Hook</text>
    <text x="72" y="268" font-size="60" font-weight="800" letter-spacing="-1.5">${esc(t.h1a)}</text>
    <text x="72" y="342" font-size="60" font-weight="800" letter-spacing="-1.5" fill="#19b894">${esc(t.h1b)}</text>
    <text x="72" y="410" font-size="25" font-weight="400" fill="#93a8a2">${esc(t.sub[0])}</text>
    <text x="72" y="444" font-size="25" font-weight="400" fill="#93a8a2">${esc(t.sub[1])}</text>
    <rect x="72" y="510" width="346" height="48" rx="24" fill="#0d1a17" stroke="#1c2e2a" stroke-width="2"/>
    <circle cx="100" cy="534" r="6" fill="#19b894"/>
    <text x="118" y="542" font-size="21" font-weight="600" fill="#e7f1ee">Open source · MIT · Android</text>
    <text x="446" y="542" font-size="21" font-weight="500" fill="#93a8a2">qrishook.pages.dev</text>
  </g>
</svg>`;
}

async function phone() {
  const w = 280;
  const h = 560;
  const r = 34;
  const shot = await sharp(`${root}src/assets/screenshots/1.jpg`)
    .resize(w, h, { fit: "cover", position: "top" })
    .composite([{ input: Buffer.from(`<svg width="${w}" height="${h}"><rect width="${w}" height="${h}" rx="${r}" fill="#fff"/></svg>`), blend: "dest-in" }])
    .png()
    .toBuffer();
  const pad = 10;
  const frame = Buffer.from(`<svg width="${w + pad * 2}" height="${h + pad * 2}">
    <rect width="${w + pad * 2}" height="${h + pad * 2}" rx="${r + pad}" fill="#0d1a17" stroke="#2a4740" stroke-width="2"/>
  </svg>`);
  return sharp(frame)
    .composite([{ input: shot, top: pad, left: pad }])
    .png()
    .toBuffer();
}

const device = await phone();
const rotated = await sharp(device)
  .rotate(-6, { background: { r: 0, g: 0, b: 0, alpha: 0 } })
  .png()
  .toBuffer();
const meta = await sharp(rotated).metadata();

for (const t of Object.values(locales)) {
  await sharp(Buffer.from(background(t)))
    .composite([{ input: rotated, left: W - meta.width - 40, top: Math.round((H - meta.height) / 2) + 60 }])
    .png({ compressionLevel: 9 })
    .toFile(`${root}${t.out}`);
  console.log(`wrote ${t.out}`);
}
