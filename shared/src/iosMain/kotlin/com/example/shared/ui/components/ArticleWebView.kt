package com.example.shared.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ArticleWebView(
    url: String,
    onPageLoaded: () -> Unit,
    onError: () -> Unit
) {

    UIKitView(
        factory = {

            val config = WKWebViewConfiguration()

            val webView = WKWebView(
                frame = platform.CoreGraphics.CGRectZero.readValue(),
                configuration = config
            )

            webView.navigationDelegate = object : NSObject(),
                WKNavigationDelegateProtocol {

                override fun webView(
                    webView: WKWebView,
                    didFinishNavigation: WKNavigation?
                ) {
                    onPageLoaded()
                }
            }

            val request = NSURLRequest.requestWithURL(
                NSURL.URLWithString(url)!!
            )

            webView.loadRequest(request)

            webView
        }
    )
}