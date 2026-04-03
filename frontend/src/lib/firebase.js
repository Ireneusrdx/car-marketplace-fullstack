import { initializeApp, getApps } from "firebase/app";
import { getAuth, connectAuthEmulator } from "firebase/auth";
import { getStorage, connectStorageEmulator } from "firebase/storage";

// Only initialise Firebase when a real API key has been supplied at build time.
// When running without Firebase keys (empty or default placeholder values),
// firebase is disabled and social auth buttons will be hidden — email auth
// continues to work through the backend.
const _apiKey = import.meta.env.VITE_FIREBASE_API_KEY;
export const firebaseEnabled =
  Boolean(_apiKey) && _apiKey !== "placeholder" && _apiKey !== "undefined";

let firebaseAuth = null;
let firebaseStorage = null;

if (firebaseEnabled) {
  const firebaseConfig = {
    apiKey: _apiKey,
    authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
    projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
    storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
    messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
    appId: import.meta.env.VITE_FIREBASE_APP_ID,
  };

  try {
    const app = getApps().length ? getApps()[0] : initializeApp(firebaseConfig);
    firebaseAuth = getAuth(app);
    firebaseStorage = getStorage(app);

    // Dev-only: connect to local emulator when running outside Docker
    if (import.meta.env.MODE === "development" && window.location.hostname === "localhost") {
      try {
        connectAuthEmulator(firebaseAuth, "http://localhost:9099", { disableWarnings: true });
        connectStorageEmulator(firebaseStorage, "localhost", 9199);
      } catch {
        // Emulator already connected
      }
    }
  } catch (error) {
    console.warn("Firebase initialisation failed:", error.message);
  }
} else {
  console.info("Firebase not configured — social auth disabled. Email auth works via the backend.");
}

export { firebaseAuth, firebaseStorage };
export default null;


