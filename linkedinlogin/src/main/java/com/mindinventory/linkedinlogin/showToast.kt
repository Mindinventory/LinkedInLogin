package com.mindinventory.linkedinlogin

import android.content.Context
import android.widget.Toast

fun showToast(context: Context, str: String) {
    Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
}