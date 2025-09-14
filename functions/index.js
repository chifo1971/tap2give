const stripe = require('stripe')(process.env.STRIPE_SECRET_KEY || 'your-stripe-secret-key-here');
// Firebase Functions for Stripe Terminal Connection Tokens
// File: functions/index.js

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const stripe = require('stripe')('${process.env.STRIPE_SECRET_KEY || "your-stripe-secret-key-here"}');

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
      // Create connection token with Stripe
      const connectionToken = await stripe.terminal.connectionTokens.create();
      
      // Log the request (without sensitive data)
      console.log('Connection token created for terminal');
      
      return res.status(200).json({
        secret: connectionToken.secret
      });
      
    } catch (error) {
      console.error('Error creating connection token:', error);
      return res.status(500).json({
        error: 'Failed to create connection token',
        details: error.message
      });
    }
  });
});

// Create PaymentIntent for Stripe Terminal
exports.createPaymentIntent = functions.https.onRequest((req, res) => {
  cors(req, res, async () => {
    if (req.method !== 'POST') {
      return res.status(405).json({
        error: 'Method not allowed'
      });
    }

    try {
      const { amount, currency = 'usd' } = req.body;
      
      if (!amount || amount <= 0) {
        return res.status(400).json({
          error: 'Invalid amount provided'
        });
      }

      // Create PaymentIntent with Stripe
      const paymentIntent = await stripe.paymentIntents.create({
        amount: Math.round(amount), // Amount in cents
        currency: currency,
        automatic_payment_methods: {
          enabled: true,
        },
        metadata: {
          source: 'tap2give_terminal'
        }
      });
      
      console.log(`PaymentIntent created: ${paymentIntent.id} for $${amount/100}`);
      
      return res.status(200).json({
        client_secret: paymentIntent.client_secret,
        id: paymentIntent.id
      });
      
    } catch (error) {
      console.error('Error creating PaymentIntent:', error);
      return res.status(500).json({
        error: 'Failed to create PaymentIntent',
        details: error.message
      });
    }
  });
});

// Optional: Log successful donations for mosque records
exports.logDonation = functions.https.onRequest((req, res) => {
  cors(req, res, async () => {
    if (req.method !== 'POST') {
      return res.status(405).json({
        error: 'Method not allowed'
      });
    }

    try {
      const { amount, paymentIntentId } = req.body;
      
      if (!amount || !paymentIntentId) {
        return res.status(400).json({
          error: 'Missing required fields: amount, paymentIntentId'
        });
      }

      // Store in Firestore
      await admin.firestore().collection('donations').add({
        amount: parseFloat(amount),
        paymentIntentId: paymentIntentId,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        status: 'completed'
      });

      console.log(`Donation logged: $${amount} - ${paymentIntentId}`);
      
      return res.status(200).json({
        success: true,
        message: 'Donation logged successfully'
      });
      
    } catch (error) {
      console.error('Error logging donation:', error);
      return res.status(500).json({
        error: 'Failed to log donation',
        details: error.message
      });
    }
  });
});

// Optional: Get daily donation summary for mosque admin
exports.getDailySummary = functions.https.onRequest((req, res) => {
  cors(req, res, async () => {
    if (req.method !== 'GET') {
      return res.status(405).json({
        error: 'Method not allowed'
      });
    }

    try {
      // Get today's date range
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);

      // Query today's donations
      const snapshot = await admin.firestore()
        .collection('donations')
        .where('timestamp', '>=', today)
        .where('timestamp', '<', tomorrow)
        .get();

      let totalAmount = 0;
      let totalCount = 0;

      snapshot.forEach(doc => {
        const data = doc.data();
        if (data.amount && data.status === 'completed') {
          totalAmount += data.amount;
          totalCount += 1;
        }
      });

      return res.status(200).json({
        date: today.toISOString().split('T')[0],
        totalAmount: totalAmount,
        totalCount: totalCount,
        averageAmount: totalCount > 0 ? totalAmount / totalCount : 0
      });
      
    } catch (error) {
      console.error('Error getting daily summary:', error);
      return res.status(500).json({
        error: 'Failed to get daily summary',
        details: error.message
      });
    }
  });
});
