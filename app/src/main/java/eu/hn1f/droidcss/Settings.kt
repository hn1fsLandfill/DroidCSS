package eu.hn1f.droidcss

import android.annotation.SuppressLint
import android.util.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Settings {

    @Suppress("UNUSED_PARAMETER")
    @SuppressLint("SetTextI18n")
    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        Log.v("DroidCSS", "Hello Settings mrrp~~ :3")
    }
}