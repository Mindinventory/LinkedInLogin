package com.mindinventory.linkedinlogin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.mindinventory.linkedinlogin.data.LinkedInUserDetails
import com.mindinventory.linkedinlogin.domain.ApiParam
import com.mindinventory.linkedinlogin.domain.WebApiClient
import com.mindinventory.linkedinlogin.response.AccessTokenResponse
import com.mindinventory.linkedinlogin.response.UserEmailResponse
import com.mindinventory.linkedinlogin.response.UserListResponse
import kotlinx.android.synthetic.main.activity_mi_linked_in.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MiLinkedInActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE = 101
        const val KEY_LINKEDIN_CONTENT = "key_linkedin_detail_data"
        const val KEY_LINKEDIN_DETAIL_DATA = "key_linkedin_detail_data"
        fun startLinkedInActivityForDetails(
            activity: Activity,
            clientId: String,
            clientSecret: String,
            rediecturi: String,
            stateValue: String
        ) {
            val intent = Intent(activity, MiLinkedInActivity::class.java)
            intent.putExtra(KEY_LINKEDIN_CONTENT, KEY_LINKEDIN_DETAIL_DATA)
            intent.putExtra(CLIENT_ID, clientId)
            intent.putExtra(CLIENT_SECRET, clientSecret)
            intent.putExtra(REDIRECT_URI, rediecturi)
            intent.putExtra(STATE_VALUE, stateValue)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }
    }

    lateinit var cookieManager: CookieManager
    var userDeatails = LinkedInUserDetails()
    var OAUTHURL: String? = null
    var clientId = ""
    var clientSercet = ""
    var linkedInData = ""
    var redirectUri = ""
    var accessToken = ""
    var stateValue = ""
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_linked_in)
        val intent = intent
        if (intent != null && intent.extras != null) {
            val bundle = intent.extras
            if (bundle!!.containsKey(KEY_LINKEDIN_CONTENT)) {
                linkedInData = intent.getStringExtra(KEY_LINKEDIN_CONTENT)
            }
            if (bundle.containsKey(CLIENT_ID)) {
                clientId = intent.getStringExtra(CLIENT_ID)
            }
            if (bundle.containsKey(CLIENT_SECRET)) {
                clientSercet = intent.getStringExtra(CLIENT_SECRET)
            }
            if (bundle.containsKey(REDIRECT_URI)) {
                redirectUri = intent.getStringExtra(REDIRECT_URI)
            }
            if (bundle.containsKey(STATE_VALUE)) {
                stateValue = intent.getStringExtra(STATE_VALUE)
            }
        }
        cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.removeAllCookies(null)
        val db = WebViewDatabase.getInstance(this)
        db.clearHttpAuthUsernamePassword()
        wvLinkIn.webViewClient = MyWebClient()
        OAUTHURL =
            "$COMMON_URL$RESPONSE_TYPE=$CODE&$CLIENT_ID_KEY=$clientId&$REDIRECT_URI_KEY=$redirectUri&$STATE=$stateValue&$SCOPE=$SCOPE_VALUE"
        wvLinkIn.loadUrl(OAUTHURL)
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
            if (!uri.toString().contains(redirectUri)) {
                return false
            } else if (uri.getQueryParameter(ERROR) != null) {
                wvLinkIn.loadUrl(OAUTHURL)
                uri.getQueryParameter(ERROR)?.toString()?.let { showToast(this@MiLinkedInActivity, it) }
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
        val hashMap: HashMap<String, String> = HashMap()
        hashMap[ApiParam.CLIENT_ID] = clientId
        hashMap[ApiParam.CODE] = code
        hashMap[ApiParam.REDIRECT_URI] = redirectUri
        hashMap[ApiParam.GRANT_TYPE] = AUTHORIZATION_CODE
        hashMap[ApiParam.CLIENT_SECRET] = clientSercet
        WebApiClient.webApiTwitchAccessToken(ApiParam.BASE_URL)
            .callAccessTokenApi(AUTHORIZATION_VALUE, hashMap)
            .enqueue(object : Callback<AccessTokenResponse> {
                override fun onResponse(call: Call<AccessTokenResponse>?, response: Response<AccessTokenResponse>?) {
                    if (response != null) {
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                accessToken = response.body()?.access_token.toString()
                                response.body()?.access_token?.let { callUserDetailsApi(it) }
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
                                val userDetails = response.body()
                                var firstName: String? = ""
                                var lastName: String? = ""
                                if (userDetails != null) {
                                    if (userDetails.firstName?.localized?.en_US != null) {
                                        firstName = userDetails.firstName?.localized?.en_US
                                    }
                                    if (userDetails.lastName?.localized?.en_US != null) {
                                        lastName = userDetails.lastName?.localized?.en_US
                                    }
                                    val image = userDetails.profilePicture?.displayImageUrl?.elements?.get(0)
                                        ?.identifiers?.get(0)
                                        ?.identifier
                                    firstName?.let { userDeatails.firstName = it }
                                    lastName?.let { userDeatails.lastName = it }
                                    image?.let { userDeatails.image = it }
                                    callUserEmailApi(accessToken)
                                }
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
                                val userDetails = response.body()
                                if (userDetails != null) {
                                    val email = userDetails.elements?.get(0)?.handleData?.emailAddress
                                    val intent = Intent()
                                    email?.let { userDeatails.email = it }
                                    intent.putExtra(KEY_LINKEDIN_CONTENT, userDeatails)
                                    setResult(REQUEST_CODE, intent)
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
