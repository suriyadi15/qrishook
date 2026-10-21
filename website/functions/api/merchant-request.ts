interface Env {
  GITHUB_TOKEN: string;
  GITHUB_REPO: string;
}

type PagesFunction = (context: { request: Request; env: Env }) => Response | Promise<Response>;

// Cache API is per data center and not atomic, so this is best-effort spam
// protection that needs no paid plan or extra bindings.
declare const caches: { default: Cache };

interface RequestBody {
  merchantAppName?: unknown;
  packageName?: unknown;
  expectedDetails?: unknown;
  contact?: unknown;
  debugPayload?: unknown;
  appVersion?: unknown;
  androidVersion?: unknown;
}

const MAX_BODY_BYTES = 100 * 1024;
const MAX_SHORT = 200;
const MAX_LONG = 2000;
const RATE_LIMIT = 5;
const RATE_WINDOW_SECONDS = 60 * 60;

const rateLimited = async (request: Request): Promise<boolean> => {
  const ip = request.headers.get("CF-Connecting-IP") ?? "unknown";
  const key = new Request(`https://rate-limit.internal/merchant-request/${encodeURIComponent(ip)}`);
  const cache = caches.default;

  const hit = await cache.match(key);
  const state = hit ? ((await hit.json()) as { count: number; resetAt: number }) : null;
  const now = Date.now();
  const resetAt = state && state.resetAt > now ? state.resetAt : now + RATE_WINDOW_SECONDS * 1000;
  const count = state && state.resetAt > now ? state.count : 0;

  if (count >= RATE_LIMIT) return true;

  await cache.put(
    key,
    new Response(JSON.stringify({ count: count + 1, resetAt }), {
      headers: { "Cache-Control": `max-age=${Math.ceil((resetAt - now) / 1000)}` },
    }),
  );
  return false;
};

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });

const text = (value: unknown, max: number, required: boolean): string | null => {
  if (value === undefined || value === null || value === "") return required ? null : "";
  if (typeof value !== "string") return null;
  const trimmed = value.trim();
  if (required && !trimmed) return null;
  return trimmed.length <= max ? trimmed : null;
};

const row = (label: string, value: string) => `**${label}:** ${value || "-"}`;

export const onRequestPost: PagesFunction = async ({ request, env }) => {
  if (!request.headers.get("X-QrisHook-Client")?.startsWith("android/")) {
    return json({ error: "Invalid client" }, 400);
  }

  if (await rateLimited(request)) {
    return json({ error: "Too many requests. Please try again later." }, 429);
  }

  const raw = await request.text();
  if (new TextEncoder().encode(raw).length > MAX_BODY_BYTES) {
    return json({ error: "Payload too large" }, 400);
  }

  let data: RequestBody;
  try {
    data = JSON.parse(raw);
  } catch {
    return json({ error: "Invalid JSON" }, 400);
  }

  const merchantAppName = text(data.merchantAppName, MAX_SHORT, true);
  const packageName = text(data.packageName, MAX_SHORT, true);
  const appVersion = text(data.appVersion, MAX_SHORT, true);
  const androidVersion = text(data.androidVersion, MAX_SHORT, true);
  const expectedDetails = text(data.expectedDetails, MAX_LONG, false);
  const contact = text(data.contact, MAX_SHORT, false);
  const debugPayload = text(data.debugPayload, MAX_BODY_BYTES, true);

  if (
    merchantAppName === null || packageName === null || appVersion === null ||
    androidVersion === null || expectedDetails === null || contact === null ||
    debugPayload === null
  ) {
    return json({ error: "Invalid fields" }, 400);
  }

  try {
    JSON.parse(debugPayload);
  } catch {
    return json({ error: "debugPayload must be valid JSON" }, 400);
  }

  if (!env.GITHUB_TOKEN || !env.GITHUB_REPO) {
    return json({ error: "Server is not configured" }, 500);
  }

  const body = [
    row("Merchant app", merchantAppName),
    row("Package", packageName),
    row("QRIS Hook version", appVersion),
    row("Android", androidVersion),
    row("Contact", contact),
    "",
    "### Expected payment details",
    expectedDetails || "-",
    "",
    "### Debug payload",
    "```json",
    debugPayload.replaceAll("```", "'''"),
    "```",
  ].join("\n");

  const res = await fetch(`https://api.github.com/repos/${env.GITHUB_REPO}/issues`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${env.GITHUB_TOKEN}`,
      Accept: "application/vnd.github+json",
      "Content-Type": "application/json",
      "User-Agent": "qrishook-merchant-request",
      "X-GitHub-Api-Version": "2022-11-28",
    },
    body: JSON.stringify({
      title: `[Merchant Parser] ${merchantAppName}`,
      body,
      labels: ["merchant-parser", "needs-triage"],
    }),
  });

  if (!res.ok) {
    return json({ error: "Failed to create request" }, 502);
  }

  const issue = (await res.json()) as { number: number };
  return json({ id: issue.number });
};

export const onRequest: PagesFunction = async () =>
  json({ error: "Method not allowed" }, 405);
