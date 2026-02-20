// Firebase Functions for Stripe Terminal Connection Tokens
// File: functions/index.js

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const stripe = require('stripe')(functions.config().stripe.secret_key);

admin.initializeApp();

const db = admin.firestore();

// CORS configuration for Android app
const cors = require('cors')({
  origin: true,
});

// Create connection token for Stripe Terminal
exports.createConnectionToken = functions.https.onRequest((req, res) => {
  cors(req, res, async () => {
    // Only allow POST requests
    if (req.method !== 'POST') {
      return res.status(405).json({
        error: 'Method not allowed'
      });
    }

    try {
      // Create a connection token using Stripe SDK
      const connectionToken = await stripe.terminal.connectionTokens.create();

      res.json({
        secret: connectionToken.secret
      });
    } catch (error) {
      console.error('Error creating connection token:', error);
      res.status(500).json({
        error: error.message
      });
    }
  });
});

// One-time callable function to fix stuck UAT submissions.
// Finds all submissions with status "in_progress" under
// mosques/TAJWEED/uat_submissions and flips them to "completed",
// preserving the existing feedback data.
exports.fixStuckUatSubmissions = functions.https.onRequest((req, res) => {
  cors(req, res, async () => {
    if (req.method !== 'POST') {
      return res.status(405).json({ error: 'Method not allowed' });
    }

    try {
      const collectionRef = db
        .collection('mosques')
        .doc('TAJWEED')
        .collection('uat_submissions');

      const snapshot = await collectionRef
        .where('status', '==', 'in_progress')
        .get();

      if (snapshot.empty) {
        return res.json({ message: 'No stuck submissions found', fixed: 0 });
      }

      const batch = db.batch();
      const fixedIds = [];

      snapshot.forEach((doc) => {
        batch.update(doc.ref, {
          status: 'completed',
          fixedAt: admin.firestore.FieldValue.serverTimestamp(),
          fixNote: 'Auto-fixed: rules previously blocked update from in_progress to completed',
        });
        fixedIds.push(doc.id);
      });

      await batch.commit();

      res.json({
        message: `Fixed ${fixedIds.length} stuck submissions`,
        fixed: fixedIds.length,
        ids: fixedIds,
      });
    } catch (error) {
      console.error('Error fixing stuck submissions:', error);
      res.status(500).json({ error: error.message });
    }
  });
});
