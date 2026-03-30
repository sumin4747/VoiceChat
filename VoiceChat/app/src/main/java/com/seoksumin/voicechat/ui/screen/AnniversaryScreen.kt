package com.seoksumin.voicechat.ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnniversaryScreen() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: 기념일 추가 화면 */ }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("기념일 관리", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("아직 등록된 기념일이 없어요", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                Text("중요한 날을 추가하고 알림을 받아보세요.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { /* TODO: 기념일 추가 화면 */ },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("기념일 추가하기")
                }
            }
        }
    }
}