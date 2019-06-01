package com.mindinventory.linkedinsample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.RequestOptions
import com.mindinventory.linkedinlogin.builder.LinkedInBuilder
import com.mindinventory.linkedinlogin.data.LinkedInUserDetails
import com.mindinventory.linkedinlogin.utils.KeyUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonLogin.setOnClickListener(this)
    }

    /**
     * Here pass client_id,client_secret,redirecturi and state_value*/
    override fun onClick(v: View?) {
        val intent = LinkedInBuilder.Builder(this)
            .setClientId(getString(R.string.client_id))//CLIENT_ID
            .setClientSecret(getString(R.string.client_secret))//CLIENT_SECRET
            .setRedirectUri(getString(R.string.redirect_uri))//REDIRECT_URI
            .setStateValue(getString(R.string.state_value))//STATE_VALUE
            .setScopeValue(KeyUtils.BOTH_EMAIL_USERDETAILS_SCOPE_VALUE)//PASS_SCOPE_VALUE_HERE
            .build()
        startActivityForResult(intent, KeyUtils.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                KeyUtils.REQUEST_CODE -> {
                    val linkedInUser =
                        data.getParcelableExtra<LinkedInUserDetails>(KeyUtils.KEY_LINKEDIN_CONTENT)
                    if (linkedInUser != null) {
                        buttonLogin.visibility = View.GONE
                        tvUserDetails.text =
                            "${linkedInUser.firstName}${linkedInUser.lastName}\n\n${linkedInUser.email}"
                        ivUserImage.loadImage(linkedInUser.image, RequestOptions.circleCropTransform())
                    } else {
                        //handle the error
                    }
                }
            }
        }
        else{
            //Login failed handle error
        }
    }
}

