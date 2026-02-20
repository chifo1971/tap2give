import { initializeApp, getApps } from "firebase/app";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  // These values come from the Firebase console for tap2give-c8a07.
  // In production, use environment variables (NEXT_PUBLIC_FIREBASE_*).
  projectId: "tap2give-c8a07",
  // Add remaining config from Firebase console:
  // apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  // authDomain: "tap2give-c8a07.firebaseapp.com",
  // storageBucket: "tap2give-c8a07.appspot.com",
  // messagingSenderId: "...",
  // appId: "...",
};

const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];
export const db = getFirestore(app);
