package com.mindinventory.linkedinlogin.presentation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.mindinventory.linkedinlogin.R
import com.mindinventory.linkedinlogin.data.AccessTokenResponse
import com.mindinventory.linkedinlogin.data.LinkedInUserDetails
import com.mindinventory.linkedinlogin.data.UserEmailResponse
import com.mindinventory.linkedinlogin.data.UserListResponse
import com.mindinventory.linkedinlogin.domain.ApiParam
import com.mindinventory.linkedinlogin.domain.WebApiClient
import com.mindinventory.linkedinlogin.utils.KeyUtils
import com.mindinventory.linkedinlogin.utils.KeyUtils.AUTHORIZATION_CODE
import com.mindinventory.linkedinlogin.utils.KeyUtils.AUTHORIZATION_VALUE
import com.mindinventory.linkedinlogin.utils.KeyUtils.BEARER
import com.mindinventory.linkedinlogin.utils.KeyUtils.CLIENT_ID_KEY
import com.mindinventory.linkedinlogin.utils.KeyUtils.CODE
import com.mindinventory.linkedinlogin.utils.KeyUtils.COMMON_URL
import com.mindinventory.linkedinlogin.utils.KeyUtils.ERROR
import com.mindinventory.linkedinlogin.utils.KeyUtils.KEY_LINKEDIN_CONTENT
import com.mindinventory.linkedinlogin.utils.KeyUtils.REDIRECT_URI_KEY
import com.mindinventory.linkedinlogin.utils.KeyUtils.RESPONSE_TYPE
import com.mindinventory.linkedinlogin.utils.KeyUtils.SCOPE
import com.mindinventory.linkedinlogin.utils.KeyUtils.STATE
import com.mindinventory.linkedinlogin.utils.ShowToastUtils
import kotlinx.android.synthetic.main.activity_mi_linked_in.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MiLinkedInActivity : AppCompatActivity() {
    lateinit var cookieManager: CookieManager
    var userDetails = LinkedInUserDetails()
    var oAuthUrl: String? = null
    var clientId: String? = null
    var clientSecret: String? = null
    var redirectUri: String? = null
    var accessToken: String? = null
    var stateValue: String? = null
    var scopeValue: String? = null
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_linked_in)
        if (intent != null && intent.extras != null) {
            if (intent.hasExtra(KeyUtils.CLIENT_ID)) {
                clientId = intent.getStringExtra(KeyUtils.CLIENT_ID)
            }
            if (intent.hasExtra(KeyUtils.CLIENT_SECRET)) {
                clientSecret = intent.getStringExtra(KeyUtils.CLIENT_SECRET)
            }
            if (intent.hasExtra(KeyUtils.REDIRECT_URI)) {
                redirectUri = intent.getStringExtra(KeyUtils.REDIRECT_URI)
            }
            if (intent.hasExtra(KeyUtils.STATE_VALUE)) {
                stateValue = intent.getStringExtra(KeyUtils.STATE_VALUE)
            }
            if (intent.hasExtra(KeyUtils.SCOPE_VALUE_KEY)) {
                scopeValue = intent.getStringExtra(KeyUtils.SCOPE_VALUE_KEY)
            }
        }
        cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.removeAllCookies(null)
        val db = WebViewDatabase.getInstance(this)
        db.clearHttpAuthUsernamePassword()
        wvLinkIn.webViewClient = MyWebClient()
        oAuthUrl =
            "$COMMON_URL$RESPONSE_TYPE=$CODE&$CLIENT_ID_KEY=$clientId&$REDIRECT_URI_KEY=$redirectUri&$STATE=$stateValue&$SCOPE=$scopeValue"
        wvLinkIn.loadUrl(oAuthUrl)
    }


    inner class MyWebClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            cookieManager.setAcceptCookie(true)
        }

        @SuppressWarnings("deprecation")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            return handleUrl(url)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return handleUrl(request?.url.toString())
        }

        /**
         * If uri doesnot contain redirect uri then return false
         * If uri contains error then reload url again
         * If  uri state value didnot match with state value then returns false
         * If authorization code is null then return false otherwise call Access token  api using authorization code
         **/

        private fun handleUrl(url: String): Boolean {
            val uri = Uri.parse(url)
            if (!uri.toString().contains(redirectUri!!)) {
                return false
            } else if (uri.getQueryParameter(ERROR) != null) {
                wvLinkIn.loadUrl(oAuthUrl)
                uri.getQueryParameter(KeyUtils.ERROR_DESCRIPTION)?.toString()?.let {
                    ShowToastUtils.showToast(
                        this@MiLinkedInActivity, it
                    )
                    setResult(Activity.RESULT_CANCELED, Intent())
                    finish()
                }
                return false
            } else if (uri.getQueryParameter(STATE) != stateValue) {
                return false
            } else if (uri.getQueryParameter(CODE) == null) {
                return false
            } else {
                val code = uri.getQueryParameter(CODE)
                if (code != null) {
                    callAccessTokenApi(code)
                }
            }
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view?.settings?.useWideViewPort = false
        }
    }

    /**
     * Get access token api from linked in get request
     *
     * */
    private fun callAccessTokenApi(code: String) {
        val hashMap = HashMap<String, String>().apply {
            clientId?.let { put(ApiParam.CLIENT_ID, it) }
            put(ApiParam.CODE, code)
            redirectUri?.let { put(ApiParam.REDIRECT_URI, it) }
            put(ApiParam.GRANT_TYPE, AUTHORIZATION_CODE)
            clientSecret?.let { put(ApiParam.CLIENT_SECRET, it) }
        }

        WebApiClient.webApiTwitchAccessToken(ApiParam.BASE_URL)
            .callAccessTokenApi(AUTHORIZATION_VALUE, hashMap)
            .enqueue(object : Callback<AccessTokenResponse> {
                override fun onResponse(call: Call<AccessTokenResponse>?, response: Response<AccessTokenResponse>?) {
                    if (response != null) {
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                accessToken = response.body()?.accessToken.toString()
                                if (scopeValue == KeyUtils.ONLY_PROFILE_SCOPE || scopeValue == KeyUtils.BOTH_EMAIL_USERDETAILS_SCOPE_VALUE) {
                                    response.body()?.accessToken?.let { callUserDetailsApi(it) }
                                } else  if(scopeValue == KeyUtils.ONLY_EMAIL_SCOPE || scopeValue == KeyUtils.BOTH_EMAIL_USERDETAILS_SCOPE_VALUE){
                                    response.body()?.accessToken?.let { callUserEmailApi(it) }
                                }
                                else{
                                    setResult(Activity.RESULT_CANCELED, Intent())
                                    finish()
                                }
                            } else {
                                setResult(Activity.RESULT_CANCELED, Intent())
                                finish()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<AccessTokenResponse>?, t: Throwable?) {
                    t?.printStackTrace()
                }
            })
    }

    /**
     * Get user details api using access token(firstname,lastname,image)
     * */
    private fun callUserDetailsApi(accessToken: String) {
        WebApiClient.webApiTwitchAccessToken(ApiParam.BASE_URL_USER)
            .callUserDetailsApi("$BEARER $accessToken")
            .enqueue(object : Callback<UserListResponse> {
                override fun onResponse(call: Call<UserListResponse>?, response: Response<UserListResponse>?) {
                    if (response != null) {
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                val userDetailsResponse = response.body()
                                if (userDetailsResponse != null) {
                                    userDetailsResponse.firstName?.localized?.englishUs?.let {
                                        userDetails.firstName = it
                                    }
                                    userDetailsResponse.lastName?.localized?.englishUs?.let {
                                        userDetails.lastName = it
                                    }
                                    userDetailsResponse.profilePicture?.displayImageUrl?.elements?.get(0)
                                        ?.identifiers?.get(0)
                                        ?.identifier?.let { userDetails.image = it }
                                    if (scopeValue == KeyUtils.ONLY_EMAIL_SCOPE || scopeValue == KeyUtils.BOTH_EMAIL_USERDETAILS_SCOPE_VALUE) {
                                        callUserEmailApi(accessToken)
                                    } else {
                                        val intent = Intent()
                                        intent.putExtra(KEY_LINKEDIN_CONTENT, userDetails)
                                        setResult(Activity.RESULT_OK, intent)
                                        finish()
                                    }
                                }
                            } else {
                                setResult(Activity.RESULT_CANCELED, Intent())
                                finish()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<UserListResponse>?, t: Throwable?) {
                    t?.printStackTrace()
                }
            })
    }

    /**
     * Get Login user email
     * */
    private fun callUserEmailApi(accessToken: String) {
        WebApiClient.webApiTwitchAccessToken(ApiParam.BASE_URL_USER)
            .callUserEmailApi("$BEARER $accessToken")
            .enqueue(object : Callback<UserEmailResponse> {
                override fun onResponse(call: Call<UserEmailResponse>?, response: Response<UserEmailResponse>?) {
                    if (response != null) {
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                val userDetailsResponse = response.body()
                                if (userDetailsResponse != null) {
                                    val intent = Intent()
                                    userDetailsResponse.elements?.get(0)?.handleData?.emailAddress.let {
                                        userDetails.email = it
                                    }
                                    intent.putExtra(KEY_LINKEDIN_CONTENT, userDetails)
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                } else {
                                    setResult(Activity.RESULT_CANCELED, Intent())
                                    finish()
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<UserEmailResponse>?, t: Throwable?) {
                    t?.printStackTrace()
                }
            })
    }
}
