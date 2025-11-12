Android integration notes:
- Use Firebase Cloud Messaging (FCM) with data-only messages (no notification payload) and high priority.
- Implement FirebaseMessagingService.onMessageReceived to enqueue a WorkManager job to fetch updates and reschedule local alarms/notifications.
- Some OEMs may restrict background delivery; test on target devices.
