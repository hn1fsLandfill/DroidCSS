package eu.hn1f.droidcss

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Color
import android.graphics.PorterDuff.Mode
import android.os.Build
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.callMethod
import eu.hn1f.droidcss.utils.callMethodSilently
import eu.hn1f.droidcss.utils.hookConstructor
import eu.hn1f.droidcss.utils.hookMethod
import eu.hn1f.droidcss.utils.setFieldSilently

fun isDarkMode(context: Context): Boolean {
    val darkModeFlag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
}

fun getTheme(darkMode: Boolean, themeName: String): Int? {
    val theme = if(darkMode)
        when (themeName) {
            "Vintage" -> android.R.style.Theme
            "Vintage.NoTitleBar" -> android.R.style.Theme_NoTitleBar
            "Holo" -> android.R.style.Theme_Holo
            "Holo.NoActionBar" -> android.R.style.Theme_Holo_NoActionBar
            "Material" -> android.R.style.Theme_Material
            "Material.NoActionBar" -> android.R.style.Theme_Material_NoActionBar
            else -> null
        }
    else
        when (themeName) {
            "Vintage" -> android.R.style.Theme_Light
            "Vintage.NoTitleBar" -> android.R.style.Theme_Light_NoTitleBar
            "Holo" -> android.R.style.Theme_Holo_Light
            "Holo.NoActionBar" -> android.R.style.Theme_Holo_Light_NoActionBar
            "Material" -> android.R.style.Theme_Material_Light
            "Material.NoActionBar" -> android.R.style.Theme_Material_Light_NoActionBar
            else -> null
        }

    return theme
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
    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam, settings: XSharedPreferences) {
        //val c = findClass("com.android.settings")
        //if (c != null) {
            /* TODO */
        //}
        // SignatureBypass().onLoad(loadPackageParam)

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

        // com/google/android/material/dialog/MaterialAlertDialogBuilder
        val mDialog = findClass("com.google.android.material.dialog.MaterialAlertDialogBuilder")
        val compatDialog = findClass("androidx.appcompat.app.AlertDialog.Builder")

        // We can proxy this one according to Material Component's documentation
        // "The type of dialog returned is still an AlertDialog; there is no specific Material
        // implementation of AlertDialog."
        mDialog.hookConstructor().runBefore { param ->
            Log.v("DroidCSS", "Attempt to use MaterialDialog, forcing AppCompat Dialog")
            var theme = R.style.Theme_Material_Light_Dialog_NoActionBar

            if(isDarkMode(param.args[0] as Context)) {
                Log.v("DroidCSS","Using Dark Dialog")
                theme = R.style.Theme_Material_Dialog_NoActionBar
            }

            param.args[0] = ContextThemeWrapper(param.args[0] as Context, theme)

            if (compatDialog != null) {
                param.result = compatDialog.constructors[0].newInstance(param.args[0])
            }
        }

        val mButton = findClass("com.google.android.material.button.MaterialButton")

        mButton.hookConstructor().runAfter { param ->
            Log.v("DroidCSS", "Attempt to use MaterialButton, forcing framework Button Theme")
            var theme = BUTTON_THEME_LIGHT
            if(isDarkMode(param.args[0] as Context)) {
                Log.v("DroidCSS","Using Dark button")
                theme = BUTTON_THEME
            }

            val db = Button(param.args[0] as Context, null, 0, 0)
            val b = param.thisObject as Button

            b.background = db.background
            b.backgroundTintList = db.backgroundTintList
            b.backgroundTintMode = db.backgroundTintMode
            b.backgroundTintBlendMode = db.backgroundTintBlendMode
            if(Build.VERSION.SDK_INT > 34) {
                b.highlights = db.highlights
            }
            b.highlightColor = db.highlightColor
            b.foreground = db.foreground
            b.foregroundTintList = db.foregroundTintList
            b.foregroundTintMode = db.foregroundTintMode
            b.foregroundTintBlendMode = db.foregroundTintBlendMode
            try {
                b.setTextColor(db.textColors)
                b.setHintTextColor(db.hintTextColors)
                b.setLinkTextColor(db.linkTextColors)
            } catch (ignored: Exception) {}
        }

        mButton.hookMethod("setShapeAppearanceModel").runBefore { param ->
            param.result = null
        }

        val themeName = settings.getString("theme_${loadPackageParam.packageName}", "Default")

        if(themeName != "Default") {
            val activity = findClass("android.app.Activity")

            activity.hookMethod("onCreate").runBefore { param ->
                val act = param.thisObject as Activity
                val theme = getTheme(isDarkMode(act), themeName!!)
                if(theme != null) act.setTheme(theme)
            }.runAfter { param ->
                val act = param.thisObject as Activity
                val theme = getTheme(isDarkMode(act), themeName!!)
                if(theme != null) act.setTheme(theme)
            }

            val materialThemeOverlay = findClass("com.google.android.material.theme.overlay.MaterialThemeOverlay")
            materialThemeOverlay.hookMethod("wrap").runBefore { param ->
                val theme = getTheme(isDarkMode(param.args[0] as Context), themeName!!)
                if(theme != null) param.result = ContextThemeWrapper(param.args[0] as Context, theme)
            }

            val typedArray = findClass("android.content.res.TypedArray")
            typedArray.hookMethod("getDimensionPixelSize").runAfter { param ->
                if(param.hasThrowable()) {
                    param.throwable = null
                    param.result = 0
                }
            }
            typedArray.hookMethod("getDimensionPixelOffset").runAfter { param ->
                if(param.hasThrowable()) {
                    param.throwable = null
                    param.result = 0
                }
            }

            val mSwitch = findClass("com.google.android.material.materialswitch.MaterialSwitch")
            //val switchCompat = findClass("androidx.appcompat.widget")

            mSwitch.hookConstructor().runBefore { param ->
                Log.v("DroidCSS", "Attempt to use MaterialSwitch, forcing framework Switch Theme")
                var context = param.args[0] as Context
                var theme = getTheme(isDarkMode(context), themeName!!)

                if(theme != null) {
                    val switchStyle = ContextThemeWrapper(context, theme).obtainStyledAttributes(arrayOf(
                        androidStyleableAttrs.switchStyle
                    ).toIntArray())

                    theme = switchStyle.getResourceId(0, 0)
                    context = ContextThemeWrapper(context, theme)
                    param.args[0] = context
                    switchStyle.recycle()

                    if (param.args.size > 3)
                        param.args[3] = theme
                }
            }.runAfter { param ->
                val s: CompoundButton = param.thisObject as CompoundButton
                Log.v("DroidCSS", "Cleaning up MaterialSwitch mess")
                // switchStyle
                val switchStyle = s.context.obtainStyledAttributes(arrayOf(
                    androidStyleableAttrs.track,
                    androidStyleableAttrs.thumb,
                    androidStyleableAttrs.switchTextAppearance,
                    androidStyleableAttrs.background,
                    androidStyleableAttrs.showText,
                    androidStyleableAttrs.switchPadding,
                    androidStyleableAttrs.switchMinWidth,
                    androidStyleableAttrs.thumbTextPadding,
                    androidStyleableAttrs.textOn,
                    androidStyleableAttrs.textOff
                ).toIntArray())

                s.callMethodSilently("setTrackDrawable", switchStyle.getDrawable(0))
                s.callMethodSilently("setThumbDrawable", switchStyle.getDrawable(1))
                s.setBackgroundResource(switchStyle.getResourceId(3, 0))

                val switchPadding = switchStyle.getDimension(5, 0f).toInt()
                s.callMethodSilently("setSwitchPadding", switchPadding)
                s.setFieldSilently("mSwitchPadding", switchPadding)

                val switchMinWidth = switchStyle.getDimension(6, 0f).toInt()
                s.callMethodSilently("setSwitchMinWidth", switchMinWidth)
                s.setFieldSilently("mSwitchMinWidth", switchMinWidth)

                val thumbTextPadding = switchStyle.getDimension(7, 0f).toInt()
                s.callMethodSilently("setThumbTextPadding", thumbTextPadding)
                s.setFieldSilently("mThumbTextPadding", thumbTextPadding)

                val textOn = switchStyle.getString(8)
                s.callMethodSilently("setTextOn", textOn)
                s.setFieldSilently("mTextOn", textOn)

                val textOff = switchStyle.getString(9)
                s.callMethodSilently("setTextOff", textOff)
                s.setFieldSilently("mTextOff", textOff)

                var showText = switchStyle.getBoolean(4, false)
                // workaround: showtext doesn't work sometimes
                if(themeName!!.contains("Holo"))
                    showText = true

                s.callMethodSilently("setShowText", showText)
                s.setFieldSilently("mShowText", showText)

                s.setTextAppearance(switchStyle.getResourceId(2, 0))
                s.callMethodSilently("setSwitchTextAppearance",
                    s.context, switchStyle.getResourceId(2, 0))

                switchStyle.recycle()
            }
        }
        val funPolice = findClass("com.google.android.material.internal.ThemeEnforcement")
        funPolice.hookMethod("checkTheme").runBefore { param ->
            Log.v("DroidCSS", "Stopped the fun police from crashing the app")
            param.result = null
        }

        Log.v("DroidCSS", "Hooked into app mrrp~~ :3")
    }
}