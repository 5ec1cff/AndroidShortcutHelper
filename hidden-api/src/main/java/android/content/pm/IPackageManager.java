package android.content.pm;

import android.content.ComponentName;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

import java.util.List;


public interface IPackageManager extends IInterface {
    String[] getPackagesForUid(int uid);
    int getPackageUid(String packageName, int flags, int userId);

    void addPreferredActivity(IntentFilter filter, int match,
                              ComponentName[] set, ComponentName activity, int userId);

    void replacePreferredActivity(IntentFilter filter, int match,
                              ComponentName[] set, ComponentName activity, int userId);

    void clearPackagePreferredActivities(String packageName);

    int getPreferredActivities(List<IntentFilter> outFilters,
                               List<ComponentName> outActivities, String packageName);

    class Stub extends Binder {
        public static IPackageManager asInterface(IBinder b) {
            throw new UnsupportedOperationException("STUB!");
        }
    }
}
