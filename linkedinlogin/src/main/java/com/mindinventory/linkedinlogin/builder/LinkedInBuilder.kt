package com.mindinventory.linkedinlogin.builder

import android.content.Context
import android.content.Intent
import com.mindinventory.linkedinlogin.presentation.MiLinkedInActivity
import com.mindinventory.linkedinlogin.utils.KeyUtils

class LinkedInBuilder {
    class Builder(private var context: Context) {
        var clientId: String? = null
        var clientSecret: String? = null
        var redirectUri: String? = null
        var stateValue: String? = null
        var scopeValue:String?=null

        fun setClientId(clientId: String): Builder {
            this.clientId = clientId
            return this
        }

        fun setClientSecret(clientSecret: String): Builder {
            this.clientSecret = clientSecret
            return this
        }

        fun setRedirectUri(redirectUri: String): Builder {
            this.redirectUri = redirectUri
            return this
        }

        fun setStateValue(stateValue: String): Builder {
            this.stateValue = stateValue
            return this
        }

        fun setScopeValue(scopeValue:String):Builder{
            this.scopeValue=scopeValue
            return this
        }

        fun build(): Intent {
            val intent = Intent(context, MiLinkedInActivity::class.java)
            clientId?.let { intent.putExtra(KeyUtils.CLIENT_ID, it) }
            clientSecret?.let { intent.putExtra(KeyUtils.CLIENT_SECRET, it) }
            redirectUri?.let { intent.putExtra(KeyUtils.REDIRECT_URI, it) }
            stateValue?.let { intent.putExtra(KeyUtils.STATE_VALUE, it) }
            scopeValue?.let { intent.putExtra(KeyUtils.SCOPE_VALUE_KEY,it) }
            return intent
        }
    }
}