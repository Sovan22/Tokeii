package com.demomiru.tokeiv2.utils

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.webkit.WebView
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
        "Authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjYWI3MzE4OTFiMjhjNWFkNjFjODVjZDk5Mzg1MWVkNyIsInN1YiI6IjY0YTk1MzUyZDFhODkzMDBhZGJlYTc5YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.8Z_74qWdW5q6iPS7G7_j3NXFFUOUUszmWNZYupFH4Fc"

    )

    val tvImdb = requests.get(
        "https://api.themoviedb.org/3/tv/$tmdbID?language=en-US",
        headers = headers
    ).okhttpResponse
    val gson = Gson()
    val response = tvImdb.body.string()
    Log.i("response", response)
    val imdbID = gson.fromJson(response, TvIMDB::class.java)
    return imdbID.number_of_seasons.toInt()
}

suspend fun getSeasonEpisodes(tmdbID: String, season: Int): Int {
    val requests = Requests()
    val headers = mapOf(
        "accept" to " application/json",
        "Authorization" to "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjYWI3MzE4OTFiMjhjNWFkNjFjODVjZDk5Mzg1MWVkNyIsInN1YiI6IjY0YTk1MzUyZDFhODkzMDBhZGJlYTc5YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.8Z_74qWdW5q6iPS7G7_j3NXFFUOUUszmWNZYupFH4Fc"

    )
    val seasonImdb = requests.get(
        "https://api.themoviedb.org/3/tv/$tmdbID/season/$season?language=en-US",
        headers = headers
    ).okhttpResponse

    val gson = Gson()
    val response = seasonImdb.body.string()
    Log.i("response", response)
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