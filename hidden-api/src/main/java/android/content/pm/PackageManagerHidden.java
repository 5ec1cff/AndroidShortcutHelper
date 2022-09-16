package android.content.pm;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.List;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(PackageManager.class)
public abstract class PackageManagerHidden {
    public abstract List<PackageInfo> getInstalledPackagesAsUser(int flags,
                                                                 int userId);

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public List<PackageInfo> getInstalledPackagesAsUser(@NonNull PackageManager.PackageInfoFlags flags,
                                                        int userId) {
        throw new UnsupportedOperationException(
                "getApplicationInfoAsUser not implemented in subclass");
    }
}
