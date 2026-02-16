package eu.hn1f.droidcss.customSysUI

import android.content.Intent
import android.util.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.getExtraField
import eu.hn1f.droidcss.utils.hookConstructor
import eu.hn1f.droidcss.utils.hookMethod
import eu.hn1f.droidcss.utils.setExtraField

class Loader {
    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val systemUIService = findClass("com.android.systemui.SystemUIService")
        if(systemUIService != null) {
            Log.v("DroidCSS", "Hooked SystemUIService")
            systemUIService.hookConstructor().runBefore { param ->
                Log.v("DroidCSS", "Replacing service")
                param.thisObject.setExtraField("realService", SystemUIService())
            }
            systemUIService.hookMethod("onCreate").runBefore { param ->
                val realService =
                    (param.thisObject.getExtraField("realService") as SystemUIService)
                try {
                    realService.onCreate()
                    param.result = null
                }
                catch (e: Throwable) {
                    param.throwable = e
                }
            }
            systemUIService.hookMethod("onBind").runBefore { param ->
                val realService =
                    (param.thisObject.getExtraField("realService") as SystemUIService)
                param.result = realService.onBind(param.args[0] as Intent?)
            }
        }

        val keyguardService = findClass("com.android.systemui.keyguard.KeyguardService")
        if(keyguardService != null) {
            Log.v("DroidCSS", "Hooked KeyguardService")
            keyguardService.hookConstructor().runBefore { param ->
                Log.v("DroidCSS", "Replacing service")
                param.thisObject.setExtraField("realService", KeyguardService())
            }
            keyguardService.hookMethod("onCreate").runBefore { param ->
                val realService =
                    (param.thisObject.getExtraField("realService") as KeyguardService)
                try {
                    realService.onCreate()
                    param.result = null
                }
                catch (e: Throwable) {
                    param.throwable = e
                }
            }
            keyguardService.hookMethod("onBind").runBefore { param ->
                val realService =
                    (param.thisObject.getExtraField("realService") as KeyguardService)
                param.result = realService.onBind(param.args[0] as Intent?)
            }
        }
    }
}