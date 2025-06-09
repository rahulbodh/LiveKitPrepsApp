const express = require('express');
const admin = require('firebase-admin');
const bodyParser = require('body-parser');
const app = express();
const PORT = 3000;

// Path to your Firebase service account key file
// Ensure this file is kept secure and not exposed publicly
const serviceAccount = require('./serviceAccountKey.json');

// Initialize Firebase Admin SDK
// This allows your Node.js server to interact with Firebase services,
// including sending FCM messages.
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Middleware to parse JSON request bodies
app.use(bodyParser.json());

// POST endpoint to send FCM notification
// This endpoint expects a JSON payload in the request body with
// details about the call and the recipient's device token(s).
app.post('/send-notification', async (req, res) => {
  const {
    callerName,
    callerAvatar,
    receiverAvatar,
    receiverAuthId,
    channel,
    callType,
    tokens // Array of device registration tokens
  } = req.body;

  // Basic validation: ensure tokens are provided
  if (!tokens || tokens.length === 0) {
    return res.status(400).json({
      success: false,
      error: 'No device token provided'
    });
  }

  // Construct the FCM message payload
  // This message is designed to be a "data-only" message.
  // This means the Android/iOS client app's FirebaseMessagingService.onMessageReceived()
  // will always be triggered, even if the app is in the background or killed.
  const message = {
    // Sending to a single token for this example.
    // For multiple tokens, consider `sendEachForDevice()` or `sendMulticast()`.
    token: tokens[0],

    // The 'data' payload: This is where you put your custom key-value pairs.
    // This entire object will be delivered to your app's onMessageReceived().
    data: {
      name: callerName,
      message: `${callerName} is calling you`, // A user-friendly message
      channel: channel,
      route: '/loadingScreenReceiver', // Example route for the client app
      callerAvatar: callerAvatar,
      receiverAvatar: receiverAvatar,
      userId: receiverAuthId,
      title: `${callerName} is calling you`, // Redundant here, but common for client processing
      body: callerName, // Redundant here, but common for client processing
      type: callType // Custom call type, e.g., 'audio', 'video'
    },

    // IMPORTANT for data-only messages:
    // Do NOT include a top-level 'notification' field.
    // If present, it will cause the system to display a notification when the app is in the background/killed.
    // notification: {
    //   title: `${callerName} is calling you`,
    //   body: callerName
    // },

    // Android specific configurations for a data-only message.
    // Crucially, the 'notification' object is REMOVED here to prevent automatic system notification display.
    android: {
      priority: "high", // Ensures immediate delivery, even if the device is in Doze mode.
      ttl: 70 * 1000, // Time-to-live in milliseconds (70 seconds). IMPORTANT: FCM's TTL is in seconds, but the Node.js Admin SDK often expects milliseconds here. Double-check your Admin SDK version if you encounter issues. Standard FCM `ttl` is in seconds, but within `admin.messaging().send()`, some fields might use milliseconds depending on exact context. I'm using 70 seconds as a standard call duration. If you consistently see `TTL: 0 seconds` in Logcat with this, try just `70` for `ttl`.
      // The 'notification' block is completely removed here to ensure a pure data-only message.
    },

    // APNs (Apple Push Notification service) specific configurations.
    // For VoIP calls, 'apns-push-type': 'voip' is standard to wake the app silently.
    apns: {
      headers: {
        "apns-push-type": "voip", // Indicates a VoIP push for incoming calls
        "apns-expiration": "1604750400", // Example expiration timestamp (use a valid future timestamp)
        "apns-priority": "10" // High priority for immediate delivery
      }
      // For a truly silent data message on iOS, ensure no 'alert' or 'sound' fields
      // are present in the 'aps' dictionary. The 'voip' type is designed for this.
    },

    // WebPush specific configurations (for web applications).
    webpush: {
      headers: {
        Urgency: "high" // High urgency for immediate delivery
      }
      // For web, you'll still need client-side JavaScript (Service Worker)
      // to handle the push event and display a notification.
    }
  };

  try {
    // Send the message using the Firebase Admin SDK
    const response = await admin.messaging().send(message);
    console.log("Successfully sent message:", response);
    res.status(200).json({
      success: true,
      message: "Data-only notification sent",
      response: response // Contains message ID
    });
  } catch (error) {
    // Log and return any errors that occur during message sending
    console.error("Error sending FCM data-only message:", error);
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// Start the Express server and listen for incoming requests
app.listen(PORT, () => {
  console.log(`🚀 Server running at http://localhost:${PORT}`);
});
