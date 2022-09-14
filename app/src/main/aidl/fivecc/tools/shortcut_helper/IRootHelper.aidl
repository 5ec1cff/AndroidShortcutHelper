// IRootHelper.aidl
package fivecc.tools.shortcut_helper;

import android.content.pm.ShortcutInfo;
import android.os.ParcelFileDescriptor;

// Declare any non-default types here with import statements

interface IRootHelper {
    String hello();
    List<ShortcutInfo> getShortcuts();
    IBinder getFS();
    ParcelFileDescriptor getShortcutIconFd(String packageName, String id, int userId);
}