package com.example.newsapp.ui.components


import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
    var webView: WebView? = null

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {

            WebView(context).apply {

                webView = this

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, progress: Int) {
                        if (progress > 60) {
                            onPageLoaded()
                        }
                    }
                }

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadsImagesAutomatically = true
                    blockNetworkImage = false
                    cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    setSupportZoom(false)
                    mediaPlaybackRequiresUserGesture = true
                    databaseEnabled = true
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