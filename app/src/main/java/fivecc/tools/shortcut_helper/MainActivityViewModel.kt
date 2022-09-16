package fivecc.tools.shortcut_helper

import android.content.pm.ShortcutInfo
import android.os.UserHandleHidden
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fivecc.tools.shortcut_helper.utils.MATCH_ALL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    val shortcutList = mutableStateListOf<ShortcutInfo>()

    fun loadShortcuts(method: String = RootHelperService.METHOD_SYSTEM_API) {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            val newList = withContext(Dispatchers.IO) {
                RootHelperService.helper?.getShortcuts(method, UserHandleHidden.myUserId(), MATCH_ALL)
            }
            shortcutList.clear()
            newList?.also { shortcutList.addAll(it) }
            _isRefreshing.emit(false)
        }
    }
}
