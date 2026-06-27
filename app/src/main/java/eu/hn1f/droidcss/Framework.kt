package eu.hn1f.droidcss

import android.content.ComponentName
import android.content.pm.ApplicationInfo
import android.util.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.getField
import eu.hn1f.droidcss.utils.hookMethod
import eu.hn1f.droidcss.utils.setField

class Framework {
    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val systemServer = findClass("com.android.server.SystemServer")
        if(systemServer != null) {
            Log.v("DroidCSS", "Got SystemServer")
        }

        SignatureBypass().onLoad(loadPackageParam)

        val resources = findClass("android.content.res.Resources")
        if(resources != null && REDIRECT_SYSTEMUI) {
            Log.v("DroidCSS", "Got Resources")
            resources.hookMethod("getString").runAfter { param ->
                if((param.result as String).contains("KeyguardService")) {
                    param.result = KEYGUARD_SERVICE;
                    Log.v("DroidCSS", "redirect: ${param.args[0] as Int} -> ${param.result as String}")
                } else if((param.result as String).contains("SystemUIService")) {
                    param.result = SYSTEMUI_SERVICE;
                    Log.v("DroidCSS", "redirect: ${param.args[0] as Int} -> ${param.result as String}")
                } else if((param.result as String).equals("com.android.systemui")) {
                    param.result = SYSTEMUI;
                    Log.v("DroidCSS", "redirect: ${param.args[0] as Int} -> ${param.result as String}")
                } else if((param.result as String).contains("com.android.systemui")) {
                    Log.v("DroidCSS", "systemui mentioned in (${param.args[0]})")
                }
            }
        }

        // Spoof SystemUI replacement as a system app
        val appInfo = findClass("android.content.pm.ApplicationInfo")

        appInfo.hookMethod("isSystemApp").runBefore { param ->
            val app = param.thisObject as ApplicationInfo?
            if(app?.packageName.equals(SYSTEMUI)) {
                param.result = true;
            }
        }

        appInfo.hookMethod("isSignedWithPlatformKey").runBefore { param ->
            val app = param.thisObject as ApplicationInfo?
            if(app?.packageName.equals(SYSTEMUI)) {
                param.result = true;
            }
        }

        appInfo.hookMethod("isAllowedToUseHiddenApis").runBefore { param ->
            val app = param.thisObject as ApplicationInfo?
            if(app?.packageName.equals(SYSTEMUI)) {
                param.result = true;
            }
        }

        val pkgManager = findClass("android.content.pm.PackageManager")

        pkgManager.hookMethod("getApplicationInfoAsUser").runAfter { param ->
            val result = param.result as ApplicationInfo?
            if(result?.packageName.equals(SYSTEMUI)) {
                result!!.flags = result.flags or ApplicationInfo.FLAG_SYSTEM
                result.setField("privateFlags", result.getField("privateFlags") as Int or (1 shl 20))
            }
        }
    }
}