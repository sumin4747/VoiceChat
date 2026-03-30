package com.seoksumin.voicechat.ui.screen

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.seoksumin.voicechat.data.remote.RetrofitClient
import com.seoksumin.voicechat.data.remote.dto.SendOtpRequest
import com.seoksumin.voicechat.data.remote.dto.SignUpRequest
import com.seoksumin.voicechat.data.remote.dto.VerifyOtpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

data class ApiErrorResponse(
    val timestamp: String? = null,
    val status: Int? = null,
    val code: String? = null,
    val message: String? = null,
    val path: String? = null,
    val errors: List<FieldErrorItem> = emptyList()
)

data class FieldErrorItem(
    val field: String? = null,
    val message: String? = null
)

private fun parseApiError(raw: String?): ApiErrorResponse? {
    return try {
        if (raw.isNullOrBlank()) null
        else Gson().fromJson(raw, ApiErrorResponse::class.java)
    } catch (e: Exception) {
        Log.e("RegisterScreen", "에러 응답 파싱 실패", e)
        null
    }
}

private fun mapHttpExceptionMessage(e: HttpException, fallback: String): String {
    val raw = try {
        e.response()?.errorBody()?.string()
    } catch (ex: Exception) {
        null
    }

    val error = parseApiError(raw)
    Log.e(
        "RegisterScreen",
        "HTTP 예외 발생 code=${e.code()}, raw=$raw, parsed=$error"
    )

    return when (error?.code) {
        "INVALID_OR_EXPIRED_CODE" -> "인증번호가 올바르지 않거나 만료되었습니다."
        "EMAIL_NOT_VERIFIED" -> "먼저 이메일 인증을 완료해주세요."
        "EMAIL_ALREADY_EXISTS" -> "이미 가입된 이메일입니다."
        "LOGIN_ID_ALREADY_EXISTS" -> "이미 사용 중인 아이디입니다."
        "VALIDATION_ERROR" -> error.errors.firstOrNull()?.message ?: "입력값을 확인해주세요."
        "DATA_INTEGRITY_VIOLATION" -> "중복되거나 잘못된 값이 있습니다."
        else -> error?.message ?: fallback
    }
}

private fun mapSignUpError(response: Response<Unit>): String {
    val raw = try {
        response.errorBody()?.string()
    } catch (e: Exception) {
        null
    }

    val error = parseApiError(raw)
    Log.e(
        "RegisterScreen",
        "회원가입 실패 code=${response.code()}, raw=$raw, parsed=$error"
    )

    return when (error?.code) {
        "EMAIL_NOT_VERIFIED" -> "먼저 이메일 인증을 완료해주세요."
        "EMAIL_ALREADY_EXISTS" -> "이미 가입된 이메일입니다."
        "LOGIN_ID_ALREADY_EXISTS" -> "이미 사용 중인 아이디입니다."
        "VALIDATION_ERROR" -> error.errors.firstOrNull()?.message ?: "입력값을 확인해주세요."
        "DATA_INTEGRITY_VIOLATION" -> "중복되거나 잘못된 값이 있습니다."
        else -> error?.message ?: "회원가입 실패: ${response.code()}"
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onGoLogin: () -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitClient.create(context) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verifyToken by remember { mutableStateOf("") }

    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isEmailVerified by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("회원가입", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                isEmailVerified = false
                verifyToken = ""
                message = null
            },
            label = { Text("이메일") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                if (email.isBlank()) {
                    message = "이메일을 입력해주세요."
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    try {
                        val response = withContext(Dispatchers.IO) {
                            api.sendOtp(SendOtpRequest(email.trim()))
                        }

                        message = if (response.devCode != null) {
                            "인증번호를 보냈습니다. 개발용 코드: ${response.devCode}"
                        } else {
                            "인증번호를 보냈습니다."
                        }
                    } catch (e: HttpException) {
                        message = mapHttpExceptionMessage(
                            e,
                            "인증번호 요청 실패: ${e.code()}"
                        )
                    } catch (e: Exception) {
                        Log.e("RegisterScreen", "OTP 요청 중 네트워크 오류", e)
                        message = "서버 연결에 실패했습니다."
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("인증번호 요청")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = { otpCode = it },
            label = { Text("인증번호") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                if (email.isBlank() || otpCode.isBlank()) {
                    message = "이메일과 인증번호를 입력해주세요."
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    try {
                        val response = withContext(Dispatchers.IO) {
                            api.verifyOtp(
                                VerifyOtpRequest(
                                    email = email.trim(),
                                    code = otpCode.trim()
                                )
                            )
                        }

                        if (response.ok && response.verified) {
                            isEmailVerified = true
                            verifyToken = response.verifyToken
                            message = "이메일 인증이 완료되었습니다."
                        } else {
                            message = "이메일 인증에 실패했습니다."
                        }
                    } catch (e: HttpException) {
                        message = mapHttpExceptionMessage(
                            e,
                            "인증 확인 실패: ${e.code()}"
                        )
                    } catch (e: Exception) {
                        Log.e("RegisterScreen", "OTP 인증 중 네트워크 오류", e)
                        message = "서버 연결에 실패했습니다."
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isEmailVerified) "인증 완료" else "인증 확인")
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = loginId,
            onValueChange = { loginId = it },
            label = { Text("아이디") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = password2,
            onValueChange = { password2 = it },
            label = { Text("비밀번호 확인") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("닉네임") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        if (message != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = message!!,
                color = if (isEmailVerified) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val normalizedEmail = email.trim()
                val normalizedLoginId = loginId.trim()
                val normalizedPassword = password.trim()
                val normalizedPassword2 = password2.trim()
                val normalizedNickname = nickname.trim()

                val okEmail = Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()
                val okLoginId = normalizedLoginId.matches(Regex("^[a-zA-Z0-9_]{4,20}$"))
                val okPw = normalizedPassword.length in 8..72
                val samePw = normalizedPassword == normalizedPassword2
                val okNick = normalizedNickname.isNotBlank() && normalizedNickname.length <= 50

                if (!okEmail) {
                    message = "이메일 형식을 확인해주세요."
                    return@Button
                }

                if (!isEmailVerified || verifyToken.isBlank()) {
                    message = "먼저 이메일 인증을 완료해주세요."
                    return@Button
                }

                if (!okLoginId) {
                    message = "아이디는 4~20자의 영문, 숫자, 언더스코어만 사용할 수 있습니다."
                    return@Button
                }

                if (!okPw) {
                    message = "비밀번호는 8자 이상 72자 이하여야 합니다."
                    return@Button
                }

                if (!samePw) {
                    message = "비밀번호가 서로 같지 않아요."
                    return@Button
                }

                if (!okNick) {
                    message = "닉네임은 1자 이상 50자 이하여야 합니다."
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    try {
                        val response = withContext(Dispatchers.IO) {
                            api.signUp(
                                SignUpRequest(
                                    email = normalizedEmail,
                                    verifyToken = verifyToken,
                                    loginId = normalizedLoginId,
                                    password = normalizedPassword,
                                    nickname = normalizedNickname
                                )
                            )
                        }

                        if (response.isSuccessful) {
                            message = null
                            onRegisterSuccess()
                        } else {
                            message = mapSignUpError(response)
                        }
                    } catch (e: Exception) {
                        Log.e("RegisterScreen", "회원가입 중 예외 발생", e)
                        message = "서버 연결에 실패했습니다."
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "처리 중..." else "가입하기")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onGoLogin,
            enabled = !isLoading
        ) {
            Text("이미 계정이 있나요? 로그인")
        }
    }
}