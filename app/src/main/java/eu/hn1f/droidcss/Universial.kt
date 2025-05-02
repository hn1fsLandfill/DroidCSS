package eu.hn1f.droidcss

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Color
import android.graphics.PorterDuff.Mode
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.callMethod
import eu.hn1f.droidcss.utils.callMethodSilently
import eu.hn1f.droidcss.utils.hookConstructor
import eu.hn1f.droidcss.utils.hookMethod

fun isDarkMode(context: Context): Boolean {
    val darkModeFlag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
}

@SuppressLint("DiscouragedApi")
class Universial {
    private fun getAppCompat(themeName: String, resId: Int, res: Resources): Int {
        val newThemeName = themeName
            .replace("GoogleMaterial3","Material3")
            .replace("Material3.","AppCompat.")
            .replace("DynamicColors.", "")
            .replace(".TextButton", "")
            .replace(".TonalButton","")
            .replace(".DarkActionBar",".ActionBar")
            .replace(".MaterialAlertDialog",".AlertDialog")

        var id = res.getIdentifier(newThemeName, res.getResourceTypeName(resId), res.getResourcePackageName(resId))

        if(id == 0) {
            id = res.getIdentifier(
                themeName.replace("ThemeOverlay","Theme"), res.getResourceTypeName(resId), res.getResourcePackageName(resId)
            )
        }

        return id
    }

    @SuppressLint("SetTextI18n")
    fun onLoad(@Suppress("UNUSED_PARAMETER") loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        //val c = findClass("com.android.settings")
        //if (c != null) {
            /* TODO */
        //}
        val resTheme = findClass("android.content.res.Resources.Theme")

        resTheme.hookMethod("applyStyle").runBefore { param ->
            val c: Theme = param.thisObject as Theme
            if((param.args[0] as Int) == 0) {
                return@runBefore
            }
            val t = c.resources.getResourceEntryName(param.args[0] as Int)
            Log.v("DroidCSS","Attempt to apply $t")

            if(t.contains("MainActivityTheme")) {
                Log.v("DroidCSS","Forcing AppCompat on Activity Theme")
                val k = c.resources.getIdentifier("Theme.AppCompat.DayNight", c.resources.getResourceTypeName(param.args[0] as Int), c.resources.getResourcePackageName(param.args[0] as Int))
                if(k != 0) {
                    Log.v("DroidCSS","Success force")
                    param.args[0] = k
                }
            } else if(t.contains("Material3.")) {
                val k = getAppCompat(t, param.args[0] as Int, c.resources)
                if(k != 0) {
                    Log.v("DroidCSS","Successfully replaced Material3 theme!")
                    param.args[0] = k
                }
            }
        }

        resTheme.hookMethod("setTo").runBefore { param ->
            val c: Theme = param.thisObject as Theme
            val c2: Theme = param.args[0] as Theme
            /*if( == 0) {
                return@runBefore
            } */
            //c2.
            val t = c.resources.getResourceEntryName(c2.callMethod("getAppliedStyleResId") as Int)
            Log.v("DroidCSS","Attempt to set style $t")

            if(t.contains("Material3.")) {
                val r = getAppCompat(t, c2.callMethod("getAppliedStyleResId") as Int, c2.resources)

                if(r != 0) {
                    Log.v("DroidCSS", "Successfully replaced Material3")
                    param.result = c.applyStyle(r, true)
                }
            }
        }

        val mSwitch = findClass("com.google.android.material.materialswitch.MaterialSwitch")
        //val switchCompat = findClass("androidx.appcompat.widget")

        mSwitch.hookConstructor().runBefore { param ->
            Log.v("DroidCSS", "Attempt to use MaterialSwitch, forcing framework Switch Theme")
            var theme = android.R.style.Widget_Material_Light_CompoundButton_Switch

            if(isDarkMode(param.args[0] as Context)) {
                Log.v("DroidCSS","Using Dark switch")
                theme = android.R.style.Widget_Material_CompoundButton_Switch
            }

            param.args[0] = ContextThemeWrapper(param.args[0] as Context, theme)

            if(param.args.size > 2) {
                param.args[2] = theme
            }
        }.runAfter { param ->
            val s: CompoundButton = param.thisObject as CompoundButton
            //val ds: CompoundButton = switchCompat.callMethod("SwitchCompat", param.args[0] as Context) as CompoundButton
            val ds = Switch(param.args[0] as Context)
            Log.v("DroidCSS", "Cleaning up MaterialSwitch mess")

            s.callMethod("setTrackDrawable",ds.trackDrawable)

            s.callMethodSilently("setShowText", false)
            s.callMethodSilently("setTrackDecorationTintList", ColorStateList.valueOf(Color.parseColor("#00FFFFFF")))
            s.callMethodSilently("setTrackDecorationTintMode", Mode.SRC_IN)
        }

        // com/google/android/material/dialog/MaterialAlertDialogBuilder
        val mDialog = findClass("com.google.android.material.dialog.MaterialAlertDialogBuilder")
        val compatDialog = findClass("androidx.appcompat.app.AlertDialog.Builder")

        // We can proxy this one according to Material Component's documentation
        // "The type of dialog returned is still an AlertDialog; there is no specific Material
        // implementation of AlertDialog."
        mDialog.hookConstructor().runBefore { param ->
            Log.v("DroidCSS", "Attempt to use MaterialDialog, forcing AppCompat Dialog")
            var theme = android.R.style.Theme_Material_Light_Dialog_NoActionBar

            if(isDarkMode(param.args[0] as Context)) {
                Log.v("DroidCSS","Using Dark Dialog")
                theme = android.R.style.Theme_Material_Dialog_NoActionBar
            }

            param.args[0] = ContextThemeWrapper(param.args[0] as Context, theme)

            if (compatDialog != null) {
                param.result = compatDialog.constructors[0].newInstance(param.args[0])
            }
        }

        val mButton = findClass("com.google.android.material.button.MaterialButton")

        mButton.hookConstructor().runAfter { param ->
            Log.v("DroidCSS", "Attempt to use MaterialButton, forcing framework Button Theme")
            var theme = android.R.style.Widget_Material_Light_Button
            if(isDarkMode(param.args[0] as Context)) {
                Log.v("DroidCSS","Using Dark button")
                theme = android.R.style.Widget_Material_Button
            }

            val db = Button(ContextThemeWrapper(param.args[0] as Context, theme))
            val b = param.thisObject as Button

            b.background = db.background
            b.backgroundTintList = db.backgroundTintList
            b.backgroundTintMode = db.backgroundTintMode
            b.backgroundTintBlendMode = db.backgroundTintBlendMode
            if(android.os.Build.VERSION.SDK_INT > 34) {
                b.highlights = db.highlights
            }
            b.highlightColor = db.highlightColor
            b.foreground = db.foreground
            b.foregroundTintList = db.foregroundTintList
            b.foregroundTintMode = db.foregroundTintMode
            b.foregroundTintBlendMode = db.foregroundTintBlendMode
            b.setTextColor(db.textColors)
            b.setHintTextColor(db.hintTextColors)
            b.setLinkTextColor(db.linkTextColors)
        }

        mButton.hookMethod("setShapeAppearanceModel").runBefore { param ->
            param.result = null
        }

        Log.v("DroidCSS", "Hooked into app mrrp~~ :3")
    }
}