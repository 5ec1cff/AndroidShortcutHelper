@file:Suppress("DEPRECATION")

package fivecc.tools.shortcut_helper

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import fivecc.tools.shortcut_helper.ui.theme.ShortcutTheme
import fivecc.tools.shortcut_helper.utils.getLabel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RootHelperService.start()
        val selectButtonText: String
        val onSelected: (ShortcutInfo) -> Unit = if (intent.action == Intent.ACTION_CREATE_SHORTCUT) {
            selectButtonText = "select"
            { s: ShortcutInfo ->
                setResult(RESULT_OK, Intent()
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, s.intent)
                    .putExtra(Intent.EXTRA_SHORTCUT_NAME, s.getLabel())
                )
                finish()
            }
        } else {
            selectButtonText = "launch"
            { s: ShortcutInfo ->
                try {
                    s.intent?.also { intent ->
                        val launchIntent = Intent(intent)
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(launchIntent)
                    }
                } catch (e: Throwable) {
                    Log.e("MainActivity", "failed to start ${s.intent}", e)
                }
            }
        }
        setContent {
            ShortcutTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var settingsDialogShown by remember { mutableStateOf(false) }
                    Scaffold(
                        topBar = {
                            TopAppBar(title = { Text("Shortcuts") },
                                actions = {
                                    IconButton(onClick = { settingsDialogShown = true }) {
                                        Icon(Icons.Filled.Settings, contentDescription = "settings")
                                    }
                                },
                                modifier = Modifier.shadow(8.dp))
                        },
                        content = {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .padding(it)
                                .consumedWindowInsets(it),
                                contentAlignment = Alignment.Center
                            ) {
                                val state by RootHelperService.serviceState.observeAsState()
                                when (state) {
                                    ServiceState.RUNNING -> {
                                        ShortcutScreen(onSelected = onSelected, selectButtonText = selectButtonText)
                                    }
                                    ServiceState.STARTING -> {
                                        TipScreen("service is starting")
                                    }
                                    ServiceState.STOPPED -> {
                                        TipScreen("service is unavailable (root permission required)")
                                    }
                                    else -> {
                                        TipScreen("WTF?")
                                    }
                                }
                            }
                            if (settingsDialogShown) {
                                BaseDialog(onDismiss = { settingsDialogShown = false }) {
                                    Box(modifier = Modifier
                                        .defaultMinSize(minHeight = 200.dp)
                                        .padding(16.dp)
                                    ) {
                                        MyListPreference(listPreference = Settings.WORK_MODE)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TipScreen(text: String) {
    Text(text = text, textAlign = TextAlign.Center, modifier = Modifier.wrapContentHeight())
}

@Composable
fun ShortcutScreen(
    selectButtonText: String? = null,
    onSelected: ((ShortcutInfo) -> Unit)? = null
) {
    val viewModel: MainActivityViewModel = viewModel()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val shortcuts = viewModel.shortcutList
    var dialogShowing by remember { mutableStateOf<ShortcutInfo?>(null) }
    val context = LocalContext.current
    val settings = Settings(context)
    val workMode by settings.getValue(Settings.WORK_MODE.preferencesKey).collectAsState(initial = null)
    if (workMode == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TipScreen(text = "Please configure work mode first")
        }
    } else {
        SwipeRefresh(state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.loadShortcuts(workMode!!) }
        ) {
            if (shortcuts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    TipScreen(text = "Nothing to show, try refresh")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    items(shortcuts) { s ->
                        ShortcutCard(shortcut = s, onClick = { dialogShowing = it })
                    }
                }
            }
        }
    }
    dialogShowing?.let { s ->
        ShortcutDialog(shortcut = s,
            onDismiss = { dialogShowing = null },
            onSelected = {
                dialogShowing = null
                onSelected?.invoke(it)
            },
            selectButtonText = selectButtonText)
    }
}

@Composable
fun ShortcutCard(
    shortcut: ShortcutInfo,
    onClick: ((ShortcutInfo) -> Unit)? = null
) {
    Row(modifier = Modifier
        .clickable(onClick = { onClick?.invoke(shortcut) })
        .padding(all = 8.dp)
        .fillMaxWidth()) {
        AsyncImage(
            model = shortcut,
            contentDescription = "icon",
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = shortcut.getLabel(), maxLines = 1, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = AppInfoCache.getAppLabel(shortcut.`package`))
        }
    }
}

@Composable
fun BaseDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(modifier = Modifier.shadow(8.dp), shape = RoundedCornerShape(12.dp)) {
            content()
        }
    }
}

@Composable
fun ShortcutDialog(
    shortcut: ShortcutInfo,
    onDismiss: () -> Unit,
    selectButtonText: String? = null,
    onSelected: ((ShortcutInfo) -> Unit)? = null
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(modifier = Modifier.shadow(8.dp), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = shortcut,
                        contentDescription = "icon",
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = shortcut.shortLabel.toString(),
                            maxLines = 1,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = AppInfoCache.getAppLabel(shortcut.`package`))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                MyTextField(title = "Package Name", content = shortcut.`package`)
                MyTextField(title = "ID", content = shortcut.id)
                MyTextField(title = "Uri", content = "${shortcut.intent?.toUri(0)}")
                if (onSelected != null && selectButtonText != null) {
                    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onSelected.invoke(shortcut) },
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(text = selectButtonText)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTextField(title: String, content: String) {
    val clipboard = LocalClipboardManager.current
    OutlinedTextField(value = content, onValueChange = {}, enabled = false,
        label = { Text(text = title) }, modifier = Modifier
            .fillMaxWidth()
            .clickable {
                clipboard.setText(AnnotatedString(content))
            })
}
