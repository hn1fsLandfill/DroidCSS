package eu.hn1f.droidcss

import android.content.ComponentName
import android.util.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.hookMethod

class Framework {
    val SYSTEMUI = "eu.hn1f.holoui"
    val SYSTEMUI_SERVICE = "eu.hn1f.holoui/eu.hn1f.holoui.SystemUIService"
    val KEYGUARD_SERVICE = "eu.hn1f.holoui/eu.hn1f.holoui.KeyguardService"

    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val systemServer = findClass("com.android.server.SystemServer")
        if(systemServer != null) {
            Log.v("DroidCSS", "Got SystemServer")
        }

        SignatureBypass().onLoad(loadPackageParam)

        val resources = findClass("android.content.res.Resources")
        if(resources != null) {
            Log.v("DroidCSS", "Got Resources")
            resources.hookMethod("getString").runAfter { param ->
                Log.v("DroidCSS", "resource ${param.args[0] as Int} -> ${param.result as String}")
                if((param.result as String).contains("KeyguardService")) {
                    param.result = KEYGUARD_SERVICE;
                    Log.v("DroidCSS", "new: ${param.result as String}")
                } else if((param.result as String).contains("SystemUIService")) {
                    param.result = SYSTEMUI_SERVICE;
                    Log.v("DroidCSS", "new: ${param.result as String}")
                } else if((param.result as String).equals("com.android.systemui")) {
                    param.result = SYSTEMUI;
                    Log.v("DroidCSS", "new: ${param.result as String}")
                }
            }
        }
    }
}