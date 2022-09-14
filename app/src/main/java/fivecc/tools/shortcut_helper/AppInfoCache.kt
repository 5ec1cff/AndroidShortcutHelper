package fivecc.tools.shortcut_helper

import android.content.pm.ApplicationInfo
import java.util.concurrent.ConcurrentHashMap

object AppInfoCache {
    private val appInfoCache = ConcurrentHashMap<String, ApplicationInfo>()
    private val appLabelCache = ConcurrentHashMap<String, String>()

    fun getAppInfo(packageName: String): ApplicationInfo {
        appInfoCache[packageName]?.let { return it }
        val pm = App.instance.packageManager
        return pm.getApplicationInfo(packageName, 0).also { appInfoCache[packageName] = it }
    }

    fun getAppLabel(packageName: String): String {
        appLabelCache[packageName]?.let { return it }
        val pm = App.instance.packageManager
        val appInfo = getAppInfo(packageName)
        return appInfo.loadLabel(pm).toString().also { appLabelCache[packageName] = it }
    }
}