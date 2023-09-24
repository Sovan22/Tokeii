package com.demomiru.tokeiv2

import android.annotation.SuppressLint
import android.content.Context

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent

import android.view.View
import android.view.ViewGroup


import android.webkit.WebChromeClient

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout

import android.widget.ProgressBar
import android.widget.Toast


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope


import androidx.media3.common.Player.*
import com.demomiru.tokeiv2.utils.SuperstreamUtils
import com.demomiru.tokeiv2.utils.clickMiddle
import com.demomiru.tokeiv2.utils.getMovieImdb
import com.demomiru.tokeiv2.utils.getMovieLink
import com.demomiru.tokeiv2.utils.getTvImdb
import com.demomiru.tokeiv2.utils.getTvLink


import com.demomiru.tokeiv2.utils.passVideoData


import com.demomiru.tokeiv2.watching.ContinueWatching

import com.demomiru.tokeiv2.watching.VideoData
import com.lagradost.nicehttp.Requests

import kotlinx.coroutines.launch



@Suppress("DEPRECATION")
class MoviePlayActivity : AppCompatActivity(){
    private lateinit var webView : WebView
    private  var fullscreenContainer: FullscreenHolder? = null
    private var urlNo = 0
    private var isSuper = false
    private var superId: Int? = null
    private var IMDBid: String? = null

//    private val database by lazy { ContinueWatchingDatabase.getInstance(this) }
//    private val watchHistoryDao by lazy { database.watchDao() }

//    private lateinit var player: ExoPlayer
//    private lateinit var videoView: PlayerView
    private val superStream = SuperstreamUtils()
    private lateinit var loading:ProgressBar
    private lateinit var id:String
    private var clickedMiddle = false
    private var season: Int = 1
    private var episode: Int = 1
    private var origin : String = ""
    private var seekProgress : Int = 0
    private var imgLink : String? = null
    private lateinit var title:String
    private var type : String? = null
    private val videoUrl = MutableLiveData<String?>()
    private var subUrl : String? = null

    private val COVER_SCREEN_PARAMS = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    private lateinit var url : String

//    private val args : MoviePlayActivityArgs by navArgs()
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_movie_play)
        Log.i("Start", "Time")

        val bundle = intent.extras
        type = bundle?.getString("type")
        when (type) {
        "movie" -> {
            val data = bundle?.getSerializable("Data") as? Movie
            origin = data!!.original_language
            id = data.id
            title = data.title
            imgLink = data.poster_path
        }
        "tvshow" -> {
            val data =  bundle?.getSerializable("Data") as? Episode

            id = bundle?.getString("id")!!
            title = bundle.getString("showTitle")!!
            imgLink = bundle.getString("poster")

            season = data!!.season_number.toInt()
            episode = data.episode_number.toInt()

        }
        else -> {
            val data = bundle?.getSerializable("Data") as? ContinueWatching
            id = data!!.tmdbID.toString()
            title = data.title
            imgLink = data.imgLink
            seekProgress = data.progress
            type = data.type
            if (type == "tvshow"){
                season = data.season
                episode = data.episode
            }
        }
    }


//        videoView = findViewById(R.id.video_view)
        loading = findViewById(R.id.loading_content)

//        id = args.tmdbID
//
//        val type = args.type
//
//        val episode = args.episodeN
//        val season = args.seasonN


        webView = findViewById(R.id.web_view)
        loading.visibility = View.VISIBLE
//        player = ExoPlayer.Builder(this).build()

//        videoView.player = player

//        AdBlockerWebView
        webView.settings.javaScriptEnabled = true
        webView.settings.displayZoomControls = true
//        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return request?.url.toString() != view?.url
            }

