import Foundation
import Firebase

/**
 * Utility class encapsulating the Firebase Analytics instance for the app
 * and providing functions that handle event logging operations.
 * https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event.html
 */
class Analytics {

  /**
   * Tracks a non-fatal exception in Firebase Analytics.
   * @param e Error that was raised
   * @param message error message associated with the exception catch block
   */
  static func logAndTrackException(_ e: Error, message: @escaping () -> String) {
    logger.error(e, message: message)
    Crashlytics.sharedInstance().recordError(e)
  }

  /**
   * Tracks an error that is not a thrown exception.
   * @param source source of the event (usually `self`)
   * @param name name of the event
   * @param detail optional event detail information
   */
  static func logAndTrackAppError(source: Any?, name: String, detail: String = "") {
    logger.error { "logAndTrackAppError: source\(TextUtils.className(source)), name=\(name), detail=\(detail)" }
    Firebase.Analytics.logEvent("app_error", parameters: [
      "source": TextUtils.className(source),
      "name": name,
      "detail": detail])
  }

  /**
   * Tracks an event.
   * @param source source of the event (usually `self`)
   * @param name name of the event
   * @param detail optional event detail information
   */
  static func trackAppEvent(source: Any?, name: String, detail: String = "") {
    Firebase.Analytics.logEvent("app_event", parameters: [
      "source": TextUtils.className(source),
      "name": name,
      "detail": detail])
  }

  /**
   * Tracks an event that takes a significant amount of time.
   * @param millis number of milliseconds
   * @param source source of the event (usually `self`)
   * @param name name of the event
   * @param detail optional event detail information
   */
  static func trackAppMillis(_ millis: UInt64, source: Any?, name: String, detail: String = "") {
    Firebase.Analytics.logEvent("app_millis", parameters: [
      "source": TextUtils.className(source),
      "name": name,
      "detail": detail,
      AnalyticsParameterValue: millis])
  }

  /**
   * Tracks a screen view, such as when a user resumes an activity or fragment.
   * @param view the view to get the event name from
   */
  static func trackView(_ view: IAnalyticsViewName) {
    let name = view.getAnalyticsViewName()
    Firebase.Analytics.logEvent(AnalyticsEventViewItem, parameters: [
      AnalyticsParameterItemName: name,
      AnalyticsParameterItemCategory: name,
      AnalyticsParameterItemID: TextUtils.className(view)])
  }
}

protocol IAnalyticsViewName {
  func getAnalyticsViewName() -> String
}
