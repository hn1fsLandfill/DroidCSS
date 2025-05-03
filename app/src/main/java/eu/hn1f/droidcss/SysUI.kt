package eu.hn1f.droidcss

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View.OVER_SCROLL_NEVER
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.callMethod
import eu.hn1f.droidcss.utils.dumpChildViews
import eu.hn1f.droidcss.utils.getField
import eu.hn1f.droidcss.utils.getFieldSilently
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

        val c = findClass("com.android.systemui.qs.tileimpl.QSTileViewImpl")

        if (c != null) {
            Log.v("DroidCSS", "Replaced QSTileView with Custom tile")
            c.hookConstructor().runAfter { param ->
                val t: LinearLayout = param.thisObject as LinearLayout
                val tl: TextView = t.getField("label") as TextView
                var theme = BUTTON_THEME_LIGHT
                if(isDarkMode(t.context)) {
                    theme = BUTTON_THEME
                }
                val butt = Button(ContextThemeWrapper(t.context, theme), null, theme)
                butt.text = tl.text
                butt.layoutParams = LayoutParams(
                    MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.MATCH_PARENT
                )
                butt.setPadding(0,0,0,0)

                t.background = ColorDrawable(Color.TRANSPARENT)
                t.removeAllViews()
                t.addView(butt)
                butt.tag = "buttonTile"

                t.setPadding(0,0,0,0)
            }

            c.hookMethod("init").runBefore { param ->
                if(param.args[0] !is OnClickListener) {
                    return@runBefore
                }

                val t: LinearLayout = param.thisObject as LinearLayout
                val button: Button = t.findViewWithTag("buttonTile")
                button.setOnClickListener(param.args[0] as OnClickListener)
                button.setOnLongClickListener(param.args[1] as OnLongClickListener)
                param.result = null
            }
            c.hookMethod("onConfigurationChanged").runBefore { param ->
                param.result = null
            }
            c.hookMethod("resetOverride").runBefore { param ->
                param.result = null
            }
            c.hookMethod("setQsLogger").runBefore { param ->
                param.result = null
            }
            c.hookMethod("updateResources").runBefore { param ->
                param.result = null
            }
            c.hookMethod("updateLongPressEffectProperties").runBefore { param ->
                param.result = null
            }
            c.hookMethod("resetLongPressEffectProperties").runBefore { param ->
                param.result = null
            }
            c.hookMethod("onFocusChanged").runBefore { param ->
                param.result = null
            }
            c.hookMethod("handleStateChanged").runBefore { param ->
                val t: LinearLayout = param.thisObject as LinearLayout
                // val state: Int = param.args[0].getField("state") as Int
                val lab: String? = param.args[0].getFieldSilently("label") as String?

                val button: Button = t.findViewWithTag("buttonTile")
                button.text = lab
                param.result = null
            }
            c.hookMethod("onStateChanged").runBefore { param ->
                val t: LinearLayout = param.thisObject as LinearLayout
                val state: Int = param.args[0].getField("state") as Int
                val lab: String? = param.args[0].getFieldSilently("label") as String?
                // val icon: Object? = param.args[0].getFieldSilently("icon") as Object?
                // Not active state
                val defaultState: Int = param.args[0].getField("DEFAULT_STATE") as Int
                var colorActive = ACTIVE_TILE_LIGHT
                var color = INACTIVE_TILE_LIGHT
                if(isDarkMode(t.context)) {
                    color = INACTIVE_TILE
                    colorActive = ACTIVE_TILE
                }

                val button: Button = t.findViewWithTag("buttonTile")

                //(icon.callMethod("getDrawable", t.context) as Drawable)

                //button.setCompoundDrawables(, null, null, null)
                button.text = lab

                if(state == defaultState) {
                    button.setTextColor(Color.parseColor(color))
                } else {
                    button.setTextColor(Color.parseColor(colorActive))
                }

                param.result = null
            }
            c.hookMethod("setClickable").runBefore { param ->
                param.result = null
            }
        }


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