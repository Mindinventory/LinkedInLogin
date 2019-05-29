package com.mindinventory.linkedinlogin.domain

import com.mindinventory.linkedinlogin.domain.ApiParam.AUTHORIZATION
import com.mindinventory.linkedinlogin.domain.ApiParam.CONTENT_TYPE
import com.mindinventory.linkedinlogin.response.AccessTokenResponse
import com.mindinventory.linkedinlogin.response.UserEmailResponse
import com.mindinventory.linkedinlogin.response.UserListResponse

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.QueryMap
import java.util.HashMap
interface WebApi {
    @GET("me?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))")
    fun callUserDetailsApi(@Header(AUTHORIZATION) authorization: String): Call<UserListResponse>

    @GET("emailAddress?q=members&projection=(elements*(handle~))")
    fun callUserEmailApi(@Header(AUTHORIZATION) authorization: String): Call<UserEmailResponse>

    @POST("accessToken")
    fun callAccessTokenApi(@Header(CONTENT_TYPE) authorization: String, @QueryMap hashMap: HashMap<String, String>): Call<AccessTokenResponse>
}