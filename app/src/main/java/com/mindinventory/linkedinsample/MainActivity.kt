package com.mindinventory.linkedinsample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.RequestOptions
import com.mindinventory.linkedinlogin.data.LinkedInUserDetails
import com.mindinventory.linkedinlogin.presentation.MiLinkedInActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonLogin.setOnClickListener(this)
    }

    /**
     * Here pass client_id,client_secret,redirecturi and state_value*/
    override fun onClick(v: View?) {
        MiLinkedInActivity.startLinkedInActivityForDetails(
            this,
            getString(R.string.client_id),//CLIENT_ID
            getString(R.string.client_secret),//CLIENT_SECRET
            getString(R.string.redirect_uri),//REDIRECT_URI
            getString(R.string.state_value)//STATE_VALUE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MiLinkedInActivity.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val intent = data?.extras
                    if (intent != null) {
                        when {
                            intent.containsKey(MiLinkedInActivity.KEY_LINKEDIN_DETAIL_DATA) -> {
                                val linkedInUser =
                                    data.getParcelableExtra<LinkedInUserDetails>(MiLinkedInActivity.KEY_LINKEDIN_DETAIL_DATA)
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
                }
            }
        }
    }
}
