package com.demomiru.tokeiv2

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException

//class MoviePlayActivity : AppCompatActivity() {
//
//
//    @SuppressLint("SetJavaScriptEnabled")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_movie_play)
//
//        val webView : WebView = findViewById(R.id.web_view)
//        webView.loadUrl("https://vidsrc.me/embed/movie?imdb=tt5433140")
//        webView.settings.javaScriptEnabled = true
//        webView.webChromeClient
//        webView.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                view?.loadUrl(request?.url.toString())
//                return true
//            }
//        }
//
//    }
//}

class MoviePlayActivity : AppCompatActivity() {
    private lateinit var webView : WebView
    private val args : MoviePlayActivityArgs by navArgs()
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_movie_play)
        val id = args.tmdbID
        webView = findViewById(R.id.web_view)

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
//            webView.loadUrl("https://vidsrc.me/embed/movie?tmdb=$id")
            val url = "https://vidsrc.to/embed/movie/$id"
            webView.loadUrl(url)
//            checkStatusCode(url)
        }

        webView.settings.javaScriptEnabled = true
        webView.webChromeClient
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

//    private fun checkStatusCode(url: String) {
//        Thread {
//            try {
//                val response = Jsoup.connect(url)
//                    .method(Connection.Method.HEAD)
//                    .execute()
//                if (response.statusCode() != 404) {
//                    runOnUiThread {
//                        webView.loadUrl(url)
//                    }
//                }
//                else {
//                    finish()
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }.start()
//    }

//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            hideSystemUI()
//        }
//    }
//
//    private fun hideSystemUI() {
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
//                // Set the content to appear under the system bars so that the
//                // content doesn't resize when the system bars hide and show.
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                // Hide the nav bar and status bar
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_FULLSCREEN)
//    }
}

