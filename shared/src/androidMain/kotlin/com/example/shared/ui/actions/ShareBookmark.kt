package com.example.shared.ui.actions

import android.content.Intent
import android.content.Context

lateinit var appContext: Context

fun initializeAppContext(context: Context) {
    appContext = context.applicationContext
}

actual fun shareBookmark(text: String) {

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val chooserIntent = Intent.createChooser(sendIntent, "Share via").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    appContext.startActivity(chooserIntent)
}