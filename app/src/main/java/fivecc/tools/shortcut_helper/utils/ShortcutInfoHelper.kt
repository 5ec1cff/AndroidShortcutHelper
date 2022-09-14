@file:Suppress("CAST_NEVER_SUCCEEDS", "Unchecked_Cast")

package fivecc.tools.shortcut_helper.utils

import android.app.Person
import android.content.ComponentName
import android.content.Intent
import android.content.LocusId
import android.content.pm.*
import android.content.res.Resources
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import com.topjohnwu.superuser.nio.ExtendedFile
import fivecc.tools.shortcut_helper.App
import fivecc.tools.shortcut_helper.RootHelperService
import java.io.File

fun IShortcutService.getPinnedShortcutCompat(packageName: String, userId: Int): ParceledListSlice<ShortcutInfo> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        return getPinnedShortcuts(packageName, userId) as ParceledListSlice<ShortcutInfo>
    } else {
        return getShortcuts(packageName, ShortcutManager.FLAG_MATCH_PINNED, userId) as ParceledListSlice<ShortcutInfo>
    }
}

fun ShortcutInfo.getIconFile(): ExtendedFile? {
    this as ShortcutInfoHidden
    return bitmapPath?.let { RootHelperService.remoteFs?.getFile(it) }
}

fun ShortcutInfo.hasIconFile(): Boolean {
    this as ShortcutInfoHidden
    return hasIconFile()
}

fun ShortcutInfo.hasIconUri(): Boolean {
    this as ShortcutInfoHidden
    return hasIconUri()
}

fun ShortcutInfo.hasIconResource(): Boolean {
    this as ShortcutInfoHidden
    return hasIconResource()
}

fun ShortcutInfo.hasAdaptiveBitmap(): Boolean {
    this as ShortcutInfoHidden
    return hasAdaptiveBitmap()
}

fun ShortcutInfo.getIconResourceId(): Int {
    this as ShortcutInfoHidden
    return getIconResourceId()
}

fun ShortcutInfo.getLabel(): String {
    shortLabel?.also { return it.toString() }
    longLabel?.also { return it.toString() }
    val packageName = `package`
    val defaultName = "Shortcut:$packageName:$id"
    this as ShortcutInfoHidden
    val pm = App.instance.packageManager
    val shortId = shortLabelResourceId
    val longId = longLabelResourceId
    val id = if (shortId != 0) shortId
    else if (longId != 0) longId
    else return defaultName
    try {
        return pm.getResourcesForApplication(packageName).getString(id)
    } catch (e: Resources.NotFoundException) {
        Log.e("ShortcutInfoHelper", "getLabel $id for $packageName not found", e)
    }
    return defaultName
}

fun newShortcutInfoCompat(
    userId: Int,
    id: String?,
    packageName: String?,
    activity: ComponentName?,
    icon: Icon?,
    title: CharSequence?,
    titleResId: Int,
    titleResName: String?,
    text: CharSequence?,
    textResId: Int,
    textResName: String?,
    disabledMessage: CharSequence?,
    disabledMessageResId: Int,
    disabledMessageResName: String?,
    categories: Set<String?>?,
    intentsWithExtras: Array<Intent?>?,
    rank: Int,
    extras: PersistableBundle?,
    lastChangedTimestamp: Long,
    flags: Int,
    iconResId: Int,
    iconResName: String?,
    bitmapPath: String?,
    iconUri: String?,
    disabledReason: Int,
    persons: Array<Person?>?,
    locusId: LocusId?,
): ShortcutInfo {
    val compat = when (Build.VERSION.SDK_INT) {
        Build.VERSION_CODES.O,
        Build.VERSION_CODES.O_MR1 -> ShortcutInfoHidden(
            userId, id, packageName, activity, icon,
            title, titleResId, titleResName, text, textResId, textResName,
            disabledMessage, disabledMessageResId, disabledMessageResName,
            categories,
            intentsWithExtras,
            rank, extras, lastChangedTimestamp, flags,
            iconResId, iconResName, bitmapPath
        )
        Build.VERSION_CODES.P -> ShortcutInfoHidden(
            userId, id, packageName, activity, icon,
            title, titleResId, titleResName, text, textResId, textResName,
            disabledMessage, disabledMessageResId, disabledMessageResName,
            categories,
            intentsWithExtras,
            rank, extras, lastChangedTimestamp, flags,
            iconResId, iconResName, bitmapPath,
            disabledReason
        )
        Build.VERSION_CODES.Q -> ShortcutInfoHidden(
            userId, id, packageName, activity, icon,
            title, titleResId, titleResName, text, textResId, textResName,
            disabledMessage, disabledMessageResId, disabledMessageResName,
            categories,
            intentsWithExtras,
            rank, extras, lastChangedTimestamp, flags,
            iconResId, iconResName, bitmapPath,
            disabledReason, persons, locusId
        )
        Build.VERSION_CODES.R -> ShortcutInfoHidden(
            userId, id, packageName, activity, icon,
            title, titleResId, titleResName, text, textResId, textResName,
            disabledMessage, disabledMessageResId, disabledMessageResName,
            categories,
            intentsWithExtras,
            rank, extras, lastChangedTimestamp, flags,
            iconResId, iconResName, bitmapPath, iconUri,
            disabledReason, persons, locusId
            )
        Build.VERSION_CODES.S, Build.VERSION_CODES.S_V2 -> ShortcutInfoHidden(
            userId, id, packageName, activity, icon,
            title, titleResId, titleResName, text, textResId, textResName,
            disabledMessage, disabledMessageResId, disabledMessageResName,
            categories,
            intentsWithExtras,
            rank, extras, lastChangedTimestamp, flags,
            iconResId, iconResName, bitmapPath, iconUri,
            disabledReason, persons, locusId, null
        )
        else -> ShortcutInfoHidden(
            userId, id, packageName, activity, icon,
            title, titleResId, titleResName, text, textResId, textResName,
            disabledMessage, disabledMessageResId, disabledMessageResName,
            categories,
            intentsWithExtras,
            rank, extras, lastChangedTimestamp, flags,
            iconResId, iconResName, bitmapPath, iconUri,
            disabledReason, persons, locusId, null, null
            )
    }
    return compat as ShortcutInfo
}
