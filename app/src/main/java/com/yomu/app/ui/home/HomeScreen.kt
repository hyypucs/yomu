package com.yomu.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.yomu.app.ui.theme.*
import com.yomu.core.TargetLanguage
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRequestScreenCapture: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scroll = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showAll by rememberSaveable { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    val readingStatus = state.readingStatus
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("최근 번역 기록을 삭제할까요?") },
            text = { Text("이 기기에서 저장된 번역 기록이 모두 삭제됩니다.") },
            confirmButton = { TextButton(colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface), onClick = { confirmClear = false; viewModel.clearHistory() }) { Text("삭제") } },
            dismissButton = { TextButton(colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface), onClick = { confirmClear = false }) { Text("유지") } }
        )
    }
    LazyColumn(
        state = scroll,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("홈", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            val label = when (readingStatus) {
                ReadingStatus.On -> "번역 읽기 켜짐"
                ReadingStatus.Off -> "번역 읽기 꺼짐"
                ReadingStatus.NotReady -> "설정을 완료하고 읽기 시작"
            }
            val icon = when (readingStatus) {
                ReadingStatus.On -> Icons.Default.Check
                ReadingStatus.Off -> Icons.Default.PlayArrow
                ReadingStatus.NotReady -> Icons.Default.Warning
            }
            PaperAction(
                onClick = {
                    if (readingStatus == ReadingStatus.On) viewModel.stopService() else onRequestScreenCapture()
                },
                enabled = readingStatus != ReadingStatus.NotReady,
                accented = readingStatus == ReadingStatus.On,
                modifier = Modifier.fillMaxWidth().semantics { role = Role.Switch; stateDescription = label }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(icon, contentDescription = null)
                    Column(Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.headlineSmall)
                        Text(when (readingStatus) {
                            ReadingStatus.On -> "눌러서 끄기"
                            ReadingStatus.Off -> "눌러서 켜기"
                            ReadingStatus.NotReady -> "먼저 몇 가지 설정이 필요합니다"
                        }, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (readingStatus == ReadingStatus.On) "만화 앱에서 계속 감상하세요. 플로팅 버튼을 누르면 페이지가 번역됩니다."
                else "번역 기능을 켠 후 평소 사용하는 만화 앱에서 감상하세요.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (readingStatus != ReadingStatus.NotReady) {
                Spacer(Modifier.height(12.dp))
                QuickSettingsStrip(state, viewModel::pickTranslationModel, viewModel::chooseLanguage, viewModel::chooseSourceLanguage)
            }
        }
        state.confirmation?.let { message -> item { PaperSuccess(message, viewModel::dismissConfirmation) } }
        if (state.serviceError) item {
            PaperError("읽기를 시작할 수 없습니다. 다시 시도해 주세요.", viewModel::dismissServiceError)
        }
        if (readingStatus == ReadingStatus.NotReady) item {
            PaperSurface(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("읽을 준비하기", style = MaterialTheme.typography.titleLarge)
                    Text(when (state.readiness) {
                        Readiness.NeedsModels -> "말풍선을 찾고 텍스트를 읽고 번역할 모델을 다운로드하세요."
                        Readiness.NeedsPermission -> "만화 앱 위에 번역을 표시할 수 있도록 Yomu 권한을 허용해 주세요."
                        Readiness.NeedsBoth -> "필요한 모델을 다운로드하고 다른 앱 위에 표시 권한을 허용해 주세요."
                        Readiness.Ready -> "간단한 설정을 마치고 읽기를 시작하세요."
                    })
                    PaperButton("설정 완료하기", viewModel::openSetup)
                }
            }
        }
        item {
            Text("오늘 번역한 페이지: ${state.pagesTranslatedToday}장", style = MaterialTheme.typography.bodySmall)
        }
        if (state.historyLoading || state.historyError || state.hasRead) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("최근 기록", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    if (state.translations.isNotEmpty()) TextButton(colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface), onClick = { confirmClear = true }) { Text("삭제") }
                }
            }
            if (state.clearError) item {
                PaperError("최근 기록을 지울 수 없습니다. 번역은 안전하게 저장되어 있습니다.", viewModel::clearHistory)
            }
            when {
                state.historyLoading -> item { PaperLoading("최근 번역 기록 불러오는 중…") }
                state.historyError -> item { PaperError("최근 번역 기록을 불러올 수 없습니다.", viewModel::loadRecentHistory) }
                state.translations.isEmpty() -> item {
                    PaperEmpty("다음 번역 결과가 여기에 나타납니다.", "읽기로 돌아가기") {
                        scope.launch { scroll.animateScrollToItem(0) }
                    }
                }
                else -> {
                    items(if (showAll) state.translations else state.translations.take(3), key = { it.id }) { translation ->
                        var expanded by rememberSaveable(translation.id) { mutableStateOf(false) }
                        PaperAction(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(translation.translatedText, maxLines = if (expanded) Int.MAX_VALUE else 3, style = MaterialTheme.typography.bodyMedium)
                                val translatedAt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(translation.createdAt))
                                val language = TargetLanguage.fromCode(translation.targetLanguage)?.promptName
                                Text(listOfNotNull(translatedAt, language).joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                                Text(if (expanded) "접기" else "번역 읽기", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    if (state.translations.size > 3) item {
                        PaperButton(if (showAll) "최근 항목만 보기" else "모든 번역 보기", { showAll = !showAll })
                    }
                }
            }
        }
    }
}
