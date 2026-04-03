import api from "@/lib/api";

function extractErrorMessage(error, fallback) {
  if (error?.response?.data?.error?.message) {
      return error.response.data.error.message;
  }
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    fallback
  );
}

function toServiceError(error, fallback) {
  const serviceError = new Error(extractErrorMessage(error, fallback));
  serviceError.status = error?.response?.status;
  return serviceError;
}

function mapAuthResponse(data) {
  return {
    accessToken: data?.accessToken || null,
    tokenType: data?.tokenType || "Bearer",
    expiresIn: Number(data?.expiresIn || 0),
    user: data?.user
      ? {
          id: data.user.id,
          email: data.user.email,
          fullName: data.user.fullName,
          role: data.user.role,
          verifiedSeller: Boolean(data.user.verifiedSeller)
        }
      : null
  };
}

export async function loginWithEmail(payload) {
  try {
    const response = await api.post("/auth/email/login", payload);
    return mapAuthResponse(response.data);
  } catch (error) {
    throw toServiceError(error, "Unable to sign in. Please try again.");
  }
}

export async function registerWithEmail(payload) {
  try {
    const response = await api.post("/auth/email/register", payload);
    return mapAuthResponse(response.data);
  } catch (error) {
    throw toServiceError(error, "Unable to create account. Please try again.");
  }
}

export async function getCurrentUser() {
  try {
    const response = await api.get("/auth/me");
    const user = response.data;
    return {
      id: user?.id,
      email: user?.email,
      fullName: user?.fullName,
      phone: user?.phone,
      role: user?.role,
      emailVerified: Boolean(user?.emailVerified),
      verifiedSeller: Boolean(user?.verifiedSeller)
    };
  } catch (error) {
    throw toServiceError(error, "Unable to load profile.");
  }
}

export async function logout() {
  try {
    await api.post("/auth/logout");
  } catch {
    // Best-effort logout endpoint call.
  }
}


