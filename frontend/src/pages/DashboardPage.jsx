import {
  Bookmark,
  Calendar,
  CheckCircle,
  Inbox,
  LayoutDashboard,
  MessageSquare,
  Pencil,
  Plus,
  Send,
  Trash2
} from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { Helmet } from "react-helmet-async";
import { Link, useNavigate } from "react-router-dom";
import Button from "@/components/global/Button";
import PageTransition from "@/components/global/PageTransition";
import Skeleton from "@/components/global/Skeleton";
import { fetchMyBookings, fetchMyReceivedBookings, confirmBooking, cancelBooking } from "@/lib/bookingService";
import { fetchSentInquiries, fetchReceivedInquiries, replyToInquiry, markInquiryRead } from "@/lib/inquiryService";
import { deleteListing, fetchMyListings, markListingSold } from "@/lib/listingsService";
import { fetchSavedListings } from "@/lib/savedService";
import { useAuthStore } from "@/store/authStore";

const tabs = [
  { key: "listings", label: "My Listings", icon: LayoutDashboard },
  { key: "saved", label: "Saved Cars", icon: Bookmark },
  { key: "bookings", label: "My Bookings", icon: Calendar },
  { key: "received-bookings", label: "Received Bookings", icon: Inbox },
  { key: "inquiries", label: "Sent Inquiries", icon: Send },
  { key: "received-inquiries", label: "Received Inquiries", icon: MessageSquare }
];

function getInitials(name) {
  if (!name) return "??";
  return name.split(" ").map((n) => n[0]).join("").slice(0, 2).toUpperCase();
}

function ReceivedInquiriesTab({ inquiries, onReply, onMarkRead, note }) {
  const [replyingTo, setReplyingTo] = useState(null);
  const [replyText, setReplyText] = useState("");

  const handleReply = async (id) => {
    if (!replyText.trim()) return;
    await onReply(id, replyText.trim());
    setReplyText("");
    setReplyingTo(null);
  };

  return (
    <div className="space-y-3">
      {inquiries.length === 0 && !note && (
        <div className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-10 text-center">
          <p className="font-semibold text-gray-900">No inquiries received yet</p>
          <p className="mt-2 text-sm text-gray-500">Buyer inquiries about your listings will appear here.</p>
        </div>
      )}
      {inquiries.map((inq) => (
        <article key={inq.id} className="rounded-2xl border border-gray-100 bg-white p-4">
          <div className="flex items-start justify-between gap-2">
            <div>
              <p className="text-sm font-semibold text-gray-900">{inq.listingTitle}</p>
              <p className="mt-1 text-xs text-gray-500">From: {inq.senderName}</p>
              <p className="mt-1 text-sm text-gray-600 line-clamp-2">{inq.message}</p>
              <p className="mt-2 text-xs text-gray-400">
                {inq.createdAt ? new Date(inq.createdAt).toLocaleDateString() : ""} • {inq.status}
              </p>
            </div>
            <div className="flex gap-1">
              {!inq.read && (
                <Button size="sm" variant="ghost" onClick={() => onMarkRead(inq.id)}>Mark read</Button>
              )}
              <Button size="sm" variant="ghost" onClick={() => setReplyingTo(replyingTo === inq.id ? null : inq.id)}>Reply</Button>
            </div>
          </div>
          {replyingTo === inq.id && (
            <div className="mt-3 flex gap-2">
              <input
                value={replyText}
                onChange={(e) => setReplyText(e.target.value)}
                placeholder="Type your reply..."
                className="flex-1 rounded-lg border border-gray-200 px-3 py-2 text-sm outline-none focus:border-blue"
              />
              <Button size="sm" onClick={() => handleReply(inq.id)}>Send</Button>
            </div>
          )}
        </article>
      ))}
    </div>
  );
}

