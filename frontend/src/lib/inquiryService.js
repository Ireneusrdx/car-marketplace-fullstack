import api from "@/lib/api";

function mapInquiryDto(dto) {
  return {
    id: dto?.id,
    listingId: dto?.listingId,
    listingTitle: dto?.listingTitle || "Vehicle",
    message: dto?.message || "",
    status: dto?.status || "OPEN",
    read: Boolean(dto?.read),
    createdAt: dto?.createdAt || null,
    senderName: dto?.senderName || "User",
    replies: Array.isArray(dto?.replies) ? dto.replies : []
  };
}

export async function sendInquiry(payload) {
  // payload: { listingId, message }
  const response = await api.post("/inquiries", payload);
  return mapInquiryDto(response.data);
}

export async function fetchSentInquiries(page = 0, size = 10) {
  const response = await api.get("/inquiries/sent", { params: { page, size } });
  const items = Array.isArray(response.data?.items)
    ? response.data.items
    : Array.isArray(response.data)
    ? response.data
    : [];
  return items.map(mapInquiryDto);
}

export async function fetchReceivedInquiries(page = 0, size = 10) {
  const response = await api.get("/inquiries/received", { params: { page, size } });
  const items = Array.isArray(response.data?.items)
    ? response.data.items
    : Array.isArray(response.data)
    ? response.data
    : [];
  return items.map(mapInquiryDto);
}

export async function replyToInquiry(inquiryId, message) {
  const response = await api.post(`/inquiries/${inquiryId}/reply`, { message });
  return response.data;
}

export async function markInquiryRead(inquiryId) {
  await api.put(`/inquiries/${inquiryId}/read`);
}
