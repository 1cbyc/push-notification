# Hybrid Push Notifications — iOS + Android + Server for you guys

I just did what i explained on IG about the hybrid notification approach:
- Local notifications (iOS UNUserNotificationCenter, Android AlarmManager/WorkManager) for offline reminders.
- Remote silent/data pushes (APNs for iOS, FCM for Android) to wake the app and resync tasks, then reschedule local notifications.

**Check what I did**
- /server : Node.js example sending APNs (token-based) and FCM data messages.
- /ios : Swift snippets (AppDelegate, NotificationManager) + README-IOS.md
- /android : Kotlin snippets (FirebaseMessagingService, Worker, helper) + README-ANDROID.md

Try to replace the placeholders (YOUR_KEY_ID, YOUR_TEAM_ID, paths, package/module IDs) with values from your Apple/Firebase projects and you are good to go.
