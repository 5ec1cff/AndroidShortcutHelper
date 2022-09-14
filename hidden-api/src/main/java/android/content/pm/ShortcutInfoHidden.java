package android.content.pm;

import android.app.Person;
import android.content.ComponentName;
import android.content.Intent;
import android.content.LocusId;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.PersistableBundle;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(ShortcutInfo.class)
public class ShortcutInfoHidden {
    public String getBitmapPath() {
        throw new RuntimeException("");
    }

    public String toInsecureString() {
        throw new RuntimeException("");
    }

    public static Intent setIntentExtras(Intent intent, PersistableBundle extras) {
        if (extras == null) {
            intent.replaceExtras((Bundle) null);
        } else {
            intent.replaceExtras(new Bundle(extras));
        }
        return intent;
    }

    public boolean hasIconUri() {
        throw new RuntimeException("");
    }

    public boolean hasIconFile() {
        throw new RuntimeException("");
    }

    public boolean hasIconResource() {
        throw new RuntimeException("");
    }

    public boolean hasAdaptiveBitmap() {
        throw new RuntimeException("");
    }

    public int getIconResourceId() {
        throw new RuntimeException("");
    }

    public int getShortLabelResourceId()  {
        throw new RuntimeException("");
    }

    public int getLongLabelResourceId() {
        throw new RuntimeException("");
    }

    // API 27 and below
    public ShortcutInfoHidden(
            int userId, String id, String packageName, ComponentName activity,
            Icon icon, CharSequence title, int titleResId, String titleResName,
            CharSequence text, int textResId, String textResName,
            CharSequence disabledMessage, int disabledMessageResId, String disabledMessageResName,
            Set<String> categories, Intent[] intentsWithExtras, int rank, PersistableBundle extras,
            long lastChangedTimestamp,
            int flags, int iconResId, String iconResName, String bitmapPath) {}

    // API 28
    public ShortcutInfoHidden(
            int userId, String id, String packageName, ComponentName activity,
            Icon icon, CharSequence title, int titleResId, String titleResName,
            CharSequence text, int textResId, String textResName,
            CharSequence disabledMessage, int disabledMessageResId, String disabledMessageResName,
            Set<String> categories, Intent[] intentsWithExtras, int rank, PersistableBundle extras,
            long lastChangedTimestamp,
            int flags, int iconResId, String iconResName, String bitmapPath, int disabledReason) {}

    // API 29 (Q)
    public ShortcutInfoHidden(
            int userId, String id, String packageName, ComponentName activity,
            Icon icon, CharSequence title, int titleResId, String titleResName,
            CharSequence text, int textResId, String textResName,
            CharSequence disabledMessage, int disabledMessageResId, String disabledMessageResName,
            Set<String> categories, Intent[] intentsWithExtras, int rank, PersistableBundle extras,
            long lastChangedTimestamp,
            int flags, int iconResId, String iconResName, String bitmapPath, int disabledReason,
            Person[] persons, LocusId locusId) {
    }

    // API 30 (R)
    public ShortcutInfoHidden(
            int userId, String id, String packageName, ComponentName activity,
            Icon icon, CharSequence title, int titleResId, String titleResName,
            CharSequence text, int textResId, String textResName,
            CharSequence disabledMessage, int disabledMessageResId, String disabledMessageResName,
            Set<String> categories, Intent[] intentsWithExtras, int rank, PersistableBundle extras,
            long lastChangedTimestamp,
            int flags, int iconResId, String iconResName, String bitmapPath, String iconUri,
            int disabledReason, Person[] persons, LocusId locusId) {
        throw new RuntimeException("");
    }

    // API 31 (S)
    public ShortcutInfoHidden(
            int userId, String id, String packageName, ComponentName activity,
            Icon icon, CharSequence title, int titleResId, String titleResName,
            CharSequence text, int textResId, String textResName,
            CharSequence disabledMessage, int disabledMessageResId, String disabledMessageResName,
            Set<String> categories, Intent[] intentsWithExtras, int rank, PersistableBundle extras,
            long lastChangedTimestamp,
            int flags, int iconResId, String iconResName, String bitmapPath, String iconUri,
            int disabledReason, Person[] persons, LocusId locusId,
            @Nullable String startingThemeResName,
            @Nullable Map<String, Map<String, List<String>>> capabilityBindings) {
        throw new RuntimeException("");
    }
}
