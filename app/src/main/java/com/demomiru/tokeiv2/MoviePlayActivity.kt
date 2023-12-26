package com.demomiru.tokeiv2

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.extractors.PrMovies
import com.demomiru.tokeiv2.extractors.ResultsAdapter
import com.demomiru.tokeiv2.extractors.SearchResponse
import com.demomiru.tokeiv2.utils.Extractor
import com.demomiru.tokeiv2.utils.GoMovies
import com.demomiru.tokeiv2.utils.GogoAnime
import com.demomiru.tokeiv2.utils.SmashyStream
import com.demomiru.tokeiv2.utils.SuperstreamUtils

import com.demomiru.tokeiv2.utils.getMovieImdb
import com.demomiru.tokeiv2.utils.getMovieLink
import com.demomiru.tokeiv2.utils.getTvImdb
import com.demomiru.tokeiv2.utils.getTvLink


import com.demomiru.tokeiv2.utils.passVideoData


import com.demomiru.tokeiv2.watching.ContinueWatching

import com.demomiru.tokeiv2.watching.VideoData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


import kotlin.Exception


@Suppress("DEPRECATION")
class MoviePlayActivity : AppCompatActivity(){
    private val gson = Gson()
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
    private lateinit var resultRc : RecyclerView
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
    private var source : String? = null

    private val COVER_SCREEN_PARAMS = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    private val prMovies = PrMovies()
    private lateinit var url : String

//    private val args : MoviePlayActivityArgs by navArgs()
    @SuppressLint("SetJavaScriptEnabled")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_movie_play)
        Log.i("Start", "Time")
        resultRc = findViewById(R.id.results_rc)
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
            year = data.year ?: ""
            origin = data.origin ?: ""
            println(data)
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

        loading = findViewById(R.id.loading_content)


        webView = findViewById(R.id.web_view)
        loading.visibility = View.VISIBLE


        videoUrl.observe(this) { hlsUri ->
            if (!hlsUri.isNullOrEmpty()) {
                webView.visibility = View.GONE
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
                   origin,
                   year
                   ),this)
                intent.putExtra("origin", origin)
                intent.putExtra("superstream",isSuper)
                intent.putExtra("animeUrl",animeUrl)
//                println(source)
                intent.putExtra("source",source)
                startActivity(intent)
                finish()
            }
        }

