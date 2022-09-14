package fivecc.tools.shortcut_helper.coil

import android.content.pm.ShortcutInfo
import coil.key.Keyer
import coil.request.Options

class ShortcutInfoKeyer : Keyer<ShortcutInfo> {
    override fun key(data: ShortcutInfo, options: Options): String {
        return "${data.`package`}_${data.id}"
    }
}