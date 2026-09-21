package com.example.data.remote.api

import com.example.data.remote.model.ApiResponse
import com.example.data.remote.model.AttachmentUploadRequest
import com.example.data.remote.model.AttachmentUploadResponse
import com.example.data.remote.model.AuthResponse
import com.example.data.remote.model.ChannelDto
import com.example.data.remote.model.ChannelPostDto
import com.example.data.remote.model.ConversationDto
import com.example.data.remote.model.CreateChannelRequest
import com.example.data.remote.model.CreateGroupRequest
import com.example.data.remote.model.EditMessageRequest
import com.example.data.remote.model.GroupDto
import com.example.data.remote.model.LoginRequest
import com.example.data.remote.model.MarkReadRequest
import com.example.data.remote.model.MessageDto
import com.example.data.remote.model.PublishChannelPostRequest
import com.example.data.remote.model.ReactionDto
import com.example.data.remote.model.RefreshTokenRequest
import com.example.data.remote.model.RegisterRequest
import com.example.data.remote.model.ReportDto
import com.example.data.remote.model.SendMessageRequest
import com.example.data.remote.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TarhiNooApiService {

    // --- Authentication ---
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    // --- Users & Profiles ---
    @GET("users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<ApiResponse<UserDto>>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<ApiResponse<List<UserDto>>>

    @PATCH("users/me")
    suspend fun updateProfile(@Body updates: Map<String, String>): Response<ApiResponse<UserDto>>

    // --- Conversations ---
    @GET("conversations")
    suspend fun getConversations(
        @Query("since") since: Long? = null,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<ConversationDto>>>

    @POST("conversations/direct/{targetUserId}")
    suspend fun getOrCreateDirectConversation(
        @Path("targetUserId") targetUserId: String
    ): Response<ApiResponse<ConversationDto>>

    // --- Messages ---
    @GET("conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("before") beforeCursor: String? = null,
        @Query("limit") limit: Int = 40
    ): Response<ApiResponse<List<MessageDto>>>

    @POST("conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body request: SendMessageRequest
    ): Response<ApiResponse<MessageDto>>

    @PATCH("messages/{messageId}")
    suspend fun editMessage(
        @Path("messageId") messageId: String,
        @Body request: EditMessageRequest
    ): Response<ApiResponse<MessageDto>>

    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: String,
        @Query("mode") mode: String = "EVERYONE" // "EVERYONE" or "FOR_ME"
    ): Response<ApiResponse<Unit>>

    @POST("conversations/{conversationId}/read")
    suspend fun markConversationRead(
        @Path("conversationId") conversationId: String,
        @Body request: MarkReadRequest
    ): Response<ApiResponse<Unit>>

    // --- Reactions ---
    @POST("messages/{messageId}/reactions")
    suspend fun addReaction(
        @Path("messageId") messageId: String,
        @Query("reaction") reaction: String
    ): Response<ApiResponse<ReactionDto>>

    @DELETE("messages/{messageId}/reactions/{reaction}")
    suspend fun removeReaction(
        @Path("messageId") messageId: String,
        @Path("reaction") reaction: String
    ): Response<ApiResponse<Unit>>

    // --- Groups ---
    @POST("groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<ApiResponse<GroupDto>>

    @POST("groups/{groupId}/members")
    suspend fun addGroupMember(
        @Path("groupId") groupId: String,
        @Query("userId") userId: String
    ): Response<ApiResponse<Unit>>

    @DELETE("groups/{groupId}/members/{userId}")
    suspend fun removeGroupMember(
        @Path("groupId") groupId: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Unit>>

    // --- Channels ---
    @POST("channels")
    suspend fun createChannel(@Body request: CreateChannelRequest): Response<ApiResponse<ChannelDto>>

    @POST("channels/{channelId}/subscribe")
    suspend fun subscribeChannel(@Path("channelId") channelId: String): Response<ApiResponse<Unit>>

    @DELETE("channels/{channelId}/subscribe")
    suspend fun unsubscribeChannel(@Path("channelId") channelId: String): Response<ApiResponse<Unit>>

    @GET("channels/{channelId}/posts")
    suspend fun getChannelPosts(
        @Path("channelId") channelId: String,
        @Query("limit") limit: Int = 30
    ): Response<ApiResponse<List<ChannelPostDto>>>

    @POST("channels/{channelId}/posts")
    suspend fun publishChannelPost(
        @Path("channelId") channelId: String,
        @Body request: PublishChannelPostRequest
    ): Response<ApiResponse<ChannelPostDto>>

    // --- Media Storage ---
    @POST("uploads/presign")
    suspend fun requestUploadPresignedUrl(
        @Body request: AttachmentUploadRequest
    ): Response<ApiResponse<AttachmentUploadResponse>>

    // --- Moderation & Reporting ---
    @POST("reports")
    suspend fun submitReport(@Body report: ReportDto): Response<ApiResponse<Unit>>

    @POST("users/block/{userId}")
    suspend fun blockUser(@Path("userId") userId: String): Response<ApiResponse<Unit>>

    @DELETE("users/block/{userId}")
    suspend fun unblockUser(@Path("userId") userId: String): Response<ApiResponse<Unit>>

    // --- System Health ---
    @GET("health")
    suspend fun checkHealth(): Response<Map<String, Any>>
}
