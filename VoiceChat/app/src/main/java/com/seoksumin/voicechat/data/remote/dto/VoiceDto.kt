package com.seoksumin.voicechat.data.remote.dto


data class CreateVoiceRequest(
    val personName: String,
    val birthDate: String
)

data class CreateVoiceResponse(
    val voiceId: Long,
    val status: String
)

data class UploadVoiceResponse(
    val voiceId: Long
)

data class VoiceStatusResponse(
    val status: String,
    val progressPercent: Int
)

data class VoiceItemDto(
    val voiceId: Long,
    val personName: String,
    val createdAt: String,
    val status: String,
    val thumbnailUrl: String?
)