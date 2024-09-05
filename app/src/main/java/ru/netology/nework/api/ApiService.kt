package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.auth.AuthPair
import ru.netology.nework.dto.*

interface ApiService {
    //POSTS
    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatestPosts(@Query("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getPostsAfter(
        @Path("post_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("posts/{post_id}/before")
    suspend fun getPostsBefore(
        @Path("post_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @POST("posts")
    suspend fun savePost(@Header("auth") auth: String, @Body post: Post): Response<Post>

    @POST("posts/{id}/likes ")
    suspend fun likePostById(@Header("auth") auth: String, @Path("id") id: Int): Response<Post>

    @DELETE("posts/{id}/likes ")
    suspend fun dislikePostById(@Header("auth") auth: String, @Path("id") id: Int): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removePostById(@Header("auth") auth: String, @Path("id") id: Int): Response<Unit>

    //EVENTS
    @GET("events")
    suspend fun getAllEvents(): Response<List<Event>>

    @GET("events/latest")
    suspend fun getLatestEvents(@Query("count") count: Int): Response<List<Event>>

    @GET("events/{id}/after")
    suspend fun getEventsAfter(
        @Path("event_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @GET("events/{event_id}/before")
    suspend fun getEventsBefore(
        @Path("event_id") id: String,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @POST("events")
    suspend fun saveEvent(@Header("auth") auth: String, @Body event: Event): Response<Event>

    @POST("events/{id}/likes ")
    suspend fun likeEventById(@Header("auth") auth: String, @Path("id") id: Int): Response<Event>

    @DELETE("events/{id}/likes ")
    suspend fun dislikeEventById(@Header("auth") auth: String, @Path("id") id: Int): Response<Event>

    @POST("events/{id}/participants")
    suspend fun participateEventById(@Header("auth") auth: String, @Path("id") id: Int): Response<Event>

    @DELETE("events/{id}/participants ")
    suspend fun unparticipateEventById(@Header("auth") auth: String, @Path("id") id: Int): Response<Event>

    @DELETE("events/{id}")
    suspend fun removeEventById(@Header("auth") auth: String, @Path("id") id: Int): Response<Unit>

    //USER WALL
    @GET("{author_id}/wall")
    suspend fun getUserWall(
        @Path("author_id") id: Int
    ): Response<List<Post>>

    @GET("{author_id}/wall/latest")
    suspend fun getUserWallLatest(
        @Query("count") count: Int,
        @Path("author_id") id: Int
    ): Response<List<Post>>

    @GET("{author_id}/wall/{post_id}/after")
    suspend fun getUserWallAfter(
        @Path("author_id") userId: Int,
        @Path("post_id") postId: Int,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("{author_id}/wall/{post_id}/before")
    suspend fun getUserWallBefore(
        @Path("author_id") userId: Int,
        @Path("post_id") postId: Int,
        @Query("count") count: Int,
    ): Response<List<Post>>

    //MEDIA UPLOAD
    @Multipart
    @POST("media")
    suspend fun upload(
        @Header("auth") auth: String,
        @Part file: MultipartBody.Part
    ): Response<MediaUpload>

    //JOBS
    @GET("my/jobs/")
    suspend fun getMyJobs(@Header("auth") auth: String): Response<List<Job>>

    @GET("{user_id}/jobs/")
    suspend fun getPersonJobs(@Path("user_id") id: String): Response<List<Job>>

    @POST("my/jobs/")
    suspend fun addNewJob(@Header("auth") auth: String, @Body job: Job): Response<Job>

    @DELETE("my/jobs/{job_id}/")
    suspend fun removeJob(@Header("auth") auth: String, @Path("job_id") id: String): Response<Unit>

    //USERS
    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @FormUrlEncoded
    @POST("users/authentication/")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("password") password: String
    ): Response<AuthPair>

    @FormUrlEncoded
    @POST("users/registration/")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("name") name: String,
    ): Response<AuthPair>

    @POST("users/registration/")
    suspend fun registerUserWithAvatar(
        @Body body: MultipartBody
    ): Response<AuthPair>
}