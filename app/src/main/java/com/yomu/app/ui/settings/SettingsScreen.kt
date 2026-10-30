package com.yomu.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yomu.app.ui.theme.*

enum class SettingsSection(val route: String, val title: String, val description: String) {
    Translation("settings/translation", "번역", "기기 내에서 텍스트를 원하는 언어로 번역하는 방식입니다."),
    NameGlossary("settings/name-glossary", "이름/고유명사 사전", "항상 동일하게 번역할 고유명사 목록입니다."),
    Pipeline("settings/pipeline", "파이프라인", "말풍선을 찾고 텍스트를 읽는 모델을 설정합니다."),
    Typesetting("settings/typesetting", "조판 (식자)", "말풍선 안에 번역 텍스트를 배치하는 형태입니다."),
    Performance("settings/performance", "성능", "번역 리소스 소모량 및 모델 메모리 사용량을 확인합니다."),
    Appearance("settings/appearance", "화면 테마", "Yomu의 화면 디자인 및 테마를 설정합니다.")
}

@Composable
fun SettingsScreen(onOpen: (SettingsSection) -> Unit) {
    val context = LocalContext.current
    val version = remember { context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty() }
    SettingsPage("설정") {
        PaperSurface(Modifier.fillMaxWidth()) {
            Column {
                SettingsSection.entries.forEach { section ->
                    SettingsEntry(section.title, section.description, onClick = { onOpen(section) })
                }
                SettingsEntry("제공자", "출시 예정", enabled = false)
                SettingsEntry("정보", "Yomu v$version")
            }
        }
    }
}

@Composable
private fun SettingsEntry(title: String, description: String, enabled: Boolean = true, onClick: (() -> Unit)? = null) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val row: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = if (enabled) LocalContentColor.current else muted)
                Text(description, style = MaterialTheme.typography.bodySmall, color = muted)
            }
            if (onClick != null) Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
        }
    }
    if (onClick == null) row() else Surface(onClick = onClick, enabled = enabled, color = Color.Transparent, content = row)
}

@Composable
internal fun SettingsPage(title: String, description: String? = null, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            onBack?.let { IconButton(onClick = it) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            Text(title, style = MaterialTheme.typography.headlineLarge)
        }
        description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        content()
    }
}

@Composable
internal fun SettingRow(label: String, description: String, control: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val text: @Composable () -> Unit = {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (maxWidth < 600.dp) {
            Column { text(); Spacer(Modifier.height(8.dp)); control() }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { text() }
                Spacer(Modifier.width(16.dp))
                Box(Modifier.weight(1f)) { control() }
            }
        }
    }
}
