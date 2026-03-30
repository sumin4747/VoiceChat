package com.seoksumin.voicechat.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthChoiceScreen(
    onGoRegister: () -> Unit,
    onGoLogin: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("환영해요!", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("처음이신가요? 회원가입을 진행해주세요.")
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onGoRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("회원가입")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onGoLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("로그인")
            }
        }
    }
}