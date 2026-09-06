package com.openjarvis

import android.app.Application
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Custom Application class whose only job is to make crashes visible.
 *
 * Without this, a crash on a background thread (or one that happens before
 * MainActivity's own try/catch runs) just kills the app with no way to see
 * why. This installs a global handler that writes the full stack trace to
 * a file before the crash proceeds normally, so the NEXT time the app is
 * opened, MainActivity can read that file and show the real error on
 * screen instead of silently trying (and possibly failing) again.
 */
class LaraApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                File(filesDir, CRASH_LOG_FILE).writeText(sw.toString())
            } catch (_: Throwable) {
                // If we can't even write the crash log, there's nothing more we can do here.
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        const val CRASH_LOG_FILE = "last_crash.txt"
    }
}
