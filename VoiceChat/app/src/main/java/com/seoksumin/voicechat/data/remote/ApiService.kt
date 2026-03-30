package com.seoksumin.voicechat.data.remote

import com.seoksumin.voicechat.data.remote.dto.ChatMessageDto
import com.seoksumin.voicechat.data.remote.dto.CreateVoiceRequest
import com.seoksumin.voicechat.data.remote.dto.CreateVoiceResponse
import com.seoksumin.voicechat.data.remote.dto.LoginRequest
import com.seoksumin.voicechat.data.remote.dto.LoginResponse
import com.seoksumin.voicechat.data.remote.dto.MeResponse
import com.seoksumin.voicechat.data.remote.dto.SendChatRequest
import com.seoksumin.voicechat.data.remote.dto.SendChatResponse
import com.seoksumin.voicechat.data.remote.dto.SendOtpRequest
import com.seoksumin.voicechat.data.remote.dto.SendOtpResponse
import com.seoksumin.voicechat.data.remote.dto.SignUpRequest
import com.seoksumin.voicechat.data.remote.dto.SignUpResponse
import com.seoksumin.voicechat.data.remote.dto.UploadVoiceResponse
import com.seoksumin.voicechat.data.remote.dto.VerifyOtpRequest
import com.seoksumin.voicechat.data.remote.dto.VerifyOtpResponse
import com.seoksumin.voicechat.data.remote.dto.VoiceItemDto
import com.seoksumin.voicechat.data.remote.dto.VoiceStatusResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @GET("/health")
    suspend fun healthCheck(): Response<Map<String, Boolean>>

    @POST("/users/auth/otp/send")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): SendOtpResponse

    @POST("/users/auth/otp/verify")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): VerifyOtpResponse

    @POST("/users/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): retrofit2.Response<Unit>

    @POST("/users/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("/users/me")
    suspend fun getMe(): MeResponse

    @POST("/users/voices")
    suspend fun createVoice(
        @Body request: CreateVoiceRequest
    ): CreateVoiceResponse

    @Multipart
    @POST("/users/voices/{voiceId}/upload")
    suspend fun uploadVoice(
        @Path("voiceId") voiceId: Long,
        @Part files: List<MultipartBody.Part>
    ): UploadVoiceResponse

    @GET("/users/voices/{voiceId}/status")
    suspend fun getVoiceStatus(
        @Path("voiceId") voiceId: Long
    ): VoiceStatusResponse

    @GET("/users/voices")
    suspend fun getVoices(): List<VoiceItemDto>

    @POST("/users/voices/{voiceId}/chat")
    suspend fun sendChat(
        @Path("voiceId") voiceId: Long,
        @Body request: SendChatRequest
    ): SendChatResponse

    @GET("/users/voices/{voiceId}/messages")
    suspend fun getMessages(
        @Path("voiceId") voiceId: Long
    ): List<ChatMessageDto>
}