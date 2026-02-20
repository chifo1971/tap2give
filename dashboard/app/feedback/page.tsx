"use client";

import { useState, useEffect, useCallback, useRef } from "react";
import { db } from "@/lib/firebase";
import { doc, setDoc, serverTimestamp } from "firebase/firestore";

// -------------------------------------------------------------------
// UAT Feedback Form for Tap2Give kiosk
//
// BUG FIX: The original implementation used addDoc() for auto-save
// (which creates a NEW doc each time) and updateDoc() for final submit.
// updateDoc() fails if:
//   a) Firestore rules don't allow "update" (only "create")
//   b) The doc ID from addDoc is lost between renders / page reloads
//
// FIX: Use setDoc() with { merge: true } for EVERY write.  This is an
// upsert — creates the doc if it doesn't exist, merges fields if it
// does.  The doc ID is generated once and persisted in sessionStorage
// so it survives re-renders and page refreshes.
// -------------------------------------------------------------------

const MOSQUE_ID = "TAJWEED";

interface FormData {
  testerName: string;
  testerEmail: string;
  deviceTested: string;
  overallRating: number;
  paymentFlowRating: number;
  paymentFlowNotes: string;
  uiRating: number;
  uiNotes: string;
  nfcRating: number;
  nfcNotes: string;
  issuesFound: string;
  generalComments: string;
}

const INITIAL_FORM: FormData = {
  testerName: "",
  testerEmail: "",
  deviceTested: "",
  overallRating: 0,
  paymentFlowRating: 0,
  paymentFlowNotes: "",
  uiRating: 0,
  uiNotes: "",
  nfcRating: 0,
  nfcNotes: "",
  issuesFound: "",
  generalComments: "",
};

const STEPS = [
  { title: "Tester Info", fields: ["testerName", "testerEmail", "deviceTested"] },
  { title: "Payment Flow", fields: ["paymentFlowRating", "paymentFlowNotes"] },
  { title: "UI / UX", fields: ["uiRating", "uiNotes"] },
  { title: "NFC Tap Experience", fields: ["nfcRating", "nfcNotes"] },
  { title: "Issues & Comments", fields: ["overallRating", "issuesFound", "generalComments"] },
];

// Generate or retrieve a stable document ID for this session.
function getOrCreateDocId(): string {
  const KEY = "uat_submission_id";
  let id = sessionStorage.getItem(KEY);
  if (!id) {
    id = crypto.randomUUID();
    sessionStorage.setItem(KEY, id);
  }
  return id;
}