//            if(type == "tvshow"){
//                lifecycleScope.launch {
//                    val imdbId = getTvImdb(id)
//                    if(imdbId.isNotBlank()){
//                        origin = "hi"
//                        IMDBid = imdbId
//                        val link = getTvLink(imdbId,season-1,episode-1)
//                        if(link.isNotBlank())
//                        videoUrl.value = link
//                        else{
//                            withContext(Dispatchers.Main){
//                                Toast.makeText(this@MoviePlayActivity,"No Links Available", Toast.LENGTH_SHORT).show()
//                                finish()
//                            }
//                        }
//                    }
//                    else{
//                        //Superstream add
//                        val mainData = superStream.search(title)
//                        superId = mainData.data.list[0].id
//                        if (superId != null) {
//                            isSuper = true
//                            val tvLinks = superStream.loadLinks(false, superId!!, season, episode)
//                            val urlMaps: MutableMap<String,String> = mutableMapOf()
//                            tvLinks.data?.list?.forEach {
//                                if(!it.path.isNullOrBlank()){
//                                    println("${it.quality} : ${it.path}")
//                                    urlMaps[it.quality!!] = it.path
//                                    if(it.quality == "720p") {
//                                        val subtitle = superStream.loadSubtile(false,it.fid!!,superId!!,season,episode).data
////
//                                        getSub(subtitle)
//
//                                        return@forEach
//                                    }
//                                }
//                            }
//                            if(urlMaps.isNotEmpty())
//                                videoUrl.value = gson.toJson(urlMaps)
//                            if(videoUrl.value.isNullOrBlank()){
////                                withContext(Dispatchers.Main){
////                                    Toast.makeText(this@MoviePlayActivity, "Not Available",Toast.LENGTH_SHORT).show()
////                                    finish()
////                                }
//                                isSuper = false
//                                getGoMovieLink(false)
////                                getSmashLink(false)
//                            }
//                        }
//                        else{
//                            isSuper = false
//                            getGoMovieLink(false)
////                            getSmashLink(false)
//                        }
//
//                    }
//                }
//            }
            if(type == "tvshow"){
                lifecycleScope.launch (Dispatchers.IO){
                    val imdbId = getTvImdb(id)
                    if (imdbId.isNotBlank()) {
                        origin = "hi"
                        IMDBid = imdbId
                    }
                    val links = Extractor(origin).loadExtractor(title,id,year,season,episode,false)
                    println(links)
                    val list = prMovies.getPrMovieLink(title)
                    withContext(Dispatchers.Main) {
                        if (!links.videoUrl.isNullOrBlank()) {
                            println("if run")
                            subUrl.addAll(links.subs)
                            isSuper = links.isSuper
                            source = links.source
                            videoUrl.value = links.videoUrl

                        } else {
                            println("else run")
                            loading.visibility = View.GONE
                            try {
                                val rcAdapter = ResultsAdapter {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val src = prMovies.loadLinks(it.link!!)
                                        if (src.file.isNullOrBlank()) throw Exception("no video url found")
                                        withContext(Dispatchers.Main) {
                                            isSuper = false
                                            source = "prmovies"
                                            videoUrl.value = src.file
                                        }
                                    }
                                }
                                val width = Resources.getSystem().displayMetrics.widthPixels
                                val dpi = Resources.getSystem().displayMetrics.densityDpi
                                val grid = (width*160)/(165*dpi)
                                resultRc.apply {
                                    layoutManager = GridLayoutManager(this@MoviePlayActivity,grid)
                                    adapter = rcAdapter
                                }
                                if (list.isEmpty()) throw Exception("Empty search")

                                rcAdapter.submitList(list)
                            }catch (e:Exception){
                                Toast.makeText(
                                    this@MoviePlayActivity,
                                    "No available links",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    }
                }
            }
            else if(type == "movie"){
                println("Reached here")
//                if (origin != "hi") {
                    lifecycleScope.launch(Dispatchers.IO) {
//                        try {
//                            val mainData = superStream.search(title)
////                        println(mainData.data.list[0].year)
//                            val item = mainData.data.list[0]
//                            println(year + " ${item.year}")
//                            superId =
//                                if (item.title == title && item.year.toString() == year) item.id else null
//                            getMovieEn()
//                        }catch (e : Exception){
//                            getMovieEn()
//                        }
                        val links = Extractor(origin).loadExtractor(title,id,year, season,episode,true)
                        println(links)
                        val query = title.filter {
                            it.isLetterOrDigit() || it.isWhitespace()
                        }
                        val list = prMovies.getPrMovieLink(query)

                        withContext(Dispatchers.Main) {
                            if (!links.videoUrl.isNullOrBlank()) {
                                subUrl.addAll(links.subs)
                                isSuper = links.isSuper
                                source = links.source
                                videoUrl.value = links.videoUrl

                            } else {
                                println("else run")
                                println(list)
                                loading.visibility = View.GONE
                                try {
                                    if (list.isEmpty()) throw Exception("Empty search")
                                    val rcAdapter = ResultsAdapter {
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            val src = prMovies.loadLinks(it.link!!)
                                            if (src.file.isNullOrBlank()) throw Exception("no video url found")
                                            withContext(Dispatchers.Main) {
                                                isSuper = false
                                                source = "prmovies"
                                                videoUrl.value = src.file
                                            }
                                        }
                                    }
                                    val width = Resources.getSystem().displayMetrics.widthPixels
                                    val dpi = Resources.getSystem().displayMetrics.densityDpi
                                    val grid = (width*160)/(165*dpi)
                                    resultRc.apply {
                                        layoutManager = GridLayoutManager(this@MoviePlayActivity,grid)
                                        adapter = rcAdapter
                                    }


                                    rcAdapter.submitList(list)
                                }catch (e:Exception){
                                    Toast.makeText(
                                        this@MoviePlayActivity,
                                        "No available links",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
//                                Toast.makeText(
//                                    this@MoviePlayActivity,
//                                    "No available links",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                finish()

                            }
                        }
//                        webView.loadUrl(url)
                    }

//                }
//                else {
//                    lifecycleScope.launch {
//                        val imdbId = getMovieImdb(id)
//                        IMDBid = imdbId
//                        println(imdbId)
//                        lifecycleScope.launch {
//                            try {
//                                val mainData = superStream.search(title)
////                        println(mainData.data.list[0].year)
//                                val item = mainData.data.list[0]
//                                println(year + " ${item.year}")
//                                superId =
//                                    if (item.title == title && item.year.toString() == year) item.id else null
//                                getMovieHi(imdbId)
//                            } catch (e: Exception) {
//                                getMovieHi(imdbId)
//                            }
//                        }
// //                        videoUrl.value = getMovieLink(imdbId)
//
//
//                    }
//                }
            }
            else{
                lifecycleScope.launch (Dispatchers.IO){
                    val gogoSrc = GogoAnime()
                    val link = gogoSrc.extractVideos(animeUrl)
                    withContext(Dispatchers.Main){
                        videoUrl.value = link
                    }
                }
            }
    }

    private fun getPrMovie(list: List<SearchResponse>){

    }

    private suspend fun getGoMovieLink(isMovie: Boolean){
        val goMovie = GoMovies()
        val data = goMovie.search(season,episode,title,isMovie,year)
        val vidLink = data.first
        val subLinks = data.second
        if(vidLink.isNullOrBlank()){
//            Toast.makeText(this@MoviePlayActivity, "Not Available", Toast.LENGTH_SHORT).show()
//            finish()
            getSmashLink(isMovie)
        }
        else{
            if (!subLinks.isNullOrEmpty())subUrl.add(subLinks)
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
//                            println("${sub.language} : ${sub.file_path}")
                        }


                }
                return
            }
        }
    }

    private fun getSub2(subtitle: SuperstreamUtils.PrivateSubtitleData?){
        subtitle?.list?.forEach { subList->
                subList.subtitles.forEach { sub->
                    if (!sub.file_path.isNullOrBlank()) {
                        subUrl.add("${subList.language} :${sub.file_path}")
                    }
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

    private suspend fun getMovieHi(imdbId: String)
    {
        if (superId != null) {
            isSuper = true
            val movieLinks = superStream.loadLinks(true, superId!!)
            val urlMaps: MutableMap<String,String> = mutableMapOf()
            movieLinks.data?.list?.forEach {
                if(!it.path.isNullOrBlank()){
                    println("${it.quality} : ${it.path}")
                    urlMaps[it.quality!!] = it.path
                    if(it.quality == "720p") {
                        val subtitle = superStream.loadSubtile(true,it.fid!!,superId!!).data
//
                        getSub(subtitle)

                        return@forEach
                    }
                }

            }
            if(urlMaps.isNotEmpty())
                videoUrl.value = gson.toJson(urlMaps)
            if(videoUrl.value.isNullOrBlank()){
                withContext(Dispatchers.Main){
//                    webView.loadUrl(url)
//                    getSmashLink(true)
                    isSuper = false
                    val link = getMovieLink(imdbId)
                    if (link.isBlank()) {
                        getSmashLink(true,"hi")
                    }
                    else
                        videoUrl.value = link

                }
            }
        }
        else{
            withContext(Dispatchers.Main){
//                webView.loadUrl(url)
//                getSmashLink(true)
                isSuper = false
                val link = getMovieLink(imdbId)
                if (link.isBlank())
                    getSmashLink(true,"hi")
                else
                    videoUrl.value = link

            }
        }
    }

    private suspend fun getMovieEn()
    {
        if (superId != null) {
            isSuper = true
            val movieLinks = superStream.loadLinks(true, superId!!)
            val urlMaps: MutableMap<String,String> = mutableMapOf()
            movieLinks.data?.list?.forEach {
                if(!it.path.isNullOrBlank()){
                    println("${it.quality} : ${it.path}")
                    urlMaps[it.quality!!] = it.path
                    if(it.quality == "720p") {
                        val subtitle = superStream.loadSubtile(true,it.fid!!,superId!!).data
//
                        getSub(subtitle)

                        return@forEach
                    }
                }

            }
            if(urlMaps.isNotEmpty())
                videoUrl.value = gson.toJson(urlMaps)
            if(videoUrl.value.isNullOrBlank()){
                withContext(Dispatchers.Main){
//                    webView.loadUrl(url)
//                    getSmashLink(true)
                    isSuper = false
                    getGoMovieLink(true)

                }
            }
        }
        else{
            withContext(Dispatchers.Main){
//                webView.loadUrl(url)
//                getSmashLink(true)
                isSuper = false
                getGoMovieLink(true)

            }
        }
    }

    private fun getSmashLink(isMovie:Boolean,src: String = "en")
    {
        val smashSrc = SmashyStream()
        lifecycleScope.launch {
            val links = smashSrc.getLink(isMovie,id, season, episode,src)
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


