package com.seoksumin.voicechat.ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    // 앱 시작 후 1.2초 기다렸다가 onFinished() 실행
    LaunchedEffect(Unit) {
        delay(1200)
        onFinished()
    }

    // 가운데 정렬로 "앱 이름 + 로딩 동그라미" 보여주기
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "VoiceChat",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}
