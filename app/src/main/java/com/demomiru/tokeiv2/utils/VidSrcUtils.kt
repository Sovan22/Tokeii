package com.demomiru.tokeiv2.utils

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.webkit.WebView
import com.demomiru.tokeiv2.BuildConfig
import com.demomiru.tokeiv2.ImdbEpisode
import com.demomiru.tokeiv2.TvIMDB
import com.google.gson.Gson
import com.lagradost.nicehttp.Requests

fun clickMiddle(webView : WebView,time: Long)
{
    val middleX = webView.width / 2
    val middleY = webView.height / 2
    val downTime = SystemClock.uptimeMillis()
    val eventTime = SystemClock.uptimeMillis() + 100
    val metaState = 0
    val downEvent = MotionEvent.obtain(
        downTime,
        eventTime,
        MotionEvent.ACTION_DOWN,
        middleX.toFloat(),
        middleY.toFloat(),
        metaState
    )

    val upEvent = MotionEvent.obtain(
        downTime + 2000,
        eventTime + 1000,
        MotionEvent.ACTION_UP,
        middleX.toFloat(),
        middleY.toFloat(),
        metaState
    )


    Handler(Looper.getMainLooper()).postDelayed({
        webView.dispatchTouchEvent(downEvent)
        webView.dispatchTouchEvent(upEvent)
    }, time)
}

suspend fun getTvSeasons(tmdbID: String): Int {
    val requests = Requests()
    val headers = mapOf(
        "accept" to " application/json",
        "Authorization" to "Bearer ${BuildConfig.TMDB_TOKEN}"

    )

    val tvImdb = requests.get(
        "https://api.themoviedb.org/3/tv/$tmdbID?language=en-US",
        headers = headers
    ).okhttpResponse
    val gson = Gson()
    val response = tvImdb.body.string()
//    Log.i("response", response)
    val imdbID = gson.fromJson(response, TvIMDB::class.java)
    return imdbID.number_of_seasons.toInt()
}

suspend fun getSeasonEpisodes(tmdbID: String, season: Int): Int {
    val requests = Requests()
    val headers = mapOf(
        "accept" to " application/json",
        "Authorization" to "Bearer ${BuildConfig.TMDB_TOKEN}"

    )
    val seasonImdb = requests.get(
        "https://api.themoviedb.org/3/tv/$tmdbID/season/$season?language=en-US",
        headers = headers
    ).okhttpResponse

    val gson = Gson()
    val response = seasonImdb.body.string()
//    Log.i("response", response)
    val episodesList = gson.fromJson(response, ImdbEpisode::class.java)
    return episodesList.episodes.size
}

//Might be used

//                webView.loadUrl("https://vidsrc.me/embed/tv?tmdb=$id&season=$season&episode=$episode")
//                webView.evaluateJavascript("javascript:document.documentElement.outerHTML") {
//                    if (it.contains("404 Not Found")) {
//                        Log.i("Not found", " Yes")
//                        finish()
//                    }
//                }
//                clickMiddle(webView, 5000)
//                clickMiddle(webView,6000)
//                isNextEpisode.value = false
//                Handler(Looper.getMainLooper()).postDelayed({
//                    webView.loadUrl("about:blank")
//                }, 10000)


//Previous TVShow Provider
//                        webView.loadUrl(url)
//                        webView.evaluateJavascript("javascript:document.documentElement.outerHTML"){
//                            if(it.contains("404 Not Found")){
//                                Log.i("Not found"," Yes" )
//                                finish()
//                            }
//                        }
//                        clickMiddle(webView,5000)
//                        clickMiddle(webView,6000)
//
////                        clickMiddle(webView,1000)
//                        clickedMiddle = true
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            if (clickedMiddle && videoUrl.value.isNullOrBlank()) {
//                                Toast.makeText(this@MoviePlayActivity, "Not Released Yet",Toast.LENGTH_SHORT).show()
//                                finish()
//                            }
//                        }, 20000)

//Movie Play Activity
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