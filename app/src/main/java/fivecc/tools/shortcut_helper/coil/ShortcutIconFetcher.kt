package fivecc.tools.shortcut_helper.coil

import android.content.pm.ShortcutInfo
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.MimeTypeMap
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.DataSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.decode.ImageSource
import coil.fetch.DrawableResult
import coil.request.Options
import fivecc.tools.shortcut_helper.RootHelperService
import fivecc.tools.shortcut_helper.utils.*
import okio.buffer
import okio.source

// frameworks/base/core/java/android/content/pm/LauncherApps.java getShortcutIconDrawable
class ShortcutIconFetcher(
    private val info: ShortcutInfo,
    private val options: Options
) : Fetcher {
    companion object {
        private const val TAG = "ShortcutIconFetcher"
        private const val USE_API = true
    }

    @OptIn(ExperimentalCoilApi::class)
    override suspend fun fetch(): FetchResult? {
        if (info.hasIconFile()) {
            if (USE_API) {
                RootHelperService.helper?.getShortcutIconFd(
                    info.`package`, info.id, 0
                )?.also { fd ->
                    return SourceResult(
                        source = ImageSource(
                            source = ParcelFileDescriptor.AutoCloseInputStream(fd).source()
                                .buffer(),
                            context = options.context,
                            metadata = null
                        ),
                        mimeType = null,
                        dataSource = DataSource.DISK
                    )
                }
            } else {
                val iconFile = info.getIconFile()
                if (iconFile != null) {
                    kotlin.runCatching {
                        if (iconFile.exists() && iconFile.isFile) {
                            return SourceResult(
                                source = ImageSource(
                                    source = iconFile.newInputStream().source().buffer(),
                                    context = options.context,
                                    metadata = null
                                ),
                                mimeType = MimeTypeMap.getSingleton()
                                    .getMimeTypeFromExtension(iconFile.extension),
                                dataSource = DataSource.DISK
                            )
                        }
                    }.onFailure {
                        Log.e(TAG, "failed to fetch icon from root: $iconFile")
                    }
                }
            }
        } else if (info.hasIconResource()) {
            val res = options.context.packageManager.getResourcesForApplication(info.`package`)
            return DrawableResult(
                drawable = res.getDrawable(info.getIconResourceId()),
                isSampled = false,
                dataSource = DataSource.MEMORY
            )
        }
        return null
    }

    class Factory : Fetcher.Factory<ShortcutInfo> {
        override fun create(
            data: ShortcutInfo,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return ShortcutIconFetcher(data, options)
        }
    }
}