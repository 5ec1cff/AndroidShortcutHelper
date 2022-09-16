// IRootHelper.aidl
package fivecc.tools.shortcut_helper;

import android.content.pm.ShortcutInfo;
import android.os.ParcelFileDescriptor;

// Declare any non-default types here with import statements

interface IRootHelper {
    List<ShortcutInfo> getShortcuts(String method, int user, int flags);
    ParcelFileDescriptor getShortcutIconFd(String packageName, String id, int userId);
    void startShortcut(in ShortcutInfo shortcutInfo);
}