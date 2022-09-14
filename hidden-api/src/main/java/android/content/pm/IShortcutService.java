package android.content.pm;

import android.os.Binder;
import android.os.IBinder;

public interface IShortcutService {
    // API 29 and below
    ParceledListSlice getPinnedShortcuts(String packageName, int userId);
    // API 30
    ParceledListSlice getShortcuts(String packageName, int matchFlags, int userId);

    class Stub extends Binder {
        public static IShortcutService asInterface(IBinder b) {
            throw new UnsupportedOperationException("STUB!");
        }
    }
}
