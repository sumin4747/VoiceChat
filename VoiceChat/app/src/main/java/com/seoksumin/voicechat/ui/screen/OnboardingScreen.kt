package com.seoksumin.voicechat.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "그리운 목소리, 다시 한번",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "AI 기술로 소중한 사람의 목소리를 복원하고\n따뜻한 대화를 나눠보세요",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(30.dp))
            Button(onClick = onStartClick) {
                Text("시작하기")
            }
        }
    }
}