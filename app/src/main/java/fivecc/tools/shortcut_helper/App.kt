package fivecc.tools.shortcut_helper

import android.app.Application
import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import fivecc.tools.shortcut_helper.coil.ShortcutIconFetcher
import fivecc.tools.shortcut_helper.coil.ShortcutInfoKeyer
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application(), ImageLoaderFactory {
    companion object {
        lateinit var instance: Application
    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }
        instance = this
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(ShortcutIconFetcher.Factory())
                add(SvgDecoder.Factory())
                add(ShortcutInfoKeyer())
            }.build()
    }
}
