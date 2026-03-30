package com.seoksumin.voicechat.data.remote.dto

data class SendOtpRequest(
    val email: String
)

data class SendOtpResponse(
    val ok: Boolean,
    val expiresInSec: Int,
    val devCode: String? = null
)

data class VerifyOtpRequest(
    val email: String,
    val code: String
)

data class VerifyOtpResponse(
    val ok: Boolean,
    val verified: Boolean,
    val verifyToken: String
)

data class SignUpRequest(
    val email: String,
    val verifyToken: String,
    val loginId: String,
    val password: String,
    val nickname: String
)

data class SignUpResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

data class LoginRequest(
    val loginId: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: LoginUser
)

data class LoginUser(
    val userId: Long,
    val nickname: String
)