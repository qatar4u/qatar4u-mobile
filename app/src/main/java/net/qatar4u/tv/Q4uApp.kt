package net.qatar4u.mobile
import android.app.Application
import androidx.work.Configuration
// No background polling by default on the phone edition.
class Q4uApp : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setMinimumLoggingLevel(android.util.Log.WARN).build()
}
