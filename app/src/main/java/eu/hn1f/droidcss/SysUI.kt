package eu.hn1f.droidcss

import android.annotation.SuppressLint
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View.OVER_SCROLL_NEVER
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Switch
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.customSysUI.Loader
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.callMethod
import eu.hn1f.droidcss.utils.dumpChildViews
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

        val notificationPanelView = findClass("com.android.systemui.shade.NotificationPanelView")

        notificationPanelView.hookMethod("addView").runAfter { param ->
            (param.thisObject as FrameLayout).dumpChildViews()
        }

        Loader().onLoad(loadPackageParam)

        /* val keyguardViewMediator = findClass("com.android.systemui.keyguard.KeyguardViewMediator")
        if(keyguardViewMediator != null) {
            keyguardViewMediator.hookMethod("handleSystemReady").runBefore { param ->
                param.result = null
            }
            keyguardViewMediator.hookMethod("handleShowInner").runBefore { param ->
                param.result = null
            }
        } */

        val qsImpl = findClass("com.android.systemui.qs.QSImpl")
        if(qsImpl != null) {
            Log.v("DroidCSS", "Hooked QSImpl")
            /*qsImpl.hookConstructor().runBefore { param ->
                param.setField("mQsDisabled", true)
            } */
        }

        //val phoneStatusBarView = findClass("com.android.systemui.statusbar.phone.PhoneStatusBarView")
        //phoneStatusBarView

        val mSwitch = findClass("android.widget.Switch")
        var isInHook = false

        mSwitch.hookConstructor().runAfter { param ->
            if(isInHook) {
                return@runAfter
            }
            isInHook = true
            var theme = SWITCH_THEME_LIGHT
            val s = param.thisObject as Switch
            if(isDarkMode(s.context)) {
                theme = SWITCH_THEME
            }
            val ds = Switch(ContextThemeWrapper(s.context, theme))
            s.trackDrawable = ds.trackDrawable
            s.thumbDrawable = ds.thumbDrawable
            s.background = ds.background
            s.foreground = ds.foreground
            isInHook = false
        }

        val mButton = findClass("android.widget.Button")
        var isInHookButton = false

        mButton.hookConstructor().runAfter { param ->
            val s = param.thisObject as Button
            val name = s.resources.getResourceName(s.context.theme.callMethod("getAppliedStyleResId") as Int)
            if(isInHookButton || !name.contains("Theme.SystemUI")) {
                return@runAfter
            }
            isInHookButton = true
            var theme = BUTTON_THEME_LIGHT
            if(isDarkMode(s.context)) {
                theme = BUTTON_THEME
            }
            val ds = Button(ContextThemeWrapper(s.context, theme))
            s.background = ds.background
            s.foreground = ds.foreground
            s.setTextColor(ds.textColors)
            isInHookButton = false
        }

        val footerView = findClass("com.android.systemui.statusbar.notification.footer.ui.view.FooterView")
        footerView.hookMethod("updateColors").runBefore { param ->
            param.result = null
        }

        val alphaButton = findClass("com.android.systemui.statusbar.AlphaOptimizedButton")
        alphaButton.hookConstructor().runAfter { param ->
            isInHookButton = true
            val s = param.thisObject as Button
            var theme = BUTTON_THEME_LIGHT
            if(isDarkMode(s.context)) {
                theme = BUTTON_THEME
            }
            val ds = Button(ContextThemeWrapper(s.context, theme))
            s.background = ds.background
            s.foreground = ds.foreground
            s.setTextColor(ds.textColors)
            isInHookButton = false
        }

        alphaButton.hookMethod("setBackgroundDrawable").runBefore { param ->
            if(isInHookButton) {
                return@runBefore
            }
            param.result = null
        }

        Log.v("DroidCSS", "Hello SystemUI mrrp~~ :3")
    }

    fun onResources(initPackageResourcesParam: XC_InitPackageResources.InitPackageResourcesParam) {
        /* initPackageResourcesParam.res.hookLayout("com.android.systemui", "layout", "status_bar", object : XC_LayoutInflated() {
            override fun handleLayoutInflated(param: LayoutInflatedParam) {
                //param.view.visibility = GONE;
                param.view.background = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(Color.MAGENTA, Color.WHITE))
            }
        }) */
    }
}