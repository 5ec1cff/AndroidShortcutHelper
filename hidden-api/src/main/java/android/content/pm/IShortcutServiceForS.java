package android.content.pm;

import com.android.internal.infra.AndroidFuture;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(IShortcutService.class)
public interface IShortcutServiceForS {
    AndroidFuture<ParceledListSlice> getShortcuts(String packageName, int matchFlags, int userId);
}
