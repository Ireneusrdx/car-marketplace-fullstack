import api from "@/lib/api";

function formatCompact(value) {
  const numeric = Number(value || 0);
  if (!Number.isFinite(numeric) || numeric <= 0) return "0";
  if (numeric >= 1000000) return `${(numeric / 1000000).toFixed(1)}M+`;
  if (numeric >= 1000) return `${Math.round(numeric / 1000)}K+`;
  return `${numeric}+`;
}

export async function fetchHomeMakes(limit = 8) {
  const response = await api.get("/cars/makes");
  const items = Array.isArray(response.data) ? response.data : [];

  return items.slice(0, limit).map((item) => ({
    id: String(item.id || item.name),
    name: item.name || "Brand",
    logoUrl: item.logoUrl || "",
    count: "Available"
  }));
}

export async function fetchHomeStats() {
  const response = await api.get("/listings", {
    params: { page: 0, size: 1, sort: "newest" }
  });

  const totalElements = Number(response.data?.totalElements || 0);

  return {
    totalListingsLabel: formatCompact(totalElements)
  };
}

