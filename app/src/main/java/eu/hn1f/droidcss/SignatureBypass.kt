package eu.hn1f.droidcss

import android.util.Log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.XposedHook.Companion.findClass
import eu.hn1f.droidcss.utils.hookMethod

class SignatureBypass {
    fun onLoad(packageParam: XC_LoadPackage.LoadPackageParam) {
        val packageManagerServiceUtils = findClass("com.android.server.pm.PackageManagerServiceUtils")

        if(packageManagerServiceUtils != null) {
            // TODO: only allow apps inside a list to get bypassed signatures
            Log.v("DroidCSS", "Disabling signature verification")
            packageManagerServiceUtils.hookMethod("verifySignatures").runAfter { param ->
                val msg = param.throwable?.message
                if(msg != null) Log.v("DroidCSS", "throwable: $msg")
                if(msg != null && msg.contains("has no signatures that match")) {
                    Log.v("DroidCSS", "signatures are totally valid trust")
                    param.result = false;
                }
            }
        }
    }
}