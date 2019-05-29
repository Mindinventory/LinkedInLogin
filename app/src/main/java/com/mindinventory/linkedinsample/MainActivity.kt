package com.mindinventory.linkedinsample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.RequestOptions
import com.mindinventory.linkedinlogin.MiLinkedInActivity
import com.mindinventory.linkedinlogin.data.LinkedInUserDetails
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
        MiLinkedInActivity.startLinkedInActivityForDetails(
            this,
            "CLIENT_ID",
            "CLIENT_SECRET",
            "REDIRECT_URI",
            "STATE_VALUE"
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MiLinkedInActivity.REQUEST_CODE -> {
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
                            }
                        }
                    }
                }
            }
        }
    }
}
