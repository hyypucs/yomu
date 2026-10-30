package com.yomu.app.ui.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.yomu.app.db.entities.ModelType
import com.yomu.app.ui.settings.SlotPicker
import com.yomu.app.ui.theme.*
import com.yomu.core.toFileSizeString

@Composable
fun SetupScreen(state: HomeUiState, viewModel: HomeViewModel) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("읽기 설정", style = MaterialTheme.typography.headlineLarge)
        Text("내 언어로 만화 즐기기", style = MaterialTheme.typography.headlineSmall)
        Text("Yomu는 말풍선을 찾아 텍스트를 인식하고 기기 내에서 직접 번역합니다. 데이터가 외부로 전송되지 않습니다.")
        PaperSurface(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("원하는 만화 앱에서 바로 감상", style = MaterialTheme.typography.titleLarge)
                Text("번역 읽기를 켜두고 웹툰/만화를 감상하세요. 페이지를 번역하고 싶을 때 Yomu 플로팅 버튼을 누르면 됩니다.")
                Text("먼저 페이지를 읽고 번역할 모델을 다운로드합니다. 기본 모델은 대부분의 기기에 최적화되어 있습니다.")
            }
        }
        if (!state.setupStarted || state.setupError) {
            SetupSlot(state, viewModel, ModelType.DETECTION, "말풍선 검출", "페이지에서 말풍선 위치를 감지합니다.")
            SetupSlot(state, viewModel, ModelType.OCR, "문자 인식 (OCR)", "말풍선 속 텍스트를 읽어냅니다.")
            SetupSlot(state, viewModel, ModelType.LLM, "온디바이스 번역", "각 말풍선을 원하는 언어로 번역하는 로컬 AI 모델입니다.")
        }
        when {
            state.setupBlockedBy != null -> PaperError(
                "${state.setupBlockedBy} 모델은 이 기기의 가용 메모리보다 큽니다. 더 작은 모델을 선택해 주세요.",
                viewModel::prepareSetup
            )
            state.setupError -> PaperError(
                "설정을 완료하지 못했습니다. 네트워크와 저장공간을 확인 후 다시 시도해 주세요.",
                viewModel::prepareSetup
            )
            !state.setupStarted -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (state.setupDownloadBytes > 0) "다운로드 크기: ${state.setupDownloadBytes.toFileSizeString()}"
                    else "선택된 모든 모델이 이미 다운로드되어 있습니다.",
                    style = MaterialTheme.typography.titleMedium
                )
                if (state.onMeteredNetwork && state.setupDownloadBytes > 0) {
                    Text("모바일 데이터를 사용 중입니다. 데이터 요금제에 따라 요금이 발생할 수 있습니다.")
                }
                PaperButton("다운로드", viewModel::prepareSetup, Modifier.fillMaxWidth())
            }
            !state.setupDownloadsReady -> PaperSurface(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp).semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(state.downloading ?: "읽기 기능 준비 중…", style = MaterialTheme.typography.titleMedium)
                    if (state.downloadProgress == 0) LinearProgressIndicator(Modifier.fillMaxWidth())
                    else {
                        LinearProgressIndicator(progress = state.downloadProgress / 100f, modifier = Modifier.fillMaxWidth())
                        Text("${state.downloadProgress}%")
                    }
                    Text("다운로드가 완료될 때까지 Yomu를 켜두세요.", style = MaterialTheme.typography.bodySmall)
                }
            }
            state.readiness != Readiness.Ready -> PaperSurface(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("다른 앱 위에 표시 권한 허용", style = MaterialTheme.typography.titleLarge)
                    Text("만화 위에 번역 텍스트와 플로팅 버튼을 표시하려면 권한이 필요합니다. ‘다른 앱 위에 표시’를 허용한 후 돌아와 주세요.")
                    PaperButton("권한 설정 열기", {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                    })
                }
            }
            else -> PaperSurface(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Check, contentDescription = "준비 완료")
                    Text("모든 준비가 완료되었습니다", style = MaterialTheme.typography.titleLarge)
                    Text("번역 읽기를 켜면 Android에서 화면 캡처 공유 권한을 요청합니다.")
                    PaperButton("홈으로 이동", viewModel::completeSetup)
                }
            }
        }
        TextButton(colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface), onClick = viewModel::dismissSetup) { Text("홈으로 돌아가기") }
    }
}

@Composable
private fun SetupSlot(state: HomeUiState, viewModel: HomeViewModel, type: ModelType, title: String, description: String) {
    SlotPicker(
        title = title,
        description = description,
        deliverables = state.deliverables[type].orEmpty(),
        selectedId = state.chosenIds[type],
        pendingId = null,
        statuses = state.models.associate { it.id to it.status },
        downloads = emptyMap(),
        fits = state::fits,
        needsSlowWarning = state::needsSlowWarning,
        languageBlock = state::languageBlock,
        onPick = { viewModel.pickSetupModel(type, it) },
        onCancel = {},
        onDelete = viewModel::deleteModel
    )
}
