package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

public interface ILauncherApps {
    ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id,
                                           int userId);

    class Stub extends Binder {
        public static ILauncherApps asInterface(IBinder b) {
            throw new UnsupportedOperationException("STUB!");
        }
    }
}