//            override fun onPageFinished(view: WebView?, url: String?) {
//                webView.evaluateJavascript("javascript:document.getElementByTagName('video')[0].src") {
//                    Log.i("videoLink", it)
//                }
//                super.onPageFinished(view, url)
//            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if (request?.url.toString().endsWith(".m3u8")){
                    url+=1
                    Log.i("Video Link",request?.url.toString())
                    lifecycleScope.launch {
                        videoUrl.value = request?.url.toString()
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        videoUrl.observe(this) { hlsUri ->
            if (!hlsUri.isNullOrEmpty()) {
                webView.visibility = View.GONE
//                videoView.visibility = View.VISIBLE
                //send to VideoPlayActivity
//                player.setMediaItem(MediaItem.fromUri(hlsUri))
//                player.prepare()
//                player.playWhenReady = true

//                loading.visibility = View.GONE
                videoUrl.removeObservers(this)
               val intent =  passVideoData(VideoData(
                    seekProgress,
                    imgLink!!,
                    id.toInt(),
                    IMDBid,
                    title,
                    episode,
                    season,
                    type!!,
                    hlsUri,
                    superId,
                   subUrl
                   ),this)
                intent.putExtra("origin", origin)
                intent.putExtra("superstream",isSuper)
                startActivity(intent)
                finish()
            }
        }

//    val listener = object : Listener{
//        @SuppressLint("UnsafeOptInUsageError")
//        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
//            if (player.duration != C.TIME_UNSET) {
//                // Duration is available
//                val seek = player.duration / 100 * seekProgress
//                player.seekTo(seek)
//                videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
//            }
//            super.onTimelineChanged(timeline, reason)
//        }
//    }
//    player.addListener(listener)



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

//        if(origin)
            url = if(type == "movie"){
//                "https://vidsrc.me/embed/movie?tmdb=$id/"
                webView.webViewClient = object : WebViewClient() {

//                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                        return request?.url.toString() != view?.url
//                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        if (request?.url.toString().endsWith("playlist.m3u8"))
//                        if (request?.url.toString().endsWith("playlist.m3u8"))
                        {
//                    Log.i("Video Link","Found")
                            lifecycleScope.launch {
                                videoUrl.value = request?.url.toString()
                            }
                        }
//                        else if(request?.url.toString().contains(".mp4")){
//                            lifecycleScope.launch {
//                                videoUrl.value = request?.url.toString()
//                            }
//                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                }
//                "https://vidsrc.me/embed/movie?tmdb=$id"
                    "https://multiembed.mov/directstream.php?video_id=$id&tmdb=1"
//                "https://movie-web.app/media/tmdb-movie-$id"
            }
            else{
//                "https://vidsrc.me/embed/tv?tmdb=$id&season=$season&episode=$episode"
                "https://vidsrc.to/embed/tv/$id/$season/$episode"
//                " https://multiembed.mov/directstream.php?video_id=$id&tmdb=1&s=$season&e=$episode"
            }


//
//            url = if(type == "movie")
//                "https://vidsrc.to/embed/movie/$id"
//            else
//                "https://vidsrc.to/embed/tv/$id/$season/$episode"

            if(type == "tvshow"){
                lifecycleScope.launch {
                    val imdbId = getTvImdb(id)
                    if(imdbId.isNotBlank()){
                        origin = "hi"
                        IMDBid = imdbId
                        videoUrl.value = getTvLink(imdbId,season-1,episode-1)?:""
                    }
                    else{


                        //Superstream add
                        val mainData = superStream.search(title)
                        superId = mainData.data[0].id
                        if (superId != null) {
                            isSuper = true
                            val tvLinks = superStream.loadLinks(false, superId!!, season, episode)
                            tvLinks.data?.list?.forEach {
                                if(!it.path.isNullOrBlank()){
                                    println("${it.quality} : ${it.path}")
                                    if(it.quality == "720p") {
//                                        val subtitle = superStream.loadSubtile(false,it.fid!!,superId!!,season,episode)
//                                        subtitle.data?.list?.forEach {sub->
//                                            if(sub.language == "English"){
////                                                subUrl = sub.subtitles[0].filePath
//                                                sub.subtitles.forEach { s->
//                                                    if (!s.filePath.isNullOrBlank()) {
//                                                        subUrl = s.filePath
//                                                        println("Sub Url : $subUrl")
//                                                        return@forEach
//                                                    }
//                                                }
//
//                                                return@forEach
//                                            }
//                                        }
                                        videoUrl.value = it.path
                                        return@forEach
                                    }
                                }
                            }
                        }
                        else{
                            println("Not Available")
                        }

                    }
                }
            }else {


                if (origin != "hi") {
                    webView.loadUrl(url)
//                    clickMiddle(webView,1000)
//                    clickMiddle(webView, 5000)
//                    clickedMiddle = true
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        if (clickedMiddle && videoUrl.value.isNullOrBlank()) {
//                            Toast.makeText(
//                                this@MoviePlayActivity,
//                                "Not Released Yet",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            finish()
//                        }
//                    }, 20000)
                }
                else {
                    lifecycleScope.launch {
                        val imdbId = getMovieImdb(id)
                        IMDBid = imdbId
                        videoUrl.value = getMovieLink(imdbId) ?: ""

                    }
                }
            }



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

//    private fun sendGetRequest(url: String) : String {
//        val client  = OkHttpClient.Builder()
//            .connectTimeout(20, TimeUnit.SECONDS) // Set connection timeout
//            .readTimeout(20, TimeUnit.SECONDS)    // Set read timeout
//            .build()
//        val request = Request.Builder().url(url).addHeader("ngrok-skip-browser-warning","20").build()
//
//        client.newCall(request).execute().use { response ->
//            val responseBody = response.body()?.string()
//            Log.i("response", responseBody?:"systemHang")
//            return responseBody?:""
//        }
//    }

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


//    override fun onDestroy() {

//        val currentPosition = player.currentPosition
//        val duration = player.duration
//        val progress = (currentPosition * 100 / duration).toInt()
//
//        if (progress > 2) {
//            GlobalScope.launch(Dispatchers.IO) {
//                if (type == "movie") {
//                    watchHistoryDao.insert(
//                        ContinueWatching(
//                            progress = progress,
//                            imgLink = imgLink!!,
//                            tmdbID = id.toInt(),
//                            title = title,
//                            type = type!!
//                        )
//                    )
//                } else if (type == "tvshow") {
//                    watchHistoryDao.insert(
//                        ContinueWatching(
//                            progress = progress,
//                            imgLink = imgLink!!,
//                            tmdbID = id.toInt(),
//                            title = title,
//                            season = season,
//                            episode = episode,
//                            type = type!!
//                        )
//                    )
//                }
//            }
//        }
//        Log.i("Progress", progress.toString())
//        player.playWhenReady = false
//        player.stop()
//        player.seekTo(0)
//
//        super.onDestroy()
//    }


}


@Suppress("DEPRECATION")
class FullscreenHolder(ctx: Context) : FrameLayout(ctx) {
    init {
        setBackgroundColor(ctx.resources.getColor(android.R.color.black))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(evt: MotionEvent): Boolean {
        return true
    }


}


