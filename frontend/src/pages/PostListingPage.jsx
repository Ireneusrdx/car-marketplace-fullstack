import { Camera, Check, Crown, MapPin, Plus, Sparkles, Trash2, Upload } from "lucide-react";
import { motion } from "framer-motion";
import { useEffect, useMemo, useState } from "react";
import { Helmet } from "react-helmet-async";
import { useNavigate } from "react-router-dom";
import Button from "@/components/global/Button";
import Input from "@/components/global/Input";
import PageTransition from "@/components/global/PageTransition";
import { notify } from "@/components/global/ToastProvider";
import api from "@/lib/api";

const steps = [
  "Basic Info",
  "Specifications",
  "Condition & Features",
  "Photos",
  "Location & Description",
  "Review & Publish"
];

const popularFeatures = [
  "Sunroof",
  "Leather Seats",
  "Backup Camera",
  "Adaptive Cruise",
  "Apple CarPlay",
  "Blind Spot Assist",
  "360 Camera",
  "Panoramic Sunroof",
  "Premium Audio",
  "Heated Seats",
  "Wireless Charging",
  "Lane Assist"
];

const bodyTypes = ["Sedan", "SUV", "Hatchback", "Coupe", "Truck", "Van", "Wagon", "Convertible"];
const fuelTypes = ["Petrol", "Diesel", "Electric", "Hybrid", "CNG"];
const transmissions = ["Automatic", "Manual"];
const conditions = ["New", "Like New", "Excellent", "Good", "Fair"];

