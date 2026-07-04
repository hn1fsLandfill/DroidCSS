package eu.hn1f.droidcss

import android.content.res.XResources
import eu.hn1f.droidcss.utils.XposedHook
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import eu.hn1f.droidcss.utils.HookRes


class Main: IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {
    private val hookRes = HookRes()
    private val sys = SysUI()
    //private val hookEntry = HookEntry()

    override fun handleInitPackageResources(initPackageResourcesParam: XC_InitPackageResources.InitPackageResourcesParam) {
        /*hookRes.handleInitPackageResources(initPackageResourcesParam)
        if(initPackageResourcesParam.packageName.contains("systemui")) {
            sys.onResources(initPackageResourcesParam)
        } */
    }

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        // We don't wanna hook ourselves.
        if(loadPackageParam.packageName == "eu.hn1f.droidcss") return;

        XposedHook.init(loadPackageParam)
        val settings = XSharedPreferences("eu.hn1f.droidcss", "SomeSettings")

        if(settings.file.canRead()) {
            REDIRECT_SYSTEMUI = settings.getBoolean("redirSysUI", false)
            REMOVE_ICONBGS = settings.getBoolean("removeIconBGs", true)
            HOLO_FRAMEWORK_DIALOGS = settings.getBoolean("holoFrameworkDialogs", false)
        }

        if(loadPackageParam.packageName.equals("android")) {
            Framework().onLoad(loadPackageParam)
            return
        }
        Universial().onLoad(loadPackageParam, settings)
        IconBackgroundNuker().onLoad(loadPackageParam, settings)
        if(loadPackageParam.packageName.contains("systemui") || loadPackageParam.packageName.contains("SystemUI")) {
            sys.onLoad(loadPackageParam)
        } else if(loadPackageParam.packageName.contains("com.android.settings")) {
            Settings().onLoad(loadPackageParam, settings)
        }
        //hookEntry.handleLoadPackage(loadPackageParam)
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        hookRes.initZygote(startupParam)
    }
}