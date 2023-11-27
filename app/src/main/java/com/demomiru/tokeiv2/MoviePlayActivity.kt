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
import com.demomiru.tokeiv2.utils.GoMovies
import com.demomiru.tokeiv2.utils.GogoAnime
import com.demomiru.tokeiv2.utils.HdMovie2
import com.demomiru.tokeiv2.utils.SmashyStream
import com.demomiru.tokeiv2.utils.SuperstreamUtils

import com.demomiru.tokeiv2.utils.getMovieImdb
import com.demomiru.tokeiv2.utils.getMovieLink
import com.demomiru.tokeiv2.utils.getTvImdb
import com.demomiru.tokeiv2.utils.getTvLink


import com.demomiru.tokeiv2.utils.passVideoData


import com.demomiru.tokeiv2.watching.ContinueWatching

import com.demomiru.tokeiv2.watching.VideoData
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Suppress("DEPRECATION")
class MoviePlayActivity : AppCompatActivity(){
    private lateinit var webView : WebView
    private  var fullscreenContainer: FullscreenHolder? = null
    private var animeEp : List<GogoAnime.Episode> = listOf()
    private var isSuper = false
    private var superId: Int? = null
    private var IMDBid: String? = null
    private val superStream = SuperstreamUtils()
    private lateinit var loading:ProgressBar
    private lateinit var id:String
//    private var clickedMiddle = false
    private var animeUrl = ""
    private var season: Int = 1
    private var episode: Int = 1
    private var origin : String = ""
    private var year : String = ""
    private var seekProgress : Int = 0
    private var imgLink : String? = null
    private lateinit var title:String
    private var type : String? = null
    private val videoUrl = MutableLiveData<String?>()
    private var subUrl : MutableList<String> = mutableListOf()

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
            year = data.release_date.substringBefore("-")
            if(origin == "kn" || origin == "ml" || origin == "ta" || origin == "te")
                origin = "hi"
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
        "anime" ->{
            val data =  bundle?.getParcelable("Data") as? GogoAnime.AnimeDetails
            id = bundle?.getString("id")!!
            title = data?.title!!
            imgLink = data.poster
            season = 1
            episode = bundle.getInt("ep")
            animeEp = data.episodes!!
            animeUrl = data.episodes[episode].url
        }
        else -> {
            val data = bundle?.getParcelable("Data") as? ContinueWatching
            id = data!!.tmdbID.toString()
            title = data.title
            imgLink = data.imgLink
            seekProgress = data.progress
            type = data.type
            origin = data.origin ?: ""
            if (type != "movie"){
                season = data.season
                episode = data.episode
                if(type == "anime"){
                    animeEp = data.animeEp!!
                    animeUrl = data.animeEp[episode].url
                }
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
                   subUrl,
                   animeEp,
                   origin
                   ),this)
                intent.putExtra("origin", origin)
                intent.putExtra("superstream",isSuper)
                intent.putExtra("animeUrl",animeUrl)
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
                        val link = getTvLink(imdbId,season-1,episode-1)
                        if(link.isNotBlank())
                        videoUrl.value = link
                        else{
                            withContext(Dispatchers.Main){
                                Toast.makeText(this@MoviePlayActivity,"No Links Available", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
                    else{
                        //Superstream add
                        val mainData = superStream.search(title)
                        superId = mainData.data.list[0].id
                        if (superId != null) {
                            isSuper = true
                            val tvLinks = superStream.loadLinks(false, superId!!, season, episode)
                            tvLinks.data?.list?.forEach {
                                if(!it.path.isNullOrBlank()){
                                    println("${it.quality} : ${it.path}")
                                    if(it.quality == "720p") {
                                        val subtitle = superStream.loadSubtile(false,it.fid!!,superId!!,season,episode).data
//
                                        getSub(subtitle)
                                        videoUrl.value = it.path
                                        return@forEach
                                    }
                                }
                            }
                            if(videoUrl.value.isNullOrBlank()){
//                                withContext(Dispatchers.Main){
//                                    Toast.makeText(this@MoviePlayActivity, "Not Available",Toast.LENGTH_SHORT).show()
//                                    finish()
//                                }
                                isSuper = false
//                                getGoMovieLink(false)
                                getSmashLink(false)
                            }
                        }
                        else{
                            isSuper = false
//                            getGoMovieLink(false)
                            getSmashLink(false)
                        }

                    }
                }
            }else if(type == "movie"){
                if (origin != "hi") {
                    lifecycleScope.launch {
                        val mainData = superStream.search(title)
//                        println(mainData.data.list[0].year)
                        val item = mainData.data.list[0]
                        println(year + " ${item.year}")
                        superId = if(item.title == title && item.year.toString() == year) item.id else null
                        getMovieEn()
//                        webView.loadUrl(url)
                    }

                }
                else {
                    lifecycleScope.launch {
                        val imdbId = getMovieImdb(id)
                        IMDBid = imdbId
                        println(imdbId)
//                        videoUrl.value = getMovieLink(imdbId)
                        val link = getMovieLink(imdbId)
                        if (link.isBlank()) {

                            //add smash link
                            getSmashLink(true)
//                            val mainData = superStream.search(title)
//                            superId = mainData.data.list[0].id
//                            getMovie()

                        }
                        else
                            videoUrl.value = link
                    }
                }
            }
            else{
                lifecycleScope.launch {
                    val gogoSrc = GogoAnime()
                    videoUrl.value = gogoSrc.extractVideos(animeUrl)
                }
            }
        }
    }

    private suspend fun getGoMovieLink(isMovie: Boolean){
        val goMovie = GoMovies()
        val data = goMovie.search(season,episode,title)
        val vidLink = data.first
        val subLinks = data.second
        if(vidLink.isNullOrBlank()){
            Toast.makeText(this@MoviePlayActivity, "Not Available", Toast.LENGTH_SHORT).show()
            finish()
        }
        else{
            if (!subLinks.isNullOrEmpty())subUrl.addAll(subLinks)
            videoUrl.value = vidLink
        }
    }

    private fun getSub(subtitle: SuperstreamUtils.PrivateSubtitleData?){
        subtitle?.list?.forEach { subList->
            if(subList.language == "English"){
                subList.subtitles.forEach { sub->

                        if (subUrl.size == 3) {
                        return
                        }
                        if (sub.lang == "en" && !sub.file_path.isNullOrBlank()) {
                            subUrl.add(sub.file_path)
                            println("${sub.language} : ${sub.file_path}")
                        }


                }
                return
            }
        }
    }

    private suspend fun getMovie()
    {
        if (superId != null) {
            isSuper = true
            val movieLinks = superStream.loadLinks(true, superId!!)
            movieLinks.data?.list?.forEach {
                if(!it.path.isNullOrBlank()){
                    println("${it.quality} : ${it.path}")
                    if(it.quality == "720p") {
                        val subtitle = superStream.loadSubtile(true,it.fid!!,superId!!).data
//
                        getSub(subtitle)
                        videoUrl.value = it.path
                        return
                    }
                }
            }
            if(videoUrl.value.isNullOrBlank()){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MoviePlayActivity, "Not Available",Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        else{
            withContext(Dispatchers.Main){
                Toast.makeText(this@MoviePlayActivity, "Not Available",Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private suspend fun getMovieEn()
    {
        if (superId != null) {
            isSuper = true
            val movieLinks = superStream.loadLinks(true, superId!!)
            movieLinks.data?.list?.forEach {
                if(!it.path.isNullOrBlank()){
                    println("${it.quality} : ${it.path}")
                    if(it.quality == "720p") {
                        val subtitle = superStream.loadSubtile(true,it.fid!!,superId!!).data
//
                        getSub(subtitle)
                        videoUrl.value = it.path
                        return@forEach
                    }
                }
            }
            if(videoUrl.value.isNullOrBlank()){
                withContext(Dispatchers.Main){
//                    webView.loadUrl(url)
                    getSmashLink(true)
                    isSuper = false
                }
            }
        }
        else{
            withContext(Dispatchers.Main){
//                webView.loadUrl(url)
                getSmashLink(true)
                isSuper = false
            }
        }
    }

    private fun getSmashLink(isMovie:Boolean)
    {
        val smashSrc = SmashyStream()
        lifecycleScope.launch {
            val links = smashSrc.getLink(isMovie,id, season, episode)
            val vidLink = links.first
            val subLink = links.second
            if(vidLink.isNullOrBlank()){

                if (isMovie && origin == "hi"){
                    val mainData = superStream.search(title)
                    superId = mainData.data.list[0].id
                    getMovie()
                }
                else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MoviePlayActivity, "Not Available", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                }
            }
            else{
                if (!subLink.isNullOrBlank())subUrl.add(subLink)
                videoUrl.value = vidLink
            }
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


