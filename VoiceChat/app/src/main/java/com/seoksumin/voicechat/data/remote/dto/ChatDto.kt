package com.seoksumin.voicechat.data.remote.dto


data class SendChatRequest(
    val message: String
)

data class SendChatResponse(
    val replyText: String,
    val ttsAudioUrl: String?
)

data class ChatMessageDto(
    val role: String,
    val content: String,
    val audioUrl: String? = null,
    val createdAt: String
)