export default function FeedbackPage() {
  const [form, setForm] = useState<FormData>(INITIAL_FORM);
  const [step, setStep] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const docIdRef = useRef<string>("");

  // Initialise the stable doc ID on mount (client-only).
  useEffect(() => {
    docIdRef.current = getOrCreateDocId();
  }, []);

  // ---- Auto-save (fires on every step change) ----
  const autoSave = useCallback(
    async (data: FormData) => {
      const id = docIdRef.current;
      if (!id) return;

      try {
        const ref = doc(db, "mosques", MOSQUE_ID, "uat_submissions", id);
        // merge: true  →  upsert.  Works regardless of create-only rules.
        await setDoc(
          ref,
          {
            ...data,
            status: "in_progress",
            lastSavedAt: serverTimestamp(),
          },
          { merge: true }
        );
      } catch (e) {
        // Auto-save is best-effort; don't block the user.
        console.error("Auto-save failed:", e);
      }
    },
    []
  );

  // Auto-save whenever the user moves between steps.
  useEffect(() => {
    autoSave(form);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [step]);

  // ---- Final submit ----
  const handleSubmit = async () => {
    // Validate required fields
    if (!form.testerName.trim()) {
      setError("Tester name is required.");
      setStep(0);
      return;
    }
    if (!form.testerEmail.trim()) {
      setError("Tester email is required.");
      setStep(0);
      return;
    }
    if (form.overallRating === 0) {
      setError("Overall rating is required.");
      setStep(4);
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const id = docIdRef.current;
      const ref = doc(db, "mosques", MOSQUE_ID, "uat_submissions", id);

      // KEY FIX:  Use setDoc with merge — NOT updateDoc.
      // setDoc+merge works whether the doc was previously created or not,
      // and it doesn't require a separate "update" permission in rules.
      await setDoc(
        ref,
        {
          ...form,
          status: "completed",
          submittedAt: serverTimestamp(),
          lastSavedAt: serverTimestamp(),
        },
        { merge: true }
      );

      // Clear the session so a fresh form gets a new doc ID.
      sessionStorage.removeItem("uat_submission_id");
      setSubmitted(true);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : "Submission failed";
      console.error("Submit failed:", e);
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  // ---- Field updater ----
  const update = (field: keyof FormData, value: string | number) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setError(null);
  };

  // ---- Render helpers ----
  if (submitted) {
    return (
      <div style={styles.container}>
        <div style={styles.card}>
          <h1 style={styles.success}>Thank you!</h1>
          <p>Your UAT feedback has been submitted successfully.</p>
          <button
            style={styles.btn}
            onClick={() => {
              setForm(INITIAL_FORM);
              setStep(0);
              setSubmitted(false);
              docIdRef.current = getOrCreateDocId();
            }}
          >
            Submit Another
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={{ color: "#2E7D32", marginBottom: 4 }}>Tap2Give UAT Feedback</h1>
        <p style={{ color: "#666", marginTop: 0 }}>
          Step {step + 1} of {STEPS.length}: {STEPS[step].title}
        </p>

        {/* Progress bar */}
        <div style={styles.progressTrack}>
          <div
            style={{
              ...styles.progressFill,
              width: `${((step + 1) / STEPS.length) * 100}%`,
            }}
          />
        </div>

        {error && <div style={styles.error}>{error}</div>}

        {/* Step 0: Tester Info */}
        {step === 0 && (
          <>
            <label style={styles.label}>
              Name *
              <input
                style={styles.input}
                value={form.testerName}
                onChange={(e) => update("testerName", e.target.value)}
                placeholder="Your name"
              />
            </label>
            <label style={styles.label}>
              Email *
              <input
                style={styles.input}
                type="email"
                value={form.testerEmail}
                onChange={(e) => update("testerEmail", e.target.value)}
                placeholder="you@example.com"
              />
            </label>
            <label style={styles.label}>
              Device Tested
              <input
                style={styles.input}
                value={form.deviceTested}
                onChange={(e) => update("deviceTested", e.target.value)}
                placeholder="e.g. Samsung Galaxy Tab A8"
              />
            </label>
          </>
        )}

        {/* Step 1: Payment Flow */}
        {step === 1 && (
          <>
            <RatingField
              label="Payment Flow Rating"
              value={form.paymentFlowRating}
              onChange={(v) => update("paymentFlowRating", v)}
            />
            <label style={styles.label}>
              Notes
              <textarea
                style={styles.textarea}
                value={form.paymentFlowNotes}
                onChange={(e) => update("paymentFlowNotes", e.target.value)}
                placeholder="Any issues with the payment flow?"
              />
            </label>
          </>
        )}

        {/* Step 2: UI/UX */}
        {step === 2 && (
          <>
            <RatingField
              label="UI / UX Rating"
              value={form.uiRating}
              onChange={(v) => update("uiRating", v)}
            />
            <label style={styles.label}>
              Notes
              <textarea
                style={styles.textarea}
                value={form.uiNotes}
                onChange={(e) => update("uiNotes", e.target.value)}
                placeholder="Feedback on look & feel, layout, readability..."
              />
            </label>
          </>
        )}

        {/* Step 3: NFC */}
        {step === 3 && (
          <>
            <RatingField
              label="NFC Tap Experience"
              value={form.nfcRating}
              onChange={(v) => update("nfcRating", v)}
            />
            <label style={styles.label}>
              Notes
              <textarea
                style={styles.textarea}
                value={form.nfcNotes}
                onChange={(e) => update("nfcNotes", e.target.value)}
                placeholder="Did the tap work on first try? Any issues?"
              />
            </label>
          </>
        )}

        {/* Step 4: Overall / Issues */}
        {step === 4 && (
          <>
            <RatingField
              label="Overall Rating *"
              value={form.overallRating}
              onChange={(v) => update("overallRating", v)}
            />
            <label style={styles.label}>
              Issues Found
              <textarea
                style={styles.textarea}
                value={form.issuesFound}
                onChange={(e) => update("issuesFound", e.target.value)}
                placeholder="Describe any bugs or issues..."
                rows={4}
              />
            </label>
            <label style={styles.label}>
              General Comments
              <textarea
                style={styles.textarea}
                value={form.generalComments}
                onChange={(e) => update("generalComments", e.target.value)}
                placeholder="Anything else?"
                rows={4}
              />
            </label>
          </>
        )}

        {/* Navigation */}
        <div style={styles.nav}>
          {step > 0 && (
            <button style={styles.btnOutline} onClick={() => setStep(step - 1)}>
              Back
            </button>
          )}
          <div style={{ flex: 1 }} />
          {step < STEPS.length - 1 ? (
            <button style={styles.btn} onClick={() => setStep(step + 1)}>
              Next
            </button>
          ) : (
            <button
              style={{ ...styles.btn, opacity: submitting ? 0.6 : 1 }}
              disabled={submitting}
              onClick={handleSubmit}
            >
              {submitting ? "Submitting..." : "Submit Feedback"}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

// ---- Star rating component ----
function RatingField({
  label,
  value,
  onChange,
}: {
  label: string;
  value: number;
  onChange: (v: number) => void;
}) {
  return (
    <div style={{ marginBottom: 16 }}>
      <span style={styles.label}>{label}</span>
      <div style={{ display: "flex", gap: 8, marginTop: 4 }}>
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            key={n}
            onClick={() => onChange(n)}
            style={{
              fontSize: 28,
              background: "none",
              border: "none",
              cursor: "pointer",
              color: n <= value ? "#FFB300" : "#ccc",
            }}
          >
            &#9733;
          </button>
        ))}
      </div>
    </div>
  );
}

// ---- Inline styles ----
const styles: Record<string, React.CSSProperties> = {
  container: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "#f5f5f5",
    padding: 16,
  },
  card: {
    background: "#fff",
    borderRadius: 12,
    padding: 32,
    maxWidth: 540,
    width: "100%",
    boxShadow: "0 2px 12px rgba(0,0,0,0.08)",
  },
  label: {
    display: "block",
    fontWeight: 600,
    marginBottom: 12,
    fontSize: 14,
    color: "#333",
  },
  input: {
    display: "block",
    width: "100%",
    padding: "10px 12px",
    marginTop: 4,
    border: "1px solid #ddd",
    borderRadius: 6,
    fontSize: 14,
    boxSizing: "border-box" as const,
  },
  textarea: {
    display: "block",
    width: "100%",
    padding: "10px 12px",
    marginTop: 4,
    border: "1px solid #ddd",
    borderRadius: 6,
    fontSize: 14,
    resize: "vertical" as const,
    boxSizing: "border-box" as const,
  },
  progressTrack: {
    height: 6,
    background: "#e0e0e0",
    borderRadius: 3,
    marginBottom: 20,
    overflow: "hidden",
  },
  progressFill: {
    height: "100%",
    background: "#2E7D32",
    borderRadius: 3,
    transition: "width 0.3s ease",
  },
  nav: {
    display: "flex",
    gap: 12,
    marginTop: 24,
  },
  btn: {
    padding: "10px 24px",
    background: "#2E7D32",
    color: "#fff",
    border: "none",
    borderRadius: 6,
    fontSize: 14,
    fontWeight: 600,
    cursor: "pointer",
  },
  btnOutline: {
    padding: "10px 24px",
    background: "transparent",
    color: "#2E7D32",
    border: "2px solid #2E7D32",
    borderRadius: 6,
    fontSize: 14,
    fontWeight: 600,
    cursor: "pointer",
  },
  error: {
    background: "#FFEBEE",
    color: "#C62828",
    padding: "10px 14px",
    borderRadius: 6,
    marginBottom: 16,
    fontSize: 14,
  },
  success: {
    color: "#2E7D32",
  },
};
