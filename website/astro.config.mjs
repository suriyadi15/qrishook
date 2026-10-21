import { defineConfig } from "astro/config";
import starlight from "@astrojs/starlight";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  site: "https://qrishook.pages.dev",
  integrations: [
    starlight({
      title: "QRIS Hook",
      description: "Android app that turns QRIS payment notifications into webhooks.",
      favicon: "/favicon.svg",
      defaultLocale: "root",
      locales: {
        root: { label: "Bahasa Indonesia", lang: "id" },
        en: { label: "English", lang: "en" },
      },
      social: [
        { icon: "github", label: "GitHub", href: "https://github.com/suriyadi15/qrishook" },
      ],
      customCss: ["./src/styles/global.css"],
      sidebar: [
        {
          label: "Mulai",
          translations: { en: "Getting started" },
          items: [
            { slug: "docs", label: "Pengantar", translations: { en: "Introduction" } },
            { slug: "docs/setup", label: "Instalasi & setup", translations: { en: "Install & setup" } },
            { slug: "docs/modes", label: "Mode Active & Debug", translations: { en: "Active & Debug mode" } },
          ],
        },
        {
          label: "Integrasi",
          translations: { en: "Integration" },
          items: [
            { slug: "docs/webhook", label: "Webhook payload", translations: { en: "Webhook payload" } },
            { slug: "docs/delivery", label: "Pengiriman & retry", translations: { en: "Delivery & retry" } },
          ],
        },
        {
          label: "Merchant",
          items: [
            { slug: "docs/merchants", label: "Merchant didukung", translations: { en: "Supported merchants" } },
            { slug: "docs/request-parser", label: "Ajukan dukungan merchant", translations: { en: "Request merchant support" } },
          ],
        },
        {
          label: "Kontribusi",
          translations: { en: "Contributing" },
          items: [
            { slug: "docs/add-parser", label: "Menambah parser", translations: { en: "Adding a parser" } },
            { slug: "docs/build", label: "Build & rilis", translations: { en: "Build & release" } },
            { slug: "docs/privacy", label: "Kebijakan privasi", translations: { en: "Privacy policy" } },
            { slug: "docs/disclaimer", label: "Disclaimer & lisensi", translations: { en: "Disclaimer & license" } },
          ],
        },
      ],
    }),
  ],
  vite: { plugins: [tailwindcss()] },
});
