import api from "@/lib/api";

function formatPrice(value) {
  const numeric = Number(value || 0);
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0
  }).format(numeric);
}

function toSlug(text) {
  return String(text || "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "");
}

export function mapListingCardDto(dto) {
  const title = dto?.title || [dto?.year, dto?.make, dto?.model, dto?.variant].filter(Boolean).join(" ") || "Vehicle";
  const priceValue = Number(dto?.price || 0);
  const mileageValue = Number(dto?.mileage || 0);
  const fuel = dto?.fuelType || "Unknown";
  const location = [dto?.locationCity, dto?.locationState].filter(Boolean).join(", ") || "Unknown";

  return {
    id: String(dto?.id || toSlug(title) || Math.random()),
    slug: dto?.slug || toSlug(title),
    title,
    year: dto?.year || null,
    price: formatPrice(priceValue),
    priceValue,
    originalPrice: dto?.negotiable ? "Negotiable" : "",
    mileage: `${mileageValue.toLocaleString()} km`,
    mileageValue,
    fuel,
    transmission: dto?.transmission || "Unknown",
    bodyType: String(dto?.bodyType || "sedan").toLowerCase(),
    location,
    listedAgo: "Recently listed",
    listedDays: 0,
    image: dto?.primaryImageUrl || "https://images.unsplash.com/photo-1492144534655-ae79c964c9d7?auto=format&fit=crop&w=1400&q=80",
    featured: Boolean(dto?.featured),
    isNew: false,
    certified: Boolean(dto?.verified),
    photos: 1,
    seller: "Verified Seller",
    verifiedSeller: Boolean(dto?.verified),
    negotiable: Boolean(dto?.negotiable)
  };
}

export function mapListingDetailDto(dto) {
  const cardShape = mapListingCardDto(dto);
  const images = Array.isArray(dto?.images) && dto.images.length ? dto.images : [cardShape.image];

  return {
    ...cardShape,
    description: dto?.description || "",
    features: Array.isArray(dto?.features) ? dto.features : [],
    condition: dto?.condition || (cardShape.certified ? "Certified" : "Used"),
    viewCount: dto?.viewCount || 0,
    createdAt: dto?.createdAt || null,
    locationCity: dto?.locationCity || "",
    locationState: dto?.locationState || "",
    driveType: dto?.driveType || "",
    seats: dto?.seats || null,
    color: dto?.color || "",
    ownershipCount: dto?.ownershipCount || null,
    insuranceValid: Boolean(dto?.insuranceValid),
    registrationState: dto?.registrationState || "",
    images
  };
}

function toBackendSort(sortBy) {
  switch (sortBy) {
    case "priceAsc":
      return "price_asc";
    case "priceDesc":
      return "price_desc";
    case "mileageAsc":
      return "mileage_asc";
    case "recent":
    default:
      return "newest";
  }
}

function toSearchParams(filters = {}) {
  const params = {
    sort: toBackendSort(filters.sortBy),
    page: 0,
    size: 24
  };

  if (filters.query) params.q = filters.query;
  if (filters.bodyType && filters.bodyType !== "all") params.bodyType = filters.bodyType;
  if (filters.fuelType && filters.fuelType !== "all") params.fuelType = filters.fuelType;
  if (filters.minPrice != null) params.priceMin = filters.minPrice;
  if (filters.maxPrice != null) params.priceMax = filters.maxPrice;

  return params;
}

export async function fetchListings(filters = {}) {
  const response = await api.get("/listings", { params: toSearchParams(filters) });
  const items = Array.isArray(response.data?.items) ? response.data.items : [];
  return items.map(mapListingCardDto);
}

export async function fetchListingBySlug(slug) {
  const response = await api.get(`/listings/${slug}`);
  return mapListingDetailDto(response.data);
}

export async function fetchFeaturedListings(size = 6) {
  const response = await api.get("/listings/featured", { params: { size } });
  const items = Array.isArray(response.data) ? response.data : [];
  return items.map(mapListingCardDto);
}

export async function fetchRecentListings(size = 6) {
  const response = await api.get("/listings/recent", { params: { size } });
  const items = Array.isArray(response.data) ? response.data : [];
  return items.map(mapListingCardDto);
}

export async function fetchMyListings(page = 0, size = 12) {
  const response = await api.get("/listings/my-listings", { params: { page, size } });
  const items = Array.isArray(response.data?.items) ? response.data.items : [];
  return items.map(mapListingCardDto);
}

export async function deleteListing(id) {
  await api.delete(`/listings/${id}`);
}

export async function aiRecommend(payload) {
  // payload: { budget: { min, max }, fuelType: [], bodyType: [], transmission, usage, location }
  const response = await api.post("/ai/recommend", payload);
  return {
    summary: response.data?.summary || "",
    items: Array.isArray(response.data?.items) ? response.data.items : []
  };
}

export async function aiSimilar(listingId) {
  const response = await api.get(`/ai/similar/${listingId}`);
  return {
    summary: response.data?.summary || "",
    items: Array.isArray(response.data?.items) ? response.data.items : []
  };
}

export async function updateListing(id, payload) {
  const response = await api.put(`/listings/${id}`, payload);
  return response.data;
}

export async function markListingSold(id) {
  const response = await api.put(`/listings/${id}/mark-sold`);
  return response.data;
}


