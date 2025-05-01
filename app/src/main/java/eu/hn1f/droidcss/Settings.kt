package eu.hn1f.droidcss

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AndroidAppHelper
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.widget.TextView
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.callMethod
import eu.hn1f.droidcss.utils.dumpClass
import eu.hn1f.droidcss.utils.hookConstructor
import eu.hn1f.droidcss.utils.hookMethod

class Settings {

    @SuppressLint("SetTextI18n")
    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        //val c = findClass("com.android.settings")
        //if (c != null) {
            /* TODO */
        //}
        /*val androidContext = findClass("android.content.ContextWrapper")
        androidContext.hookMethod("setTheme").runBefore { param ->
            val c: Context = param.thisObject as Context
            var t = c.resources.getResourceEntryName(param.args[0] as Int)

            Log.v("DroidCSS","Attempt to look up $t")
            if(t.contains("Material3.")) {
                Log.v("DroidCSS","Material 3 detected, attempting to replace resource with AppCompat counterpart")
                t = t.replace("Material3.","AppCompat.")
                val resId = c.resources.getIdentifier(t, c.resources.getResourceTypeName(param.args[0] as Int), c.resources.getResourcePackageName(param.args[0] as Int))
                if(resId != 0) {
                    param.args[0] = resId
                }
            }
        }

        val androidContextTheme = findClass("android.view.ContextThemeWrapper")
        androidContextTheme.hookMethod("setTheme").runBefore { param ->
            val c: Context = param.thisObject as Context
            var t = c.resources.getResourceEntryName(param.args[0] as Int)

            Log.v("DroidCSS","Attempt to look up $t")
            if(t.contains("Material3.")) {
                Log.v("DroidCSS","Material 3 detected, attempting to replace resource with AppCompat counterpart")
                t = t.replace("Material3.","AppCompat.")
                val resId = c.resources.getIdentifier(t, c.resources.getResourceTypeName(param.args[0] as Int), c.resources.getResourcePackageName(param.args[0] as Int))
                if(resId != 0) {
                    param.args[0] = resId
                }
            }
        }

        androidContextTheme.hookConstructor().runAfter { param ->
            var c: Context = param.thisObject as Context
            if((param.args[1] as Int) == 0) {
                return@runAfter;
            }
            var t = c.resources.getResourceEntryName(param.args[1] as Int)

            Log.v("DroidCSS","Attempt to look up $t")
            if(t.contains("Material3.")) {
                Log.v("DroidCSS","Material 3 detected, attempting to replace resource with AppCompat counterpart")
                t = t.replace("Material3.","AppCompat.")
                val resId = c.resources.getIdentifier(t, c.resources.getResourceTypeName(param.args[0] as Int), c.resources.getResourcePackageName(param.args[0] as Int))
                if(resId != 0) {
                    param.args[0] = resId
                }
            }
        } */

        /*resTheme.hookConstructor().runBefore { param ->
            val c: Context = param.args[0] as Context
            Log.v("DroidCSS","Switchy thingy")

            param.args[0] = ContextThemeWrapper(c,android.R.style.Widget_Material_CompoundButton_Switch)
            if(param.args[2] != null) {
                param.args[2] = android.R.style.Widget_Material_CompoundButton_Switch
            }
        }

        val res = findClass("android.content.res.Resources")
        res.hookMethod("getIdentifier").runBefore { param ->
            val t = param.args[0] as String

            Log.v("DroidCSS","Attempt to obtain $t")
        } */


        Log.v("DroidCSS", "Hello Settings mrrp~~ :3")
    }
}