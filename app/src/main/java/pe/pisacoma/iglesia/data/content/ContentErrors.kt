package pe.pisacoma.iglesia.data.content

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

/**
 * The handler every view model launches with: a [ContentMissing] (the file is not stored and
 * the phone is offline) becomes the one notice the chrome shows; anything else crashes exactly
 * as an unhandled exception would.
 */
object ContentErrors : CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler

    /** Where a missing path is reported; the app points this at its ContentStore, tests at a recorder. */
    @Volatile var report: (ContentMissing) -> Unit = {}

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        val missing = exception as? ContentMissing ?: (exception.cause as? ContentMissing)
        if (missing != null) {
            report(missing)
            return
        }
        val thread = Thread.currentThread()
        val handler = thread.uncaughtExceptionHandler ?: Thread.getDefaultUncaughtExceptionHandler()
        if (handler != null) handler.uncaughtException(thread, exception) else throw exception
    }
}
