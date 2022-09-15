package android.content.pm;

import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

@SuppressWarnings("rawtypes")
public interface IShortcutService {
    // API 29 and below
    ParceledListSlice getPinnedShortcuts(String packageName, int userId);
    ParceledListSlice getDynamicShortcuts(String packageName, int userId);
    ParceledListSlice getManifestShortcuts(String packageName, int userId);

    // API 30 and above
    @RequiresApi(Build.VERSION_CODES.R)
    ParceledListSlice getShortcuts(String packageName, int matchFlags, int userId);

    class Stub extends Binder {
        public static IShortcutService asInterface(IBinder b) {
            throw new UnsupportedOperationException("STUB!");
        }
    }
}
