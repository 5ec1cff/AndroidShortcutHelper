package fivecc.tools.shortcut_helper

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ListPreference(
    val preferencesKey: Preferences.Key<String>,
    @StringRes val titleRes: Int,
    val values: Map<String, Int>
)

class Settings(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        val WORK_MODE = ListPreference(
            stringPreferencesKey("work_mode"),
            R.string.work_mode_title,
            mapOf(
                RootHelperService.METHOD_SYSTEM_API to R.string.work_mode_system_api,
                RootHelperService.METHOD_PARSE_FILE to R.string.work_mode_parse_xml
            )
        )
    }

    fun getValue(key: Preferences.Key<String>, defaultValue: String? = null): Flow<String?> = context.dataStore.data
        .map {
            it[key] ?: defaultValue
        }

    suspend fun setValue(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit {
            it[key] = value
        }
    }
}

@Composable
fun MyListPreference(
    listPreference: ListPreference
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dataStore = Settings(context)
    val scope = rememberCoroutineScope()
    val item by dataStore.getValue(listPreference.preferencesKey).collectAsState(initial = null)
    Row(modifier = Modifier
        .clickable { expanded = true }
        .padding(8.dp)) {
        Text(text = stringResource(id = listPreference.titleRes))
        Spacer(Modifier.weight(1f))
        Box(modifier = Modifier.width(100.dp)) {
            Text(text = stringResource(id = listPreference.values[item] ?: R.string.empty_string), textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                listPreference.values.forEach { (k, v) ->
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = v)) },
                        onClick = {
                            scope.launch { dataStore.setValue(listPreference.preferencesKey, k) }
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
