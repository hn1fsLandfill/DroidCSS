package eu.hn1f.droidcss

import android.annotation.SuppressLint
import android.content.Context
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
import android.widget.TextView
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
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

                val butt = Button(ContextThemeWrapper(t.context, android.R.style.Widget_Material_Button), null, android.R.style.Widget_Material_Button)
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

                val button: Button = t.findViewWithTag("buttonTile")

                //(icon.callMethod("getDrawable", t.context) as Drawable)

                //button.setCompoundDrawables(, null, null, null)
                button.text = lab

                if(state == defaultState) {
                    button.setTextColor(Color.BLACK)
                } else {
                    button.setTextColor(Color.parseColor("#ff33b5e5"))
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

        mSwitch.hookConstructor().runBefore { param ->
            Log.v("DroidCSS", "Forcing Framework Switch style")
            param.args[0] = ContextThemeWrapper(param.args[0] as Context, android.R.style.Widget_Material_CompoundButton_Switch)
            if(param.args.size > 2) {
                param.args[2] = android.R.style.Widget_Material_CompoundButton_Switch
            }
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