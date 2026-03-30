package com.seoksumin.voicechat.data.remote.dto

data class MeResponse(
    val id: Long,
    val email: String,
    val loginId: String,
    val nickname: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)