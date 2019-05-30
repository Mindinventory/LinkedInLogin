package com.mindinventory.linkedinlogin.data

import com.google.gson.annotations.SerializedName

class UserEmailResponse {
    val elements: ArrayList<Elements>? = null

    class Elements {
        @SerializedName("handle~")
        var handleData: Handle? = null
    }

    class Handle {
        val emailAddress: String? = null
    }
}