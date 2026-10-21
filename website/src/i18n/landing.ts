export type Locale = "id" | "en";

export const REPO = "https://github.com/suriyadi15/qrishook";
export const RELEASES = `${REPO}/releases/latest`;

export const merchants = [
  { name: "BRI Merchant", pkg: "id.co.bri.merchant" },
  { name: "GoPay Merchant", pkg: "com.gojek.gopaymerchant" },
  { name: "InterActive QRIS", pkg: "com.interactive.qrisid" },
];

export const payload = `{
  "event_id": "uuid",
  "type": "qris.payment.success",
  "merchant_id": "demo_merchant",
  "source_app": "Demo Merchant",
  "received_at": "2030-05-21T12:00:00+07:00",
  "payment": {
    "amount": 10000,
    "currency": "IDR",
    "sender_name": "John Doe",
    "payment_source": "Demo"
  },
  "raw": { "title": "string", "text": "string" }
}`;

const id = {
  lang: "id",
  docsBase: "/docs/",
  home: "/",
  other: { label: "EN", href: "/en/" },
  title: "QRIS Hook — Notifikasi QRIS jadi webhook",
  description:
    "Aplikasi Android yang membaca notifikasi pembayaran QRIS dari aplikasi merchant dan mengirimnya ke webhook milik Anda.",
  nav: { features: "Fitur", how: "Cara kerja", merchants: "Merchant", docs: "Dokumentasi" },
  hero: {
    badge: "Open source · MIT",
    h1a: "Ubah notifikasi QRIS",
    h1b: "jadi webhook.",
    sub: "QRIS Hook membaca notifikasi pembayaran dari aplikasi merchant di Android, lalu mengirimnya sebagai JSON ke backend Anda. Tanpa API pihak ketiga.",
    download: "Download APK",
    docs: "Baca dokumentasi",
  },
  featuresTitle: "Dibuat untuk otomasi pembayaran",
  features: [
    ["Pembaca notifikasi", "Memakai NotificationListenerService untuk menangkap notifikasi pembayaran QRIS secara real-time."],
    ["Webhook agnostik", "POST JSON ke URL apa pun, dengan header X-Webhook-Secret opsional untuk verifikasi."],
    ["Antrean & retry", "Event disimpan di Room, dikirim langsung, dan dicoba ulang oleh WorkManager dengan exponential backoff."],
    ["Parser per merchant", "Setiap aplikasi merchant punya parser sendiri. Menambah merchant baru cukup satu kelas Kotlin."],
    ["Debug mode", "Tangkap payload mentah notifikasi untuk membuat atau memperbaiki parser."],
    ["Data di tangan Anda", "Tidak ada server perantara. Notifikasi langsung dari ponsel ke webhook Anda."],
  ],
  howTitle: "Cara kerja",
  steps: [
    ["Aktifkan akses notifikasi", "Beri izin QRIS Hook membaca notifikasi di pengaturan Android."],
    ["Isi webhook", "Masukkan URL webhook dan secret opsional."],
    ["Pilih merchant", "Centang aplikasi merchant yang ingin dipantau."],
    ["Terima event", "Pembayaran masuk di-parse, diantre, lalu dikirim ke backend Anda."],
  ],
  payloadTitle: "Payload yang rapi",
  payloadSub: "Setiap pembayaran dikirim sebagai JSON dengan data terformat dan notifikasi mentah.",
  merchantsTitle: "Merchant yang didukung",
  merchantsSub: "Aplikasi Anda belum ada? Kirim payload dari Debug Mode agar aplikasi Anda didukung.",
  requestParser: "Ajukan dukungan merchant",
  requestParserHref: "/docs/request-parser/",
  requestParserHint: "Dikirim langsung dari aplikasi, tanpa akun GitHub.",
  screensTitle: "Tampilan aplikasi",
  ctaTitle: "Siap menerima webhook pembayaran?",
  ctaSub: "Download APK terbaru dari GitHub Releases, atau build sendiri dari source.",
  build: "Lihat source",
  disclaimer:
    "QRIS Hook hanya untuk pemantauan notifikasi yang sah, pada perangkat, akun merchant, dan endpoint yang Anda berhak akses. Anda bertanggung jawab penuh atas penggunaannya. Perangkat lunak disediakan \"apa adanya\" tanpa garansi.",
  disclaimerMore: "Selengkapnya",
  trademark:
    "Proyek independen, tidak berafiliasi dengan Bank Indonesia, ASPI, maupun penyedia aplikasi merchant. QRIS dan nama merchant adalah merek milik pemiliknya masing-masing.",
  privacy: "Kebijakan privasi",
  license: "Dirilis dengan Lisensi MIT.",
} as const;

const en = {
  lang: "en",
  docsBase: "/en/docs/",
  home: "/en/",
  other: { label: "ID", href: "/" },
  title: "QRIS Hook — Turn QRIS notifications into webhooks",
  description:
    "An Android app that watches QRIS payment notifications from merchant apps and sends them to a webhook you control.",
  nav: { features: "Features", how: "How it works", merchants: "Merchants", docs: "Docs" },
  hero: {
    badge: "Open source · MIT",
    h1a: "Turn QRIS notifications",
    h1b: "into webhooks.",
    sub: "QRIS Hook reads payment notifications from merchant apps on Android and delivers them as JSON to your backend. No third-party API involved.",
    download: "Download APK",
    docs: "Read the docs",
  },
  featuresTitle: "Built for payment automation",
  features: [
    ["Notification listener", "Uses NotificationListenerService to catch QRIS payment notifications in real time."],
    ["Backend-agnostic webhook", "JSON POST to any URL, with an optional X-Webhook-Secret header for verification."],
    ["Queue & retry", "Events are stored in Room, sent immediately, and retried by WorkManager with exponential backoff."],
    ["Per-merchant parsers", "Each merchant app has its own parser. Adding one takes a single Kotlin class."],
    ["Debug mode", "Capture raw notification payloads to build or fix a parser."],
    ["Your data stays yours", "No middleman server. Notifications go from the phone straight to your webhook."],
  ],
  howTitle: "How it works",
  steps: [
    ["Enable notification access", "Allow QRIS Hook to read notifications in Android settings."],
    ["Set your webhook", "Enter the webhook URL and an optional secret."],
    ["Pick merchants", "Select the merchant apps you want to monitor."],
    ["Receive events", "Payments are parsed, queued, then delivered to your backend."],
  ],
  payloadTitle: "A clean payload",
  payloadSub: "Every payment is sent as JSON with formatted data plus the raw notification.",
  merchantsTitle: "Supported merchants",
  merchantsSub: "Your app isn't listed? Send a Debug Mode payload to get it supported.",
  requestParser: "Request merchant support",
  requestParserHref: "/en/docs/request-parser/",
  requestParserHint: "Sent straight from the app, no GitHub account needed.",
  screensTitle: "App screenshots",
  ctaTitle: "Ready to receive payment webhooks?",
  ctaSub: "Download the latest APK from GitHub Releases, or build it from source.",
  build: "View source",
  disclaimer:
    "QRIS Hook is for lawful notification monitoring on devices, merchant accounts and endpoints you have the right to access. You are solely responsible for its use. The software is provided \"as is\", without warranty.",
  disclaimerMore: "Read more",
  trademark:
    "Independent project, not affiliated with Bank Indonesia, ASPI, or any merchant app provider. QRIS and merchant names are trademarks of their respective owners.",
  privacy: "Privacy policy",
  license: "Released under the MIT License.",
} as const;

export const dict = { id, en };
