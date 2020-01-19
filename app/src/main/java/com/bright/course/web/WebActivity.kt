package com.bright.course.web

import android.content.Context
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bright.course.BaseEventBusActivity
import com.bright.course.R
import com.bright.course.http.APICallback
import com.bright.course.http.APIService
import com.bright.course.http.response.ResponseDataT
import com.bright.course.http.response.ResponseExam
import kotlinx.android.synthetic.main.activity_web.*
import org.jetbrains.anko.intentFor

/**
 * Created by kim on 2018/10/22.
 *
 */
class WebActivity : BaseEventBusActivity() {
    var webView: WebView? = null
    private val defaultUrl: String = "http://www.baidu.com"

    companion object {
        fun launch(context: Context) {
            context.startActivity(context.intentFor<WebActivity>())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        initWebView()

        btnClose.setOnClickListener {
            finish()
        }

        btnDone.setOnClickListener {
            if (etUrl.text.isNotEmpty()) {
                webView?.loadUrl(etUrl.text.toString())
            }
        }

        APIService.create().fetchExam("20").enqueue(object : APICallback<ResponseDataT<ResponseExam>>() {
            override fun onFinish(msg:String) {

            }

            override fun onSuccess(response: ResponseDataT<ResponseExam>?) {

            }

        })
    }

    private fun initWebView() {
        webView = WebView(this)
        webViewContainer.addView(webView)


        val settings = webView?.getSettings()
        settings?.javaScriptEnabled = true
        settings?.blockNetworkImage = true
        settings?.domStorageEnabled = true

        settings?.setJavaScriptCanOpenWindowsAutomatically(true)
        webView?.getSettings()?.loadWithOverviewMode = true
        webView?.getSettings()?.builtInZoomControls = true
        webView?.getSettings()?.displayZoomControls = false

        webView?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                progressBar.progress = progress
                if (progress == 100) {
                    //                    renderTitle(webView.getTitle())
                    supportInvalidateOptionsMenu()
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }
        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                view.settings.blockNetworkImage = false
                if (view.canGoBack()) {
                }
                super.onPageFinished(view, url)
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                handler.proceed()
            }
        }

        webView?.loadUrl(defaultUrl)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            webView?.loadUrl("about:blank");
            webView?.destroy()
            webViewContainer.removeAllViews()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}