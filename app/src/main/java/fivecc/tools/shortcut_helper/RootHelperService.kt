@file:Suppress("DEPRECATION")

package fivecc.tools.shortcut_helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ILauncherApps
import android.content.pm.IShortcutService
import android.content.pm.ShortcutInfo
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.ServiceManager
import android.system.Os
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import com.topjohnwu.superuser.ipc.RootService
import fivecc.tools.shortcut_helper.utils.ShortcutParser
import fivecc.tools.shortcut_helper.utils.getShortcutInfoCompat

enum class ServiceState {
    STOPPED,
    STARTING,
    RUNNING
}

@Suppress("Unchecked_Cast")
class RootHelperService : RootService() {
    companion object {
        const val METHOD_PARSE_FILE = "parse_file"
        const val METHOD_SYSTEM_API = "system_api"
        val serviceState = MutableLiveData(ServiceState.STOPPED)
        var helper: IRootHelper? = null
            private set
        private val mConnection = object : ServiceConnection, IBinder.DeathRecipient {
            override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
                binder.linkToDeath(this, 0)
                helper = IRootHelper.Stub.asInterface(binder)
                serviceState.value = ServiceState.RUNNING
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                helper = null
                serviceState.value = ServiceState.STOPPED
            }

            override fun binderDied() {
                helper = null
                serviceState.postValue(ServiceState.STOPPED)
            }
        }

        @MainThread
        fun start() {
            if (serviceState.value == ServiceState.STOPPED) {
                bind(
                    Intent().setComponent(
                        ComponentName(
                            BuildConfig.APPLICATION_ID,
                            RootHelperService::class.java.name
                        )
                    ),
                    mConnection
                )
                serviceState.value = ServiceState.STARTING
            }
        }
    }

    private val shortcutService by lazy {
        IShortcutService.Stub.asInterface(ServiceManager.getService(Context.SHORTCUT_SERVICE))
    }

    private val launcherAppsService by lazy {
        ILauncherApps.Stub.asInterface(ServiceManager.getService(Context.LAUNCHER_APPS_SERVICE))
    }

    private val mHelper = object : IRootHelper.Stub() {
        override fun getShortcuts(method: String, user: Int, flags: Int): MutableList<ShortcutInfo>? {
            return when (method) {
                METHOD_PARSE_FILE -> {
                    ShortcutParser.loadUserLocked(user)
                }
                METHOD_SYSTEM_API -> {
                    val result = mutableListOf<ShortcutInfo>()
                    packageManager.getInstalledPackages(0).forEach {
                        result.addAll(shortcutService.getShortcutInfoCompat(it.packageName, user, flags))
                    }
                    result
                }
                else -> {
                    throw IllegalArgumentException("unknown method $method")
                }
            }
        }

        override fun getShortcutIconFd(
            packageName: String?,
            id: String?,
            userId: Int
        ): ParcelFileDescriptor {
            return launcherAppsService.getShortcutIconFd("android", packageName, id, userId)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Os.seteuid(1000)
    }

    override fun onBind(intent: Intent): IBinder = mHelper.asBinder()
}