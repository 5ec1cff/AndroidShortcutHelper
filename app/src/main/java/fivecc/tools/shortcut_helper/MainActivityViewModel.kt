package fivecc.tools.shortcut_helper

import android.content.pm.ShortcutInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

// ShortcutService.getShortcuts does not return their icon
// Consider to parse xml instead of use system API
// Reference: frameworks/base/services/core/java/com/android/server/pm/ShortcutParser.java

class MainActivityViewModel : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    val shortcutList = mutableStateListOf<ShortcutInfo>()

    fun loadShortcuts() {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            val newList = withContext(Dispatchers.IO) {
                RootHelperService.helper?.shortcuts
            }
            shortcutList.clear()
            newList?.also { shortcutList.addAll(it) }
            _isRefreshing.emit(false)
        }
    }
}
