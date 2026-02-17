package eu.hn1f.droidcss

import android.annotation.SuppressLint
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.os.UserHandle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View.OVER_SCROLL_NEVER
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Switch
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.callMethod
import eu.hn1f.droidcss.utils.dumpChildViews
import eu.hn1f.droidcss.utils.getField
import eu.hn1f.droidcss.utils.hookConstructor
import eu.hn1f.droidcss.utils.hookMethod


@Suppress("UNUSED_PARAMETER")
class SysUI {

    @Suppress("UNUSED_PARAMETER")
    @SuppressLint("SetTextI18n", "DiscouragedApi")
    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val androidView = findClass("android.view.View")

        androidView.hookMethod("setOverScrollMode").runBefore { param ->
            param.args[0] = OVER_SCROLL_NEVER
        }

        val systemUIService = findClass("com.android.systemui.SystemUIService")
        if(systemUIService != null) {
            Log.v("DroidCSS", "Hooked SystemUIService")

            systemUIService.hookMethod("onCreate").runBefore { param ->
                // incase framework fails us
                Log.v("DroidCSS", "Starting our SystemUIService")
                val service = param.thisObject as Service
                service.callMethod("startServiceAsUser", Intent().apply {
                    component = ComponentName(SYSTEMUI, ".SystemUIService")
                }, UserHandle::class.getField("SYSTEM"))
                param.result = null;
            }
            systemUIService.hookMethod("onBind").runBefore { param ->
                param.result = null;
            }
        }

        val keyguardService = findClass("com.android.systemui.keyguard.KeyguardService")
        if(keyguardService != null) {
            Log.v("DroidCSS", "Hooked KeyguardService")
            keyguardService.hookMethod("onCreate").runBefore { param ->
                param.result = null;
            }
            keyguardService.hookMethod("onBind").runBefore { param ->
                param.result = null;
            }
        }
    }

    fun onResources(initPackageResourcesParam: XC_InitPackageResources.InitPackageResourcesParam) {

    }
}