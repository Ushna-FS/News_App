package com.example.newsapp.ui.components

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ArticleWebView(
    url: String,
    onPageLoaded: () -> Unit,
    onError: () -> Unit
) {
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            WebView(context).apply {
                webView = this

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, progress: Int) {
                        // Trigger when some content is visible (progress > 20%)
                        if (progress > 20) {
                            onPageLoaded()
                            // Enable network images once initial content is visible
                            settings.blockNetworkImage = false
                        }
                    }
                }

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadsImagesAutomatically = true
                    blockNetworkImage = true   // Initially block images
                    cacheMode = WebSettings.LOAD_DEFAULT
                    setSupportZoom(false)
                    mediaPlaybackRequiresUserGesture = true
                    databaseEnabled = true
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        // Ensure images load after page finished if not already
                        view?.settings?.blockNetworkImage = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        onError()
                    }
                }

                loadUrl(url)
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.destroy()
        }
    }
}