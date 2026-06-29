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
    }

    fun onResources(initPackageResourcesParam: XC_InitPackageResources.InitPackageResourcesParam) {

    }
}