import UIKit
import UserNotifications

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        requestNotificationPermissions()
        application.registerForRemoteNotifications()
        return true
    }

    func requestNotificationPermissions() {
        let center = UNUserNotificationCenter.current()
        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if let e = error { print("Auth error: \(e)") }
            print("Notification permission granted: \(granted)")
        }
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let tokenString = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        print("APNs device token: \(tokenString)")
        // TODO: send tokenString to your server for this user
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("Failed to register for remote notifications: \(error)")
    }

    // Handle silent push with background fetch
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        // APNs sends content-available as a number (1), not a boolean
        let hasSyncFlag = (userInfo["sync"] as? Bool) == true || 
                         (userInfo["sync"] as? Int) == 1 ||
                         (userInfo["aps"] as? [AnyHashable: Any])?["content-available"] as? Int == 1
        
        guard hasSyncFlag else {
            completionHandler(.noData)
            return
        }

        // Fetch minimal delta and reschedule local notifications
        Task {
            do {
                // Replace ApiClient with your networking client
                // Example: let tasks = try await ApiClient.shared.fetchTasks()
                // For now, using a placeholder that you'll need to implement
                print("TODO: Implement ApiClient.shared.fetchTasks()")
                
                // Clear and reschedule (example)
                UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
                // for t in tasks {
                //     NotificationManager.shared.scheduleReminder(id: t.id, title: t.title, body: t.note ?? "", date: t.dueDate)
                // }
                completionHandler(.newData)
            } catch {
                print("Background fetch error: \(error)")
                completionHandler(.failed)
            }
        }
    }
}

extension AppDelegate: UNUserNotificationCenterDelegate {
    // Handle notifications when app is in foreground
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                               willPresent notification: UNNotification,
                               withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        // Show notification even when app is in foreground
        completionHandler([.banner, .sound, .badge])
    }
    
    // Handle notification tap
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                               didReceive response: UNNotificationResponse,
                               withCompletionHandler completionHandler: @escaping () -> Void) {
        // Handle notification tap - navigate to task, etc.
        completionHandler()
    }
}
