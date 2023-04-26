package cc.rigoligo.jlusmartcard

import android.app.Application

class JluSmartCardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: JluSmartCardApplication
            private set
    }
}