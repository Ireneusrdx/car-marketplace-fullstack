import axios from "axios";
import { useAuthStore } from "../store/authStore";

// When VITE_API_URL is set (e.g. "http://localhost:8080"), use it as the base.
// When not set, fall back to relative "/api" which the Vite dev proxy forwards.
const envUrl = import.meta.env.VITE_API_URL;
const baseURL = envUrl ? `${envUrl}/api` : "/api";

const api = axios.create({
  baseURL,
  timeout: 15000,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

// Paths that should NOT trigger a redirect on 401
const AUTH_PATHS = ["/auth/me", "/auth/refresh", "/auth/logout"];

function isAuthPath(url) {
  return AUTH_PATHS.some((path) => url?.includes(path));
}

// Request interceptor — attach auth token
api.interceptors.request.use((config) => {
  const { accessToken } = useAuthStore.getState();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

// Prevent concurrent refresh storms: queue all 401 requests while one refresh is in-flight
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token);
  });
  failedQueue = [];
};

// Response interceptor — handle 401 with token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response, config } = error;

    if (
      response?.status === 401 &&
      config &&
      !config._retry &&
      !isAuthPath(config.url)
    ) {
      // Another refresh is already in-flight — queue this request
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            config.headers.Authorization = `Bearer ${token}`;
            return api(config);
          })
          .catch((err) => Promise.reject(err));
      }

      config._retry = true;
      isRefreshing = true;

      try {
        const { data } = await axios.post(
          `${baseURL}/auth/refresh`,
          {},
          { withCredentials: true }
        );

        const newToken = data.accessToken;
        useAuthStore.getState().setAuth({
          user: data.user,
          accessToken: newToken,
        });
        processQueue(null, newToken);
        config.headers.Authorization = `Bearer ${newToken}`;
        return api(config);
      } catch (refreshError) {
        processQueue(refreshError, null);
        useAuthStore.getState().clearAuth();
        if (!window.location.pathname.startsWith("/auth")) {
          window.location.href = "/auth";
        }
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;
