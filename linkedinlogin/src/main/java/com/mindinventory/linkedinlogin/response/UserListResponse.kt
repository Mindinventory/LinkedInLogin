package com.mindinventory.linkedinlogin.response

import com.google.gson.annotations.SerializedName


class UserListResponse {
    var firstName: FirstName? = null
    var lastName: LastName? = null
    var profilePicture: ProfilePicture? = null
    var id: String? = null

    class FirstName {
        var localized: Localized? = null
    }

    class Localized {
        var en_US: String? = null
    }

    class LastName {
        var localized: Localized? = null
    }

    class ProfilePicture {
        var displayImage: String? = null
        @SerializedName("displayImage~")
        var displayImageUrl: DisplayImage? = null
    }

    class DisplayImage {
        @SerializedName("elements")
        val elements: Array<Elements>? = null
    }

    class Elements {
        val identifiers: Array<Identifiers>? = null
    }

    class Identifiers {
        val identifier: String? = null
    }
}