package com.mindinventory.linkedinlogin.data

import com.google.gson.annotations.SerializedName

class AccessTokenResponse {
    @SerializedName("access_token")
    var accessToken: String? = null
}