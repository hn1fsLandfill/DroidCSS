package eu.hn1f.droidcss.customSysUI

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SystemUIService: Service() {
    override fun onCreate() {
        super.onCreate()
        // throw RuntimeException("mrow meow mrrp")
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null;
    }
}