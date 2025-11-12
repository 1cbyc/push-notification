iOS integration notes:
- Add Push Notifications and Background Modes -> Remote notifications to your target capabilities.
- Add the APNs Auth Key (.p8) in your server and use token-based authentication.
- Ensure your app registers for remote notifications and sends the device token to your backend.
- iOS may throttle silent pushes; keep local notifications authoritative.
