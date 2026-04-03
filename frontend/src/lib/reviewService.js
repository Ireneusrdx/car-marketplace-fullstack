import api from "@/lib/api";

function mapReviewDto(dto) {
  return {
    id: dto?.id,
    rating: dto?.rating || 0,
    comment: dto?.comment || "",
    reviewerName: dto?.reviewerName || "Anonymous",
    createdAt: dto?.createdAt || null,
  };
}

export async function createSellerReview(sellerId, payload) {
  const response = await api.post(`/reviews/seller/${sellerId}`, payload);
  return mapReviewDto(response.data);
}

export async function fetchSellerReviews(sellerId, page = 0, size = 10) {
  const response = await api.get(`/reviews/seller/${sellerId}`, { params: { page, size } });
  const items = Array.isArray(response.data?.items) ? response.data.items : [];
  return items.map(mapReviewDto);
}
