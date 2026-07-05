package eu.hn1f.droidcss

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.callMethod
import eu.hn1f.droidcss.utils.findMethod
import eu.hn1f.droidcss.utils.getField
import eu.hn1f.droidcss.utils.hookConstructor
import eu.hn1f.droidcss.utils.hookMethod
import eu.hn1f.droidcss.utils.runBefore
import eu.hn1f.droidcss.utils.setField
import java.util.Objects

// FIXME: Framework-like preference UI
// FIXME: Disable the dumbass billboard

class Settings {

    @Suppress("UNUSED_PARAMETER")
    @SuppressLint("SetTextI18n")
    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam, settings: XSharedPreferences) {
        val themeName = settings.getString("theme_${loadPackageParam.packageName}", "Default")

        val androidxPreference = findClass("androidx.preference.Preference")
        androidxPreference.hookConstructor().runBefore { param ->
            val mContext = param.args[0] as Context

            if(themeName != "Default") {
                val themeId = getTheme(isDarkMode(mContext), themeName!!)

                if(themeId != null) mContext.setTheme(themeId)
            }
        }
        androidxPreference.hookMethod("onBindViewHolder").runAfter { param ->
            val holder = param.args[0] as Object
            val itemView = holder.getField("itemView") as View
        }

        val roundCornerPreferenceAdapter = findClass("com.android.settings.core.RoundCornerPreferenceAdapter")
        roundCornerPreferenceAdapter.hookMethod("updateBackground").runBefore { param ->
            param.result = null
        }

        val restrictedCategory = findClass("com.android.settings.widget.HomepagePreferenceLayoutHelper")
        restrictedCategory.hookMethod("onBindViewHolder").runBefore { param ->
            val holder = param.args[0] as Object
            val itemView = holder.getField("itemView") as ViewGroup

            val selectedBg = itemView.context.obtainStyledAttributes(arrayOf(
                androidStyleableAttrs.selectableItemBackground
            ).toIntArray())
            itemView.background = selectedBg.getDrawable(0)
            holder.setField("mBackground", itemView.background)
            itemView.setPadding(0,0,0,0)
            selectedBg.recycle()

            Log.v("DroidCSS", "hook onBindViewHolder")
            param.result = null
        }

        // Turn the expandable Material3 AppBar into a static brick like on older Material versions
        val appBarLayout = findClass("com.google.android.material.appbar.AppBarLayout")
        appBarLayout.hookConstructor().runAfter { param ->
            val ts = param.thisObject as ViewGroup
            ts.callMethod("setExpanded", false, false)
        }
        appBarLayout.hookMethod("setExpanded").runBefore { param ->
            param.args[0] = false
            param.args[1] = false
        }

        val appBarLayoutBehavior = findClass("com.google.android.material.appbar.AppBarLayout.Behavior")
        appBarLayoutBehavior.hookMethod("onNestedPreScroll").runBefore { param -> param.result = null }
        appBarLayoutBehavior.hookMethod("onNestedScroll").runBefore { param -> param.result = null }

        Log.v("DroidCSS", "Hello Settings mrrp~~ :3")
    }
}