package com.mindinventory.linkedinsample


import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

fun ImageView.loadImage(source: String?, requestOptions: RequestOptions? = null) {
    source?.let { url ->
        val requestBuilder = Glide.with(context).load(url)
        requestOptions?.let {
            requestBuilder.apply(it)
        }
        requestBuilder.into(this)
    }
}