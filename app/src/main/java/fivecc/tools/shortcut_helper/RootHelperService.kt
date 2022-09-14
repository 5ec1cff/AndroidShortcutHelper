@file:Suppress("DEPRECATION")

package fivecc.tools.shortcut_helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ILauncherApps
import android.content.pm.IShortcutService
import android.content.pm.ShortcutInfo
import android.ddm.DdmHandleAppName
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.Process
import android.os.ServiceManager
import android.system.Os
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.FileSystemManager
import fivecc.tools.shortcut_helper.utils.ShortcutParser
import fivecc.tools.shortcut_helper.utils.getPinnedShortcutCompat

enum class ServiceState {
    STOPPED,
    STARTING,
    RUNNING
}

@Suppress("Unchecked_Cast")
class RootHelperService : RootService() {
    companion object {
        val serviceState = MutableLiveData(ServiceState.STOPPED)
        var helper: IRootHelper? = null
            private set
        var remoteFs: FileSystemManager? = null
            private set
        private val mConnection = object : ServiceConnection, IBinder.DeathRecipient {
            override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
                binder.linkToDeath(this, 0)
                helper = IRootHelper.Stub.asInterface(binder)
                remoteFs = FileSystemManager.getRemote(helper!!.fs)
                serviceState.value = ServiceState.RUNNING
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                helper = null
                remoteFs = null
                serviceState.value = ServiceState.STOPPED
            }

            override fun binderDied() {
                helper = null
                remoteFs = null
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
        override fun hello(): String {
            return "hello from ${Process.myPid()}:${Process.myUid()}"
        }

        override fun getShortcuts(): MutableList<ShortcutInfo>? = getShortcutsImpl()

        override fun getFS(): IBinder = FileSystemManager.getService()

        override fun getShortcutIconFd(
            packageName: String?,
            id: String?,
            userId: Int
        ): ParcelFileDescriptor {
            return launcherAppsService.getShortcutIconFd("android", packageName, id, userId)
        }
    }

    private fun getShortcutsImpl(): MutableList<ShortcutInfo>? {
        return ShortcutParser.loadUserLocked(0)
    }

    override fun onCreate() {
        super.onCreate()
        Os.seteuid(1000)
    }

    override fun onBind(intent: Intent): IBinder = mHelper.asBinder()
}