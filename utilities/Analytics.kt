package utilities

import Injector
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineExceptionHandler
import mu.KLogging
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class encapsulating the Firebase Analytics instance for the app
 * and providing functions that handle event logging operations.
 * https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event.html
 */
@Singleton
class Analytics @Inject constructor(
  private val firebase: FirebaseAnalytics
) {

  fun coroutineExceptionHandler() = CoroutineExceptionHandler {
      _, throwable -> logAndTrackException(throwable) { "CoroutineExceptionHandler" }
  }

  companion object : KLogging() {
    val INSTANCE: Analytics get() = Injector.analytics
    val firebase: FirebaseAnalytics get() = INSTANCE.firebase
    fun coroutineExceptionHandler() = INSTANCE.coroutineExceptionHandler()

    /**
     * Tracks a non-fatal exception in Firebase Analytics.
     * @param t Throwable that was raised
     * @param message error message associated with the exception catch block
     */
    fun logAndTrackException(t: Throwable, message: () -> Any?) {
      try {
        logger.error(t, message)
        Crashlytics.logException(t)
      }
      catch (e: Exception) {
        logger.error(e) { "logAndTrackException" }
      }
    }

    /**
     * Tracks an error that is not a thrown exception.
     * @param source source of the event (usually `this`)
     * @param name name of the event
     * @param detail optional event detail information
     */
    fun logAndTrackAppError(source: Any?, name: String, detail: String = "") {
      try {
        logger.error { "logAndTrackAppError: source=$source, name=$name, detail=$detail" }
        val bundle = Bundle()
        bundle.putString("source", if (source == null) "null" else source::class.toString())
        bundle.putString("name", name)
        bundle.putString("detail", detail)
        firebase.logEvent("app_error", bundle)
      }
      catch (e: Exception) {
        logger.error(e) { "trackAppError: name=$name, detail=$detail" }
      }
    }

    /**
     * Tracks an event.
     * @param source source of the event (usually `this`)
     * @param name name of the event
     * @param detail optional event detail information
     */
    fun trackAppEvent(source: Any?, name: String, detail: String = "") {
      try {
        val bundle = Bundle()
        bundle.putString("source", if (source == null) "null" else source::class.toString())
        bundle.putString("name", name)
        bundle.putString("detail", detail)
        firebase.logEvent("app_event", bundle)
      }
      catch (e: Exception) {
        logger.error(e) { "trackAppEvent: name=$name, detail=$detail" }
      }
    }

    /**
     * Tracks an event that takes a significant amount of time.
     * @param millis number of milliseconds
     * @param source source of the event (usually `this`)
     * @param name name of the event
     * @param detail optional event detail information
     */
    fun trackAppMillis(millis: Long, source: Any?, name: String, detail: String = "") {
      try {
        val bundle = Bundle()
        bundle.putString("source", if (source == null) "null" else source::class.toString())
        bundle.putString("name", name)
        bundle.putString("detail", detail)
        bundle.putLong(FirebaseAnalytics.Param.VALUE, millis)
        firebase.logEvent("app_millis", bundle)
      }
      catch (e: Exception) {
        logger.error(e) { "trackAppMillis: millis=$millis, name=$name, detail=$detail" }
      }
    }

    /**
     * Tracks a screen view, such as when a user resumes an activity or fragment.
     * @param view the view to get the event name from
     */
    fun trackView(view: IAnalyticsViewName) {
      val name = view.getAnalyticsViewName()
      try {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, name)
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, view.javaClass.name)
        firebase.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
      }
      catch (e: Exception) {
        logger.error(e) { "trackView: name=$name" }
      }
    }
  }
}

interface IAnalyticsViewName {
  fun getAnalyticsViewName(): String
}
