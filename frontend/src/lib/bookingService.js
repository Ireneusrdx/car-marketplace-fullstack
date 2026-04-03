import api from "@/lib/api";

function mapBookingDto(dto) {
  return {
    id: dto?.id,
    bookingNumber: dto?.bookingNumber || "",
    listingId: dto?.listingId,
    listingTitle: dto?.listingTitle || "Vehicle",
    listingImage: dto?.listingImage || null,
    type: dto?.type || "TEST_DRIVE",
    status: dto?.status || "PENDING",
    scheduledDate: dto?.scheduledDate || null,
    totalAmount: dto?.totalAmount || 0,
    depositAmount: dto?.depositAmount || 0,
    stripeClientSecret: dto?.stripeClientSecret || null,
    stripePaymentIntentId: dto?.stripePaymentIntentId || null,
    createdAt: dto?.createdAt || null,
    sellerName: dto?.sellerName || "Seller",
    buyerName: dto?.buyerName || "Buyer"
  };
}

export async function initiateBooking(payload) {
  // payload: { listingId, type, scheduledDate, notes }
  const response = await api.post("/bookings/initiate", payload);
  return mapBookingDto(response.data);
}

export async function fetchMyBookings(page = 0, size = 10) {
  const response = await api.get("/bookings/my-bookings", { params: { page, size } });
  const items = Array.isArray(response.data?.items) ? response.data.items : [];
  return items.map(mapBookingDto);
}

export async function fetchMyReceivedBookings(page = 0, size = 10) {
  const response = await api.get("/bookings/my-received", { params: { page, size } });
  const items = Array.isArray(response.data?.items) ? response.data.items : [];
  return items.map(mapBookingDto);
}

export async function confirmBooking(bookingId) {
  const response = await api.put(`/bookings/${bookingId}/confirm`);
  return response.data;
}

export async function cancelBooking(bookingId) {
  const response = await api.put(`/bookings/${bookingId}/cancel`);
  return response.data;
}
