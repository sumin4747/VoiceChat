package com.seoksumin.voicechat.ui.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.seoksumin.voicechat.data.remote.RetrofitClient
import com.seoksumin.voicechat.data.remote.dto.CreateVoiceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.FileOutputStream


data class UploadFileItem(
    val uri: Uri,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadVoiceScreen(
    onBack: () -> Unit,
    onUploadSuccess: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val api = remember { RetrofitClient.create(context) }
    val scope = rememberCoroutineScope()

    var personName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    val files = remember { mutableStateListOf<UploadFileItem>() }

    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            val fileName = getFileName(context, uri) ?: "voice_file_${files.size + 1}"
            files.add(UploadFileItem(uri = uri, name = fileName))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("음성 복원하기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("복원 대상자의 정보 입력", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = { Text("생년월일") },
                placeholder = { Text("예: 1950-03-01") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(Modifier.height(6.dp))

            Text("복원 대상자의 목소리 업로드", style = MaterialTheme.typography.titleLarge)
            Text(
                "복원을 위해 최소 1분 이상의 깨끗한 음성 파일을 업로드해주세요.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("☁️", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(10.dp))
                    Text("음성 파일 업로드", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("지원 파일 형식(MP3, WAV, M4A)", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf("audio/mpeg", "audio/wav", "audio/x-wav", "audio/mp4", "audio/*")
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isLoading
                    ) {
                        Text("파일 찾기")
                    }
                }
            }

            files.forEachIndexed { idx, f ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎵")
                        Spacer(Modifier.width(10.dp))
                        Text(f.name, modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                files.removeAt(idx)
                            },
                            enabled = !isLoading
                        ) {
                            Text("✕")
                        }
                    }
                }
            }

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("업로드 진행 중...", modifier = Modifier.weight(1f))
                    Text("${(progress * 100).toInt()}%")
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (message != null) {
                Text(
                    text = message!!,
                    color = if (message!!.contains("완료")) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    if (personName.isBlank()) {
                        message = "이름을 입력해주세요."
                        return@Button
                    }

                    if (birthDate.isBlank()) {
                        message = "생년월일을 입력해주세요."
                        return@Button
                    }

                    if (files.isEmpty()) {
                        message = "음성 파일을 선택해주세요."
                        return@Button
                    }

                    scope.launch {
                        try {
                            isLoading = true
                            progress = 0.1f
                            message = null

                            val createVoiceResponse = withContext(Dispatchers.IO) {
                                api.createVoice(
                                    CreateVoiceRequest(
                                        personName = personName,
                                        birthDate = birthDate
                                    )
                                )
                            }

                            val voiceId = createVoiceResponse.voiceId
                            progress = 0.35f

                            val multipartFiles = withContext(Dispatchers.IO) {
                                files.map { item ->
                                    val file = uriToFile(context, item.uri, item.name)
                                    val requestBody = file
                                        .asRequestBody("audio/*".toMediaTypeOrNull())

                                    MultipartBody.Part.createFormData(
                                        name = "files",
                                        filename = file.name,
                                        body = requestBody
                                    )
                                }
                            }

                            withContext(Dispatchers.IO) {
                                api.uploadVoice(
                                    voiceId = voiceId,
                                    files = multipartFiles
                                )
                            }

                            progress = 1f
                            message = "업로드가 완료되었습니다."
                            onUploadSuccess?.invoke()
                        } catch (e: Exception) {
                            message = "업로드에 실패했습니다."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("업로드 시작")
                }
            }
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex >= 0) {
            it.getString(nameIndex)
        } else {
            null
        }
    }
}

private fun uriToFile(context: Context, uri: Uri, fileName: String): File {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("파일을 열 수 없습니다.")

    val tempFile = File(context.cacheDir, fileName)
    FileOutputStream(tempFile).use { output ->
        inputStream.use { input ->
            input.copyTo(output)
        }
    }
    return tempFile
}