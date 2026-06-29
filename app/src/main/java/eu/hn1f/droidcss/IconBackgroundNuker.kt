package eu.hn1f.droidcss

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.callMethod
import eu.hn1f.droidcss.utils.getField
import eu.hn1f.droidcss.utils.getFieldSilently
import eu.hn1f.droidcss.utils.hookMethod

// Remove backgrounds from app icons, WIP
class IconBackgroundNuker {
    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam, settings: XSharedPreferences) {
        if(!REMOVE_ICONBGS) return;
        var appID = "";
        val icon = findClass("android.graphics.drawable.Icon")
        icon.hookMethod("loadDrawableInner").runBefore { param ->
            appID = (param.thisObject as Object).callMethod("getResPackage") as String;
        }

        val adaptiveIconDrawable = findClass("android.graphics.drawable.AdaptiveIconDrawable")

        adaptiveIconDrawable.hookMethod("inflate").runBefore { param ->
            val res = param.args[0] as Resources;
            val attrs = param.args[2] as AttributeSet

            appID = res.getResourcePackageName(
                Resources.getAttributeSetSourceResId(attrs)
            )
        }

        adaptiveIconDrawable.hookMethod("draw").runBefore { param ->
            val root = param.thisObject as Object
            if(settings.getBoolean("forceintoiconjail_$appID", false)) {
                return@runBefore
            }

            val tintIcon = settings.getBoolean("tinticon_$appID", false)
            val tintColor = settings.getInt("tintcolor_$appID", Color.WHITE)

            val child = root.getField("mLayerState").getField("mChildren") as Array<Object>
            // child[0].setField("mDrawable", null)
            val foreground = (child[1].getField("mDrawable") as Drawable)
            val monochrome = (child[2].getFieldSilently("mDrawable") as Drawable?)

            // TODO: Allow setting this per app
            if(monochrome != null && tintIcon) {
                monochrome.setTint(tintColor)
                monochrome.draw(param.args[0] as Canvas)
            } else {
                if(tintColor != Color.WHITE && tintIcon) foreground.setTint(tintColor)
                foreground.draw(param.args[0] as Canvas)
            }

            param.result = null;
        }
    }
}