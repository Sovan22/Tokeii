package com.demomiru.tokeiv2

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent

import android.view.View
import android.view.ViewGroup

import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import android.widget.VideoView

import androidx.navigation.navArgs
import coil.load
import com.demomiru.tokeiv2.utils.fixHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class MoviePlayActivity : AppCompatActivity() {
    private lateinit var webView : WebView
    private  var fullscreenContainer: FullscreenHolder? = null
//    private lateinit var one:LinearLayout
//    private lateinit var two:LinearLayout
//    private lateinit var three:LinearLayout
//    private lateinit var four:LinearLayout
    private lateinit var videoView: VideoView
    private lateinit var loading:ProgressBar
//    private var isOpen = true
//    private var play = true
    private val COVER_SCREEN_PARAMS = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    private lateinit var url : String
//    private lateinit var ppButton : ImageButton
    private val args : MoviePlayActivityArgs by navArgs()
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_movie_play)
//        ppButton = findViewById(R.id.videoView_play_pause_btn)
//        one = findViewById(R.id.videoView_one_layout)
//        two = findViewById(R.id.videoView_two_layout)
//        three = findViewById(R.id.videoView_three_layout)
//        four = findViewById(R.id.videoView_four_layout)

//        val z_layout = findViewById<RelativeLayout>(R.id.z_layout)
//        z_layout.setOnClickListener{
//            if(isOpen){
//                one.visibility = View.GONE
//                two.visibility = View.GONE
//                three.visibility = View.GONE
//                four.visibility = View.GONE
//                isOpen = false
//            }
//            else{
//                one.visibility = View.VISIBLE
//                two.visibility = View.VISIBLE
//                three.visibility = View.VISIBLE
//                four.visibility = View.VISIBLE
//                isOpen = true
//            }
//        }

//        hideSystemUI()
        videoView = findViewById(R.id.video_view)
        loading = findViewById(R.id.loading_content)
        val id = args.tmdbID
        val type = args.type
        val episode = args.episodeN
        val season = args.seasonN
        webView = findViewById(R.id.web_view)
//        AdBlockerWebView
        webView.settings.javaScriptEnabled = true
        webView.settings.displayZoomControls = true
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return request?.url.toString() != view?.url
            }
        }

        webView.webChromeClient =  object : WebChromeClient() {

            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                // Make the WebView invisible
                webView.visibility = View.GONE

                // Create a new view to hold the video display
                val decorView = window.decorView as FrameLayout
                fullscreenContainer = FullscreenHolder(this@MoviePlayActivity)
                fullscreenContainer?.addView(view, COVER_SCREEN_PARAMS)
                decorView.addView(fullscreenContainer, COVER_SCREEN_PARAMS)
            }

            override fun onHideCustomView() {

                val decorView = window.decorView as FrameLayout
                decorView.removeView(fullscreenContainer)
                fullscreenContainer = null

                webView.visibility = View.VISIBLE
                super.onHideCustomView()
            }
        }


        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
