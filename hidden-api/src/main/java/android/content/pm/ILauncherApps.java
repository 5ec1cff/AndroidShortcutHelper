package android.content.pm;

import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import androidx.annotation.RequiresApi;

public interface ILauncherApps {
    ParcelFileDescriptor getShortcutIconFd(String callingPackage, String packageName, String id,
                                           int userId);

    @RequiresApi(Build.VERSION_CODES.R)
    boolean startShortcut(String callingPackage, String packageName, String featureId, String id,
                          Rect sourceBounds, Bundle startActivityOptions, int userId);

    boolean startShortcut(String callingPackage, String packageName, String id,
                          Rect sourceBounds, Bundle startActivityOptions, int userId);

    class Stub extends Binder {
        public static ILauncherApps asInterface(IBinder b) {
            throw new UnsupportedOperationException("STUB!");
        }
    }
}
