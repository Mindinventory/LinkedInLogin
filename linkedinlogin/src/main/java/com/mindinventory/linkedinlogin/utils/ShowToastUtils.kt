package com.mindinventory.linkedinlogin.utils

import android.content.Context
import android.widget.Toast

object ShowToastUtils {
    fun showToast(context: Context, str: String) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }
}