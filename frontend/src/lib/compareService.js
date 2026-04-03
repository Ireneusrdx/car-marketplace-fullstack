import api from "@/lib/api";

export async function quickCompare(listingIds) {
  const ids = listingIds.join(",");
  const response = await api.get("/compare/quick", { params: { ids } });
  return response.data;
}

export async function saveCompareSession(listingIds) {
  const response = await api.post("/compare", { listingIds });
  return response.data;
}

export async function getCompareSession(sessionId) {
  const response = await api.get(`/compare/${sessionId}`);
  return response.data;
}
