package com.seoksumin.voicechat.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.seoksumin.voicechat.data.prefs.AppPrefs
import com.seoksumin.voicechat.data.remote.RetrofitClient
import com.seoksumin.voicechat.data.remote.dto.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

data class LoginApiErrorResponse(
    val timestamp: String? = null,
    val status: Int? = null,
    val code: String? = null,
    val message: String? = null,
    val path: String? = null,
    val errors: List<LoginFieldErrorItem> = emptyList()
)

data class LoginFieldErrorItem(
    val field: String? = null,
    val message: String? = null
)

private fun parseLoginApiError(raw: String?): LoginApiErrorResponse? {
    return try {
        if (raw.isNullOrBlank()) null
        else Gson().fromJson(raw, LoginApiErrorResponse::class.java)
    } catch (e: Exception) {
        Log.e("LoginScreen", "로그인 에러 응답 파싱 실패", e)
        null
    }
}

private fun mapLoginError(response: Response<*>): String {
    val raw = try {
        response.errorBody()?.string()
    } catch (e: Exception) {
        null
    }

    val error = parseLoginApiError(raw)
    Log.e("LoginScreen", "로그인 실패 code=${response.code()}, raw=$raw, parsed=$error")

    return when (response.code()) {
        400, 401 -> error?.message ?: "아이디 또는 비밀번호가 올바르지 않습니다."
        else -> error?.message ?: "로그인 실패: ${response.code()}"
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitClient.create(context) }
    val scope = rememberCoroutineScope()

    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("로그인", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = loginId,
            onValueChange = {
                loginId = it
                message = null
            },
            label = { Text("아이디") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                message = null
            },
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !isLoading
        )

        if (message != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = message!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val normalizedLoginId = loginId.trim()
                val normalizedPassword = password.trim()

                if (normalizedLoginId.isBlank() || normalizedPassword.isBlank()) {
                    message = "아이디와 비밀번호를 입력해주세요."
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    try {
                        val response = withContext(Dispatchers.IO) {
                            api.login(
                                LoginRequest(
                                    loginId = normalizedLoginId,
                                    password = normalizedPassword
                                )
                            )
                        }

                        if (response.isSuccessful) {
                            val body = response.body()

                            if (body == null || body.token.isBlank()) {
                                Log.e("LoginScreen", "로그인 성공 응답이지만 body 또는 token이 비어 있음. body=$body")
                                message = "로그인 응답 형식을 확인해주세요."
                            } else {
                                AppPrefs.saveToken(context, body.token)
                                AppPrefs.setLoggedIn(context, true)
                                message = null
                                onLoginSuccess()
                            }
                        } else {
                            message = mapLoginError(response)
                        }
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "로그인 중 예외 발생", e)
                        message = "서버 연결에 실패했습니다."
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "처리 중..." else "로그인")
        }
    }
}