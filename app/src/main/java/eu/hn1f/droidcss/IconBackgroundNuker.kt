package eu.hn1f.droidcss

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.getField
import eu.hn1f.droidcss.utils.getFieldSilently
import eu.hn1f.droidcss.utils.hookMethod

// Remove backgrounds from app icons, WIP
class IconBackgroundNuker {
    fun onLoad(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val adaptiveIconDrawable = findClass("android.graphics.drawable.AdaptiveIconDrawable")

        adaptiveIconDrawable.hookMethod("draw").runBefore { param ->
            val root = param.thisObject as Object
            val child = root.getField("mLayerState").getField("mChildren") as Array<Object>
            // child[0].setField("mDrawable", null)
            val foreground = (child[1].getField("mDrawable") as Drawable)
            val monochrome = (child[2].getFieldSilently("mDrawable") as Drawable?)


            // TODO: Allow setting this per app
            if(monochrome != null) {
                monochrome.setTint(Color.BLUE);
                monochrome.draw(param.args[0] as Canvas)
            } else {
                foreground.draw(param.args[0] as Canvas);
            }

            param.result = null;
            Log.v("DroidCSS", "adaptive draw");
        }
    }
}