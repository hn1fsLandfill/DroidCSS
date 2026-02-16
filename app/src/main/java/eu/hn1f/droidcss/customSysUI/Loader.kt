package eu.hn1f.droidcss.customSysUI

import android.app.Application
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.callMethod
import eu.hn1f.droidcss.utils.callMethodSilently
import eu.hn1f.droidcss.utils.getExtraField
import eu.hn1f.droidcss.utils.getField
import eu.hn1f.droidcss.utils.hookConstructor
import eu.hn1f.droidcss.utils.hookMethod
import eu.hn1f.droidcss.utils.setExtraField
import eu.hn1f.droidcss.utils.setField

class Loader {
    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val systemUIApplicationImpl = findClass("com.android.systemui.application.impl.SystemUIApplicationImpl")
        if(systemUIApplicationImpl != null) {
            systemUIApplicationImpl.hookConstructor().runBefore { param ->
                param.thisObject.setExtraField("realApplication", SystemUIApplication())
            }
            systemUIApplicationImpl.hookMethod("onCreate").runBefore { param ->
                val realApplication =
                    (param.thisObject.getExtraField("realApplication") as SystemUIApplication)

                realApplication.callMethod("attachBaseContext",
                    (param.thisObject as Application).baseContext)
                realApplication.callMethodSilently("setLoadedApk",
                    (param.thisObject as Application).baseContext)

                try {
                    realApplication.onCreate()
                    param.result = null
                }
                catch (e: Throwable) {
                    param.throwable = e
                }
            }
            systemUIApplicationImpl.hookMethod("onConfigurationChanged").runBefore { param ->
                val realApplication =
                    (param.thisObject.getExtraField("realApplication") as SystemUIApplication)

                try {
                    realApplication.onConfigurationChanged(param.args[0] as Configuration)
                    param.result = null
                }
                catch (e: Throwable) {
                    param.throwable = e
                }
            }
        }

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

                realService.callMethod("attachBaseContext",
                    (param.thisObject as Service).baseContext)
                realService.setField("mClassName",
                    param.thisObject.getField("mClassName"))
                realService.setField("mToken",
                    param.thisObject.getField("mToken"))
                realService.setField("mApplication",
                    param.thisObject.getField("mApplication"))
                // TODO: Without setting this we might break certain things
                //realService.setField("mActivityManager",
                //    param.thisObject.getField("mActivityManager"))

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

                realService.callMethod("attachBaseContext",
                    (param.thisObject as Service).baseContext)
                realService.setField("mClassName",
                    param.thisObject.getField("mClassName"))
                realService.setField("mToken",
                    param.thisObject.getField("mToken"))
                realService.setField("mApplication",
                    param.thisObject.getField("mApplication"))
                // TODO: Without setting this we might break certain things
                //realService.setField("mActivityManager",
                //    param.thisObject.getField("mActivityManager"))

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