//            webView.loadUrl("https://vidsrc.me/embed/movie?tmdb=$id")
//            val video = "<iframe id=\"the_frame\" src=\"//vidsrc.me/srcrcp/YmIzZjhhOWNhYTU5NDJiOTliYTBmOGE3OGY3M2M2MDU6UWtwaU1rZFpVR2RTSzFwdE5tWnRhM2hrTUVOMlNXdEZkMUpPV0ZsR1dqSnhhRmwxUjIwek5UbG9RVU50YjNGQ1VIRTVWVEZXTldkalpWbGlaRFYyVEVaelpqRlhZWFZaUkVOM2R6QjVRbWcwY1RaamFuWmxOa2N2ZWpGV2QzTnpVMVYxYlhCNmIzVkJjRzR5Y0VrNFkwbzBTQ3RuYUROTlNXYzVSakZLTVhSSFJWVjZjRzloT1VGU1QwZEJNMjFCWTJ4c1ZIRndNMDl3VlU5SGIzTkRiMWxUTm1oNGVtRTFVMU5ZWWxnNVZrbzVOaTgxVlZOcGFHMTRVemhoV2s4eVIwMTVURTlpZUc1U2RsRXZjVmx1SzBaaFFsVjZTWE42V1ZwaVRXVTJabXBJVTAxblVrNXFiVFJyY1ZCemMxbHJaa1Z3Y0hSaFdVOTRObGxSWTJ4MlJEUklkVE0wTTBoU1ppOTFXazlNV2tRemFtZDRXa3BtVjNwT1JUbDFlSGRxYmpRcmVYY3dPVWxNYTFCbVNuRnJkV3N5VjA1WldsWkhOazk0ZVRRdlFrTXpOUzg1UVRWblNqbFViWEJKZUhoTVVISmphRXhwWlZCQ1VYSmlORWhVU20xWmJDdFBZalJVYm1WMmNGRjZUR3BsV0RsT09IWXZTMFJITlRkalFqQmFNekpXY0hOYWIwd3hMekZ2UzJoeFZXMHdRaXQwV0dwWWVXc3ZVRGhGUTA1RGVHZ3hiMmRrUW5NMU1raHRVRmh2WTNCNlpFSjFXbFZVUW1KSllWb3lVakZSV21SRlIyaEdZMEZpWjA0eFVtdGlla3BwZVdaQldVZFlheXRwVkcxM2IwdFllRkZWWW5sVVVUUldha1l2WnpBMFRETjRkeTlsYzNvck0wRk1RVlZJTTJaM2RHbFdkVlZMZG5kV1dsSlllVFYxUXpaNFYwSXZkVFJRV0V4aFVVVlVTRk5IUW5KVFVuSlhjMUphV0RsbFEzQTVVaXMxZDAxb1lTdDVWREZX\" frameborder=\"0\" scrolling=\"no\" allowfullscreen=\"yes\" style=\"height: 100%; width: 100%;\" onload=\"remove_loading()\"></iframe>"
//            webView.loadData(video,"text/html","utf-8")

            url = if(type == "movie"){
//                "https://vidsrc.me/embed/movie?tmdb=$id/"
                webView.webViewClient = WebViewClient()
                "https://multiembed.mov/directstream.php?video_id=$id&tmdb=1"}
            else
                "https://vidsrc.me/embed/tv?tmdb=$id&season=$season&episode=$episode"


//
//            url = if(type == "movie")
//                "https://vidsrc.to/embed/movie/$id"
//            else
//                "https://vidsrc.to/embed/tv/$id/$season/$episode"
            webView.loadUrl(url)




        }

//        GlobalScope.launch (Dispatchers.IO){
//            val videoUrl= sendGetRequest("https://loon-neat-troll.ngrok-free.app/scrape?id=$id").replace("\"", "")
//            Log.i("final",videoUrl)
//            withContext(Dispatchers.Main){
////
////                val mediaController = MediaController(this@MoviePlayActivity)
////                mediaController.setAnchorView(videoView)
////                videoView.setMediaController(mediaController)
//
//                // Set the video URI and start playback
//                val videoUri = Uri.parse(videoUrl)
//                videoView.setVideoURI(videoUri)
//                loading.visibility = View.GONE
//                videoView.start()
//                ppButton.setOnClickListener{
//                    play = if(play){
//                        videoView.pause()
//                        ppButton.load(R.drawable.baseline_play_arrow_24)
//                        false
//                    }else{
//                        videoView.resume()
//                        ppButton.load(R.drawable.netflix_pause_button)
//                        true
//                    }
//
//                }
//            }
//        }


    }

    private fun sendGetRequest(url: String) : String {
        val client  = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS) // Set connection timeout
            .readTimeout(20, TimeUnit.SECONDS)    // Set read timeout
            .build()
        val request = Request.Builder().url(url).addHeader("ngrok-skip-browser-warning","20").build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body()?.string()
            Log.i("response", responseBody?:"systemHang")
            return responseBody?:""
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}

class FullscreenHolder(ctx: Context) : FrameLayout(ctx) {
    init {
        setBackgroundColor(ctx.resources.getColor(android.R.color.black))
    }

    override fun onTouchEvent(evt: MotionEvent): Boolean {
        return true
    }

}


