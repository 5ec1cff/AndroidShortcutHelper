@file:Suppress("CAST_NEVER_SUCCEEDS", "Unchecked_Cast")

package fivecc.tools.shortcut_helper.utils

import android.app.Person
import android.content.ComponentName
import android.content.Intent
import android.content.LocusId
import android.content.pm.*
import android.content.res.Resources
import android.graphics.drawable.Icon
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import fivecc.tools.shortcut_helper.App

const val MATCH_PINNED = 1
const val MATCH_DYNAMIC = 1 shl 1
const val MATCH_MANIFEST = 1 shl 2
const val MATCH_CACHED = 1 shl 3
const val MATCH_ALL = MATCH_PINNED or MATCH_DYNAMIC or MATCH_MANIFEST or MATCH_CACHED

/**
 * Use IShortcutManager API to get ShortcutInfo s
 * which `bitmapPath` are empty
 */
fun IShortcutService.getShortcutInfoCompat(
    packageName: String, userId: Int, matchFlags: Int = MATCH_ALL
): List<ShortcutInfo> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        val result = mutableListOf<ShortcutInfo>()
        if (matchFlags and MATCH_PINNED != 0) {
            result.addAll(getPinnedShortcuts(packageName, userId).list as List<ShortcutInfo>)
        }
        if (matchFlags and MATCH_MANIFEST != 0) {
            result.addAll(getManifestShortcuts(packageName, userId).list as List<ShortcutInfo>)
        }
        if (matchFlags and MATCH_DYNAMIC != 0) {
            result.addAll(getDynamicShortcuts(packageName, userId).list as List<ShortcutInfo>)
        }
        return result
    } else {
        var flags = 0
        if (matchFlags and MATCH_PINNED != 0) {
            flags = flags or ShortcutManager.FLAG_MATCH_PINNED
        }
        if (matchFlags and MATCH_MANIFEST != 0) {
            flags = flags or ShortcutManager.FLAG_MATCH_MANIFEST
        }
        if (matchFlags and MATCH_DYNAMIC != 0) {
            flags = flags or ShortcutManager.FLAG_MATCH_DYNAMIC
        }
        if (matchFlags and MATCH_CACHED != 0) {
            flags = flags or ShortcutManager.FLAG_MATCH_CACHED
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getShortcuts(packageName, flags, userId).list as List<ShortcutInfo>
        } else {
            return (this as IShortcutServiceForS).getShortcuts(packageName, flags, userId).get().list as List<ShortcutInfo>
        }
    }
}

fun ShortcutInfo.hasIconFile(): Boolean {
    this as ShortcutInfoHidden
    return hasIconFile()
}

fun ShortcutInfo.hasIconResource(): Boolean {
    this as ShortcutInfoHidden
    return hasIconResource()
}

fun ShortcutInfo.getIconResourceId(): Int {
    this as ShortcutInfoHidden
    return getIconResourceId()
}

fun ShortcutInfo.getUserId(): Int {
    this as ShortcutInfoHidden
    return userId
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
