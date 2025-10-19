// Firebase Functions for Stripe Terminal Connection Tokens
// File: functions/index.js

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const stripe = require('stripe')(functions.config().stripe.secret_key);

admin.initializeApp();

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
