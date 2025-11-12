/**
 * Minimal server to send silent/data pushes to iOS (APNs) and Android (FCM).
 *
 * WARNING: This is demo code. Do NOT commit keys to version control.
 * Replace placeholders with your own service-account JSON and APNs key file paths.
 */
const apn = require('apn');
const admin = require('firebase-admin');
const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');

const app = express();
app.use(bodyParser.json());

// Initialize FCM admin (replace with your service account path)
// const serviceAccount = require('./path/to/fcm-service-account.json');
// admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
// For demo: initialize only if service account provided via env
if (process.env.FCM_SERVICE_ACCOUNT_JSON) {
  const cred = JSON.parse(process.env.FCM_SERVICE_ACCOUNT_JSON);
  admin.initializeApp({ credential: admin.credential.cert(cred) });
  console.log('FCM initialized');
} else {
  console.warn('FCM not initialized. Set FCM_SERVICE_ACCOUNT_JSON env containing the service account JSON to enable FCM.');
}

// Initialize APNs provider (token-based)
// You need an AuthKey .p8 file from Apple Developer, plus keyId and teamId
let apnProvider = null;
if (process.env.APN_KEY_P8 && process.env.APN_KEY_ID && process.env.APN_TEAM_ID && process.env.APN_TOPIC) {
  const keyString = process.env.APN_KEY_P8;
  apnProvider = new apn.Provider({
    token: {
      key: Buffer.from(keyString, 'utf8'),
      keyId: process.env.APN_KEY_ID,
      teamId: process.env.APN_TEAM_ID
    },
    production: process.env.APN_PRODUCTION === 'true'
  });
  console.log('APNs provider initialized');
} else {
  console.warn('APNs not initialized. Set APN_KEY_P8, APN_KEY_ID, APN_TEAM_ID, and APN_TOPIC env vars to enable APNs.');
}

// POST /send/sync
// body: { platform: 'ios'|'android', deviceToken: '<token>', changeset_id: '123' }
app.post('/send/sync', async (req, res) => {
  const { platform, deviceToken, changeset_id } = req.body;
  try {
    if (platform === 'ios') {
      if (!apnProvider) throw new Error('APNs not configured on server');
      const note = new apn.Notification();
      note.topic = process.env.APN_TOPIC; // e.g. com.yourcompany.todoapp
      note.payload = { sync: true, changeset_id: String(changeset_id || '') };
      note.contentAvailable = 1; // silent push
      note.expiry = Math.floor(Date.now() / 1000) + 3600;
      const result = await apnProvider.send(note, deviceToken);
      
      // Check for failures
      if (result.failed && result.failed.length > 0) {
        console.error('APNs send failures:', result.failed);
        const firstFailure = result.failed[0];
        if (firstFailure.error) {
          // Invalid token - should remove from database
          if (firstFailure.error.status === 410 || firstFailure.error.reason === 'Unregistered') {
            console.warn('Device token is invalid/unregistered:', deviceToken);
            // TODO: Remove this token from your database
          }
          return res.status(400).json({ error: 'APNs send failed', details: firstFailure.error });
        }
      }
      
      return res.json({ 
        sent: result.sent?.length || 0,
        failed: result.failed?.length || 0,
        result 
      });
    } else if (platform === 'android') {
      if (!admin.apps.length) throw new Error('FCM not configured on server');
      const message = {
        token: deviceToken,
        android: { priority: 'high' },
        data: { sync: 'true', changeset_id: String(changeset_id || '') }
      };
      const resp = await admin.messaging().send(message);
      return res.json({ result: resp });
    } else {
      res.status(400).json({ error: 'invalid platform' });
    }
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Server listening on ${port}`));