export default function PostListingPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [publishing, setPublishing] = useState(false);
  const [selectedFeatures, setSelectedFeatures] = useState([]);
  const [images, setImages] = useState([]);
  const [primaryImageIndex, setPrimaryImageIndex] = useState(0);

  // Real makes/models from API
  const [makes, setMakes] = useState([]);
  const [models, setModels] = useState([]);

  const [form, setForm] = useState({
    makeId: "",
    modelId: "",
    year: "2024",
    bodyType: "Sedan",
    price: "",
    negotiable: true,
    fuel: "Petrol",
    transmission: "Automatic",
    condition: "Excellent",
    mileage: "",
    city: "",
    state: "",
    description: "",
    variant: "",
    color: "",
    listingDuration: "60",
    listingType: "featured"
  });

  // Fetch makes on mount
  useEffect(() => {
    api.get("/cars/makes")
      .then((res) => {
        const data = Array.isArray(res.data) ? res.data : [];
        setMakes(data);
        if (data.length) setForm((p) => ({ ...p, makeId: data[0].id }));
      })
      .catch(() => setMakes([]));
  }, []);

  // Fetch models when make changes
  useEffect(() => {
    if (!form.makeId) { setModels([]); return; }
    api.get(`/cars/makes/${form.makeId}/models`)
      .then((res) => {
        const data = Array.isArray(res.data) ? res.data : [];
        setModels(data);
        if (data.length) setForm((p) => ({ ...p, modelId: data[0].id }));
        else setForm((p) => ({ ...p, modelId: "" }));
      })
      .catch(() => setModels([]));
  }, [form.makeId]);

  const progress = useMemo(() => (step / steps.length) * 100, [step]);
  const updateForm = (patch) => setForm((prev) => ({ ...prev, ...patch }));

  const onImageUpload = (event) => {
    const files = Array.from(event.target.files || []);
    const next = files.map((file) => ({ file, url: URL.createObjectURL(file) }));
    setImages((prev) => [...prev, ...next].slice(0, 20));
    event.target.value = "";
  };

  const removeImage = (index) => {
    setImages((prev) => prev.filter((_, i) => i !== index));
    if (primaryImageIndex >= index && primaryImageIndex > 0) {
      setPrimaryImageIndex((prev) => prev - 1);
    }
  };

  const toggleFeature = (feature) => {
    setSelectedFeatures((prev) =>
      prev.includes(feature) ? prev.filter((item) => item !== feature) : [...prev, feature]
    );
  };

  const selectedMake = makes.find((m) => m.id === form.makeId);
  const selectedModel = models.find((m) => m.id === form.modelId);
  const previewTitle = `${form.year} ${selectedMake?.name || ""} ${selectedModel?.name || ""} ${form.variant}`.trim();

  const handlePublish = async () => {
    if (!form.makeId || !form.modelId || !form.price) {
      notify.error("Please fill in all required fields (Make, Model, Price).");
      return;
    }

    setPublishing(true);
    try {
      // Step 1: Create listing
      const payload = {
        title: previewTitle || `${form.year} Vehicle`,
        makeId: form.makeId,
        modelId: form.modelId,
        year: parseInt(form.year, 10),
        variant: form.variant || null,
        price: parseFloat(form.price),
        isNegotiable: form.negotiable,
        mileage: form.mileage ? parseInt(form.mileage, 10) : null,
        fuelType: form.fuel.toUpperCase(),
        transmission: form.transmission.toUpperCase(),
        bodyType: form.bodyType.toUpperCase(),
        condition: form.condition.toUpperCase().replace(" ", "_"),
        color: form.color || null,
        description: form.description || null,
        features: selectedFeatures,
        locationCity: form.city || null,
        locationState: form.state || null
      };

      const createRes = await api.post("/listings", payload);
      const { id, slug } = createRes.data;

      // Step 2: Upload images if any
      if (images.length > 0) {
        const formData = new FormData();
        // Put primary image first
        const orderedImages = [...images];
        if (primaryImageIndex > 0) {
          const [primary] = orderedImages.splice(primaryImageIndex, 1);
          orderedImages.unshift(primary);
        }
        orderedImages.forEach((img) => formData.append("files", img.file));
        await api.post(`/listings/${id}/images`, formData, {
          headers: { "Content-Type": "multipart/form-data" }
        });
      }

      notify.success("Listing published successfully!");
      navigate(`/cars/${slug}`);
    } catch (err) {
      const msg = err?.response?.data?.error?.message || err?.response?.data?.message || "Failed to publish listing.";
      notify.error(msg);
    } finally {
      setPublishing(false);
    }
  };

  return (
    <PageTransition>
      <Helmet>
        <title>Post a Listing | AutoVault</title>
        <meta name="description" content="Create and publish your car listing with AutoVault's step-by-step listing wizard." />
      </Helmet>
      <main className="min-h-screen bg-[var(--white-soft)] px-6 py-10">
        <section className="mx-auto w-full max-w-3xl">
          <div className="rounded-3xl border border-gray-100 bg-white p-6 shadow-blue-sm">
            <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
              <h1 className="font-montserrat text-2xl font-black text-gray-900">Post a Listing</h1>
              <span className="text-sm font-semibold text-blue">Step {step} of {steps.length}</span>
            </div>

            <div className="mb-4 h-2 overflow-hidden rounded-full bg-gray-100">
              <div className="h-full bg-blue transition-all duration-300" style={{ width: `${progress}%` }} />
            </div>

            <div className="grid gap-2 sm:grid-cols-3">
              {steps.map((label, index) => {
                const stepNumber = index + 1;
                const isDone = stepNumber < step;
                const isCurrent = stepNumber === step;
                return (
                  <div
                    key={label}
                    className={isCurrent ? "rounded-xl border border-blue bg-blue/5 px-3 py-2 text-xs font-semibold text-blue" : "rounded-xl border border-gray-100 bg-gray-50 px-3 py-2 text-xs text-gray-500"}
                  >
                    {isDone ? <Check size={12} className="mb-1" /> : null}
                    {stepNumber}. {label}
                  </div>
                );
              })}
            </div>
          </div>

          <motion.article
            key={step}
            initial={{ opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.28 }}
            className="mt-6 rounded-3xl border border-gray-100 bg-white p-8 shadow-blue-sm"
          >
            {step === 1 && (
              <div className="space-y-4">
                <h2 className="font-montserrat text-3xl font-bold text-gray-900">Basic Info</h2>
                <div className="grid gap-3 sm:grid-cols-2">
                  <label className="block">
                    <span className="mb-2 block text-sm font-medium text-gray-600">Make *</span>
                    <select value={form.makeId} onChange={(e) => updateForm({ makeId: e.target.value })} className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-blue">
                      {makes.map((item) => (
                        <option key={item.id} value={item.id}>{item.name}</option>
                      ))}
                      {!makes.length && <option disabled>Loading...</option>}
                    </select>
                  </label>
                  <label className="block">
                    <span className="mb-2 block text-sm font-medium text-gray-600">Model *</span>
                    <select value={form.modelId} onChange={(e) => updateForm({ modelId: e.target.value })} className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-blue">
                      {models.map((item) => (
                        <option key={item.id} value={item.id}>{item.name}</option>
                      ))}
                      {!models.length && <option disabled>Select a make first</option>}
                    </select>
                  </label>
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <Input label="Year" value={form.year} onChange={(e) => updateForm({ year: e.target.value })} />
                  <Input label="Variant (optional)" value={form.variant} onChange={(e) => updateForm({ variant: e.target.value })} placeholder="e.g. M4 Competition" />
                </div>
                <label className="block">
                  <span className="mb-2 block text-sm font-medium text-gray-600">Body Type</span>
                  <div className="grid grid-cols-4 gap-2">
                    {bodyTypes.map((type) => (
                      <button key={type} onClick={() => updateForm({ bodyType: type })} className={form.bodyType === type ? "rounded-xl border border-blue bg-blue/5 px-3 py-2 text-xs font-semibold text-blue" : "rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 text-xs text-gray-600"}>
                        {type}
                      </button>
                    ))}
                  </div>
                </label>
              </div>
            )}

            {step === 2 && (
              <div className="space-y-4">
                <h2 className="font-montserrat text-3xl font-bold text-gray-900">Specifications</h2>
                <div className="grid gap-3 sm:grid-cols-2">
                  <Input label="Price (USD) *" value={form.price} onChange={(e) => updateForm({ price: e.target.value })} placeholder="28500" />
                  <Input label="Mileage (km)" value={form.mileage} onChange={(e) => updateForm({ mileage: e.target.value })} placeholder="45200" />
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <label className="block">
                    <span className="mb-2 block text-sm font-medium text-gray-600">Fuel</span>
                    <select value={form.fuel} onChange={(e) => updateForm({ fuel: e.target.value })} className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-blue">
                      {fuelTypes.map((f) => <option key={f}>{f}</option>)}
                    </select>
                  </label>
                  <label className="block">
                    <span className="mb-2 block text-sm font-medium text-gray-600">Transmission</span>
                    <select value={form.transmission} onChange={(e) => updateForm({ transmission: e.target.value })} className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-blue">
                      {transmissions.map((t) => <option key={t}>{t}</option>)}
                    </select>
                  </label>
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <Input label="Color" value={form.color} onChange={(e) => updateForm({ color: e.target.value })} placeholder="e.g. Alpine White" />
                  <label className="block">
                    <span className="mb-2 block text-sm font-medium text-gray-600">Condition</span>
                    <select value={form.condition} onChange={(e) => updateForm({ condition: e.target.value })} className="w-full rounded-xl border border-gray-200 px-4 py-3 outline-none focus:border-blue">
                      {conditions.map((c) => <option key={c}>{c}</option>)}
                    </select>
                  </label>
                </div>
                <button onClick={() => updateForm({ negotiable: !form.negotiable })} className={form.negotiable ? "rounded-full border border-blue bg-blue/5 px-4 py-2 text-sm font-semibold text-blue" : "rounded-full border border-gray-200 px-4 py-2 text-sm text-gray-600"}>
                  Negotiable: {form.negotiable ? "Yes" : "No"}
                </button>
              </div>
            )}

            {step === 3 && (
              <div className="space-y-4">
                <h2 className="font-montserrat text-3xl font-bold text-gray-900">Condition & Features</h2>
                <p className="text-sm text-gray-500">Select all applicable features.</p>
                <div className="flex flex-wrap gap-2">
                  {popularFeatures.map((feature) => {
                    const selected = selectedFeatures.includes(feature);
                    return (
                      <button
                        key={feature}
                        onClick={() => toggleFeature(feature)}
                        className={selected ? "rounded-full border border-blue bg-blue/5 px-4 py-2 text-sm font-semibold text-blue" : "rounded-full border border-gray-200 bg-gray-50 px-4 py-2 text-sm text-gray-600"}
                      >
                        {feature}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            {step === 4 && (
              <div className="space-y-4">
                <h2 className="font-montserrat text-3xl font-bold text-gray-900">Photos</h2>
                <label className="flex cursor-pointer flex-col items-center justify-center rounded-3xl border-2 border-dashed border-gray-200 bg-gray-50 p-10 text-center hover:border-blue/30 hover:bg-blue/5">
                  <Upload size={26} strokeWidth={1.5} className="text-blue" />
                  <p className="mt-3 font-semibold text-gray-900">Drag & drop or click to upload</p>
                  <p className="mt-1 text-sm text-gray-500">Minimum 3 photos, maximum 20</p>
                  <input type="file" multiple accept="image/*" className="hidden" onChange={onImageUpload} />
                </label>

                {images.length > 0 && (
                  <div className="grid gap-3 sm:grid-cols-3">
                    {images.map((image, index) => (
                      <div key={index} className="relative overflow-hidden rounded-2xl border border-gray-100">
                        <img src={image.url} alt={`Upload ${index + 1}`} className="h-28 w-full object-cover" />
                        <div className="absolute inset-x-0 bottom-0 flex items-center justify-between bg-black/60 px-2 py-1 text-xs text-white">
                          <button onClick={() => setPrimaryImageIndex(index)} className={primaryImageIndex === index ? "font-semibold text-blue-200" : ""}>
                            <Camera size={12} className="inline" /> {primaryImageIndex === index ? "★ Primary" : "Primary"}
                          </button>
                          <button onClick={() => removeImage(index)}>
                            <Trash2 size={12} />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {step === 5 && (
              <div className="space-y-4">
                <h2 className="font-montserrat text-3xl font-bold text-gray-900">Location & Description</h2>
                <div className="grid gap-3 sm:grid-cols-2">
                  <Input
                    icon={MapPin}
                    label="City"
                    value={form.city}
                    onChange={(e) => updateForm({ city: e.target.value })}
                    placeholder="Enter city"
                  />
                  <Input
                    label="State"
                    value={form.state}
                    onChange={(e) => updateForm({ state: e.target.value })}
                    placeholder="Enter state"
                  />
                </div>
                <label className="block">
                  <span className="mb-2 block text-sm font-medium text-gray-600">Description</span>
                  <textarea
                    value={form.description}
                    onChange={(e) => updateForm({ description: e.target.value })}
                    rows={6}
                    className="w-full rounded-2xl border border-gray-200 px-4 py-3 outline-none focus:border-blue"
                    placeholder="Describe condition, ownership, service history, upgrades..."
                  />
                </label>
              </div>
            )}

            {step === 6 && (
              <div className="space-y-5">
                <h2 className="font-montserrat text-3xl font-bold text-gray-900">Review & Publish</h2>
                <div className="rounded-2xl border border-gray-100 bg-gray-50 p-4">
                  <p className="text-sm text-gray-500">Preview</p>
                  <p className="mt-1 text-lg font-semibold text-gray-900">
                    {previewTitle || "Your Vehicle"}
                  </p>
                  <p className="mt-1 text-sm text-gray-600">{form.bodyType} • {form.fuel} • {form.transmission}</p>
                  {form.city && <p className="mt-1 text-sm text-gray-500">📍 {form.city}{form.state ? `, ${form.state}` : ""}</p>}
                  <p className="mt-2 font-mono text-2xl font-bold text-blue">${form.price || "0"}</p>
                  {images.length > 0 && <p className="mt-1 text-xs text-gray-400">{images.length} photo{images.length !== 1 ? "s" : ""} attached</p>}
                  {selectedFeatures.length > 0 && (
                    <div className="mt-2 flex flex-wrap gap-1">
                      {selectedFeatures.map((f) => (
                        <span key={f} className="rounded-full bg-blue/10 px-2 py-0.5 text-xs text-blue">{f}</span>
                      ))}
                    </div>
                  )}
                </div>

                <div className="grid gap-3 sm:grid-cols-2">
                  <label className={form.listingType === "free" ? "rounded-2xl border border-blue bg-blue/5 p-4" : "rounded-2xl border border-gray-200 p-4"}>
                    <input type="radio" name="listingType" className="mr-2" checked={form.listingType === "free"} onChange={() => updateForm({ listingType: "free" })} />
                    FREE Listing
                  </label>
                  <label className={form.listingType === "featured" ? "rounded-2xl border border-blue bg-blue/5 p-4" : "rounded-2xl border border-gray-200 p-4"}>
                    <input type="radio" name="listingType" className="mr-2" checked={form.listingType === "featured"} onChange={() => updateForm({ listingType: "featured" })} />
                    <span className="inline-flex items-center gap-1">
                      <Crown size={14} strokeWidth={1.5} className="text-blue" /> FEATURED ($9.99)
                    </span>
                  </label>
                </div>

                <div className="flex flex-wrap gap-2">
                  {["30", "60", "90"].map((duration) => (
                    <button
                      key={duration}
                      onClick={() => updateForm({ listingDuration: duration })}
                      className={form.listingDuration === duration ? "rounded-full border border-blue bg-blue/5 px-4 py-2 text-sm font-semibold text-blue" : "rounded-full border border-gray-200 px-4 py-2 text-sm text-gray-600"}
                    >
                      {duration} days
                    </button>
                  ))}
                </div>

                <Button className="w-full" size="lg" onClick={handlePublish} disabled={publishing}>
                  <Sparkles size={16} strokeWidth={1.5} />
                  {publishing ? "Publishing..." : "Publish Listing"}
                </Button>
              </div>
            )}
          </motion.article>

          <div className="mt-6 flex items-center justify-between">
            <Button variant="ghost" onClick={() => setStep((prev) => Math.max(1, prev - 1))} disabled={step === 1}>
              Back
            </Button>
            {step < steps.length ? (
              <Button onClick={() => setStep((prev) => Math.min(steps.length, prev + 1))}>
                Next
                <Plus size={14} strokeWidth={1.5} />
              </Button>
            ) : (
              <span className="text-sm font-semibold text-gray-500">Ready to publish</span>
            )}
          </div>
        </section>
      </main>
    </PageTransition>
  );
}
