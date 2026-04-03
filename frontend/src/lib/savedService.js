import api from "@/lib/api";
import { mapListingCardDto } from "@/lib/listingsService";

export async function fetchSavedListings() {
  const response = await api.get("/saved");
  const items = Array.isArray(response.data?.items)
    ? response.data.items
    : Array.isArray(response.data)
    ? response.data
    : [];
  return items.map(mapListingCardDto);
}

export async function saveListing(listingId) {
  await api.post(`/saved/${listingId}`);
}

export async function unsaveListing(listingId) {
  await api.delete(`/saved/${listingId}`);
}

export async function checkSaved(listingId) {
  const response = await api.get(`/saved/check/${listingId}`);
  return Boolean(response.data?.saved ?? response.data);
}
