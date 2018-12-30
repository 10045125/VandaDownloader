package vanda.vandadownloader

import android.app.Application
import com.facebook.stetho.Stetho
import vanda.wzl.vandadownloader.core.DownloadContext

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        DownloadContext.setContext(this)
    }
}