export default function DashboardPage() {
  const [activeTab, setActiveTab] = useState("listings");
  const [loading, setLoading] = useState(true);
  const [myListings, setMyListings] = useState([]);
  const [savedCars, setSavedCars] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [receivedBookings, setReceivedBookings] = useState([]);
  const [inquiries, setInquiries] = useState([]);
  const [receivedInquiries, setReceivedInquiries] = useState([]);
  const [note, setNote] = useState("");
  const { isAuthenticated, user } = useAuthStore();

  const loadTab = useCallback(async (tab) => {
    setLoading(true);
    setNote("");
    try {
      if (tab === "listings") {
        if (!isAuthenticated) {
          setMyListings([]);
          setNote("Sign in to load your real listings.");
          return;
        }
        const data = await fetchMyListings(0, 12);
        setMyListings(data);
        if (!data.length) setNote("No listings found in your account yet.");
      } else if (tab === "saved") {
        if (!isAuthenticated) { setSavedCars([]); setNote("Sign in to see your saved cars."); return; }
        const data = await fetchSavedListings();
        setSavedCars(data);
        if (!data.length) setNote("You haven't saved any listings yet.");
      } else if (tab === "bookings") {
        if (!isAuthenticated) { setBookings([]); setNote("Sign in to see your bookings."); return; }
        const data = await fetchMyBookings(0, 10);
        setBookings(data);
        if (!data.length) setNote("No bookings found yet.");
      } else if (tab === "received-bookings") {
        if (!isAuthenticated) { setReceivedBookings([]); setNote("Sign in to see received bookings."); return; }
        const data = await fetchMyReceivedBookings(0, 10);
        setReceivedBookings(data);
        if (!data.length) setNote("No bookings received yet.");
      } else if (tab === "inquiries") {
        if (!isAuthenticated) { setInquiries([]); setNote("Sign in to see your inquiries."); return; }
        const data = await fetchSentInquiries(0, 10);
        setInquiries(data);
        if (!data.length) setNote("No inquiries sent yet.");
      } else if (tab === "received-inquiries") {
        if (!isAuthenticated) { setReceivedInquiries([]); setNote("Sign in to see received inquiries."); return; }
        const data = await fetchReceivedInquiries(0, 10);
        setReceivedInquiries(data);
        if (!data.length) setNote("No inquiries received yet.");
      }
    } catch {
      setNote("Could not load data. Please ensure the backend is running.");
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    loadTab(activeTab);
  }, [activeTab, loadTab]);

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this listing?")) return;
    try {
      await deleteListing(id);
      setMyListings((prev) => prev.filter((car) => car.id !== id));
    } catch {
      alert("Failed to delete listing.");
    }
  };

  const navigate = useNavigate();

  const handleMarkSold = async (id) => {
    if (!window.confirm("Mark this listing as sold?")) return;
    try {
      await markListingSold(id);
      setMyListings((prev) => prev.filter((car) => car.id !== id));
    } catch {
      alert("Failed to mark listing as sold.");
    }
  };

  const displayName = user?.fullName || user?.email || "My Account";
  const initials = getInitials(user?.fullName || user?.email);
  const subtitle = [
    user?.verifiedSeller ? "Verified Seller" : null,
    `${myListings.length} Listing${myListings.length !== 1 ? "s" : ""}`,
    user?.role ? user.role.charAt(0) + user.role.slice(1).toLowerCase() : null
  ].filter(Boolean).join(" • ");

  return (
    <PageTransition>
      <Helmet>
        <title>Dashboard | AutoVault</title>
        <meta name="description" content="Manage your listings, saved cars, bookings, and inquiries in your AutoVault dashboard." />
      </Helmet>
      <main className="min-h-screen bg-[var(--white-soft)] px-6 py-10">
        <section className="mx-auto w-full max-w-content">
          <div className="overflow-hidden rounded-3xl border border-gray-100 bg-white shadow-blue-sm">
            {/* Profile Header */}
            <div className="bg-gradient-to-r from-blue to-blue-dark px-8 py-8 text-white">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex items-center gap-4">
                  <div className="flex h-16 w-16 items-center justify-center rounded-full bg-white/20 text-xl font-bold">
                    {initials}
                  </div>
                  <div>
                    <h1 className="font-montserrat text-2xl font-black">{displayName}</h1>
                    <p className="text-sm text-white/80">{subtitle}</p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button variant="secondary" size="sm" className="bg-white text-gray-900">
                    Edit Profile
                  </Button>
                  <Button as={Link} to="/sell" size="sm">
                    <Plus size={14} strokeWidth={1.5} /> Post New
                  </Button>
                </div>
              </div>
            </div>

            {/* Tabs */}
            <div className="border-b border-gray-100 bg-white px-6 py-4">
              <div className="flex flex-wrap gap-2">
                {tabs.map((tab) => {
                  const Icon = tab.icon;
                  const active = activeTab === tab.key;
                  return (
                    <button
                      key={tab.key}
                      onClick={() => setActiveTab(tab.key)}
                      className={
                        active
                          ? "inline-flex items-center gap-2 rounded-full border border-blue bg-blue/5 px-4 py-2 text-sm font-semibold text-blue"
                          : "inline-flex items-center gap-2 rounded-full border border-gray-200 bg-gray-50 px-4 py-2 text-sm text-gray-600"
                      }
                    >
                      <Icon size={14} strokeWidth={1.5} />
                      {tab.label}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Tab Content */}
            <div className="p-6">
              {loading ? (
                <div className="space-y-3">
                  <Skeleton className="h-20" />
                  <Skeleton className="h-20" />
                  <Skeleton className="h-20" />
                </div>
              ) : (
                <>
                  {note && (
                    <p className="mb-4 rounded-xl border border-blue/20 bg-blue/5 px-3 py-2 text-xs font-medium text-blue">
                      {note}
                    </p>
                  )}

                  {/* My Listings */}
                  {activeTab === "listings" && (
                    <div className="space-y-3">
                      {myListings.length === 0 && !note && (
                        <div className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-10 text-center">
                          <p className="font-semibold text-gray-900">No listings yet</p>
                          <p className="mt-1 text-sm text-gray-500">Post your first car to get started.</p>
                          <Button as={Link} to="/sell" className="mt-4" size="sm">Post a Listing</Button>
                        </div>
                      )}
                      {myListings.map((car) => (
                        <article key={car.id} className="flex flex-wrap items-center gap-3 rounded-2xl border border-gray-100 bg-white p-3">
                          <img src={car.image} alt={car.title} className="h-16 w-24 rounded-xl object-cover" />
                          <div className="min-w-0 flex-1">
                            <p className="truncate text-sm font-semibold text-gray-900">{car.title}</p>
                            <p className="text-xs text-gray-500">{car.location} • {car.listedAgo}</p>
                          </div>
                          <p className="font-mono text-lg font-bold text-blue">{car.price}</p>
                          <Button variant="icon" title="Edit" onClick={() => navigate(`/sell?edit=${car.id}`)}>
                            <Pencil size={14} strokeWidth={1.5} />
                          </Button>
                          <Button variant="icon" title="Mark Sold" onClick={() => handleMarkSold(car.id)}>
                            <CheckCircle size={14} strokeWidth={1.5} />
                          </Button>
                          <Button variant="icon" title="Delete" onClick={() => handleDelete(car.id)}>
                            <Trash2 size={14} strokeWidth={1.5} />
                          </Button>
                        </article>
                      ))}
                    </div>
                  )}

                  {/* Saved Cars */}
                  {activeTab === "saved" && (
                    <div className="grid gap-3 md:grid-cols-2">
                      {savedCars.length === 0 && !note && (
                        <div className="col-span-2 rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-10 text-center">
                          <p className="font-semibold text-gray-900">No saved cars yet</p>
                          <Button as={Link} to="/listings" className="mt-4" size="sm">Browse Listings</Button>
                        </div>
                      )}
                      {savedCars.map((car) => (
                        <article key={car.id} className="rounded-2xl border border-gray-100 bg-white p-3">
                          <img src={car.image} alt={car.title} className="h-36 w-full rounded-xl object-cover" />
                          <p className="mt-2 text-sm font-semibold text-gray-900">{car.title}</p>
                          <p className="mt-1 font-mono text-blue">{car.price}</p>
                          <Button as={Link} to={`/cars/${car.slug}`} size="sm" variant="ghost" className="mt-2">
                            View Details
                          </Button>
                        </article>
                      ))}
                    </div>
                  )}

                  {/* My Bookings */}
                  {activeTab === "bookings" && (
                    <div className="space-y-3">
                      {bookings.length === 0 && !note && (
                        <div className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-10 text-center">
                          <p className="font-semibold text-gray-900">No bookings yet</p>
                          <p className="mt-1 text-sm text-gray-500">Book a test drive from any listing page.</p>
                        </div>
                      )}
                      {bookings.map((booking) => (
                        <article key={booking.id} className="rounded-2xl border border-gray-100 bg-white p-4">
                          <div className="flex flex-wrap items-center justify-between gap-2">
                            <div>
                              <p className="text-sm font-semibold text-gray-900">
                                {booking.type === "TEST_DRIVE" ? "Test Drive" : "Purchase"} — {booking.listingTitle}
                              </p>
                              <p className="mt-1 text-xs text-gray-500">
                                #{booking.bookingNumber}
                                {booking.scheduledDate ? ` • ${new Date(booking.scheduledDate).toLocaleDateString()}` : ""}
                              </p>
                            </div>
                            <div className="flex items-center gap-2">
                              <span className={`rounded-full px-3 py-1 text-xs font-bold ${
                                booking.status === "CONFIRMED" ? "bg-emerald-100 text-emerald-700" :
                                booking.status === "CANCELLED" ? "bg-red-100 text-red-700" :
                                "bg-amber-100 text-amber-700"
                              }`}>
                                {booking.status}
                              </span>
                              {booking.status === "PENDING" && (
                                <Button size="sm" variant="ghost" onClick={async () => {
                                  try { await cancelBooking(booking.id); loadTab("bookings"); } catch { /* ignore */ }
                                }}>Cancel</Button>
                              )}
                            </div>
                          </div>
                        </article>
                      ))}
                    </div>
                  )}

                  {/* Received Bookings */}
                  {activeTab === "received-bookings" && (
                    <div className="space-y-3">
                      {receivedBookings.length === 0 && !note && (
                        <div className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-10 text-center">
                          <p className="font-semibold text-gray-900">No received bookings yet</p>
                          <p className="mt-1 text-sm text-gray-500">Bookings for your listings will appear here.</p>
                        </div>
                      )}
                      {receivedBookings.map((booking) => (
                        <article key={booking.id} className="rounded-2xl border border-gray-100 bg-white p-4">
                          <div className="flex flex-wrap items-center justify-between gap-2">
                            <div>
                              <p className="text-sm font-semibold text-gray-900">
                                {booking.type === "TEST_DRIVE" ? "Test Drive" : "Purchase"} — {booking.listingTitle}
                              </p>
                              <p className="mt-1 text-xs text-gray-500">
                                #{booking.bookingNumber} • From: {booking.buyerName}
                                {booking.scheduledDate ? ` • ${new Date(booking.scheduledDate).toLocaleDateString()}` : ""}
                              </p>
                            </div>
                            <div className="flex items-center gap-2">
                              <span className={`rounded-full px-3 py-1 text-xs font-bold ${
                                booking.status === "CONFIRMED" ? "bg-emerald-100 text-emerald-700" :
                                booking.status === "CANCELLED" ? "bg-red-100 text-red-700" :
                                "bg-amber-100 text-amber-700"
                              }`}>
                                {booking.status}
                              </span>
                              {booking.status === "PENDING" && (
                                <>
                                  <Button size="sm" onClick={async () => {
                                    try { await confirmBooking(booking.id); loadTab("received-bookings"); } catch { /* ignore */ }
                                  }}>Confirm</Button>
                                  <Button size="sm" variant="ghost" onClick={async () => {
                                    try { await cancelBooking(booking.id); loadTab("received-bookings"); } catch { /* ignore */ }
                                  }}>Decline</Button>
                                </>
                              )}
                            </div>
                          </div>
                        </article>
                      ))}
                    </div>
                  )}

                  {/* Sent Inquiries */}
                  {activeTab === "inquiries" && (
                    <div className="space-y-3">
                      {inquiries.length === 0 && !note && (
                        <div className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-10 text-center">
                          <p className="font-semibold text-gray-900">No inquiries sent yet</p>
                          <p className="mt-2 text-sm text-gray-500">Send inquiries from listing detail pages.</p>
                        </div>
                      )}
                      {inquiries.map((inq) => (
                        <article key={inq.id} className="rounded-2xl border border-gray-100 bg-white p-4">
                          <p className="text-sm font-semibold text-gray-900">{inq.listingTitle}</p>
                          <p className="mt-1 text-sm text-gray-600 line-clamp-2">{inq.message}</p>
                          <p className="mt-2 text-xs text-gray-400">
                            {inq.createdAt ? new Date(inq.createdAt).toLocaleDateString() : ""} • {inq.status}
                          </p>
                        </article>
                      ))}
                    </div>
                  )}

                  {/* Received Inquiries */}
                  {activeTab === "received-inquiries" && (
                    <ReceivedInquiriesTab inquiries={receivedInquiries} onReply={async (id, msg) => {
                      await replyToInquiry(id, msg);
                      loadTab("received-inquiries");
                    }} onMarkRead={async (id) => {
                      await markInquiryRead(id);
                      loadTab("received-inquiries");
                    }} note={note} />
                  )}
                </>
              )}
            </div>
          </div>
        </section>
      </main>
    </PageTransition>
  );
}
