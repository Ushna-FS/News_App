package com.example.shared.ui.actions

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual fun shareBookmark(text: String) {

    val activityVC = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null
    )

    val controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    controller?.presentViewController(activityVC, true, null)
}