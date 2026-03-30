package com.seoksumin.voicechat.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.seoksumin.voicechat.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val appVersion: String = "1.0.0",
    val pushEnabled: Boolean = true,
    val eventNoticeEnabled: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: (() -> Unit)? = null,
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onManageDevices: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onTermsOfService: () -> Unit = {},
    onLogout: () -> Unit = {},
    onWithdraw: () -> Unit = {}
) {
    val context = LocalContext.current
    val api = remember { RetrofitClient.create(context) }

    var uiState by remember { mutableStateOf(ProfileUiState()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = null

            val me = withContext(Dispatchers.IO) {
                api.getMe()
            }

            uiState = uiState.copy(
                name = me.nickname,
                email = me.email
            )
        } catch (e: Exception) {
            errorMessage = "사용자 정보를 불러오지 못했습니다."
        } finally {
            isLoading = false
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("정말 로그아웃하시겠어요?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("로그아웃")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog = false },
            title = { Text("회원 탈퇴") },
            text = { Text("회원 탈퇴를 진행하시겠어요? 이 작업은 되돌릴 수 없어요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWithdrawDialog = false
                        onWithdraw()
                    }
                ) {
                    Text("탈퇴")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            tonalElevation = 2.dp,
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🙂")
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.name.isBlank()) "이름 없음" else uiState.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (uiState.email.isBlank()) "이메일 없음" else uiState.email,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                item {
                    Button(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("프로필 수정")
                    }
                }

                item { SectionTitle("계정 관리") }
                item { SettingRow("비밀번호 변경", onClick = onChangePassword) }
                item { SettingRow("로그인된 기기 관리", onClick = onManageDevices) }

                item { SectionTitle("알림 설정") }
                item {
                    SettingSwitchRow(
                        title = "푸시 알림",
                        checked = uiState.pushEnabled,
                        onCheckedChange = {
                            uiState = uiState.copy(pushEnabled = it)
                        }
                    )
                }
                item {
                    SettingSwitchRow(
                        title = "이벤트 및 공지 알림",
                        checked = uiState.eventNoticeEnabled,
                        onCheckedChange = {
                            uiState = uiState.copy(eventNoticeEnabled = it)
                        }
                    )
                }

                item { SectionTitle("앱 정보") }
                item { SettingRow("개인정보 처리방침", onClick = onPrivacyPolicy) }
                item { SettingRow("서비스 이용약관", onClick = onTermsOfService) }
                item { SettingRowRightValue("버전 정보", uiState.appVersion) }

                item { Spacer(Modifier.height(10.dp)) }

                item {
                    Button(
                        onClick = { showLogoutDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("로그아웃")
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWithdrawDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "회원 탈퇴",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall)
}

@Composable
private fun SettingRow(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, modifier = Modifier.weight(1f))
            Text(">")
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun SettingRowRightValue(
    title: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, modifier = Modifier.weight(1f))
            Text(value)
        }
    }
}