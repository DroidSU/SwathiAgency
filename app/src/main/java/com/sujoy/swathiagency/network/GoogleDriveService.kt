package com.sujoy.swathiagency.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

interface GoogleDriveService {
    @GET
    suspend fun downloadCustomerFile(@Url fileUrl: String): Response<ResponseBody>

    @GET
    suspend fun getITCItemFile(@Url fileUrl: String) : Response<ResponseBody>

    @Multipart
    @POST("upload/drive/v3/files")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Query("uploadType") uploadType: String,
        @Query("parents") folderId: String
    ): Response<ResponseBody>
}