package com.PlugPoint.plugpoint.networks

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Header

interface ImgurAPI {
    @Multipart
    @POST("3/image")
    suspend fun uploadImage(
        @Header("Authorization") authorization: String,
        @Part image: MultipartBody.Part
    ): Response<ImgurResponse>
}

data class ImgurResponse(
    val data: ImgurData,
    val success: Boolean,
    val status: Int
)

data class ImgurData(
    val id: String,
    val link: String
)
