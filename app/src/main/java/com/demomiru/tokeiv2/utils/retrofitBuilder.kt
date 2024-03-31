package com.demomiru.tokeiv2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.view.animation.AnimationUtils
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.TransferListener

import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.anime.AnimeAdapter
import com.demomiru.tokeiv2.Episode
import com.demomiru.tokeiv2.Movie
import com.demomiru.tokeiv2.MovieAdapter
import com.demomiru.tokeiv2.MovieAdapter2
import com.demomiru.tokeiv2.MoviePlayActivity

import com.demomiru.tokeiv2.MoviesFragmentDirections
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.TVShowAdapter
import com.demomiru.tokeiv2.TVShowAdapter2
import com.demomiru.tokeiv2.TVShowFragmentDirections
import com.demomiru.tokeiv2.TVshow
import com.demomiru.tokeiv2.VideoPlayActivity
import com.demomiru.tokeiv2.watching.ContinueWatching
import com.demomiru.tokeiv2.watching.ContinueWatchingAdapter
import com.demomiru.tokeiv2.watching.VideoData
import com.google.gson.Gson
import com.lagradost.nicehttp.addGenericDns
import com.lagradost.nicehttp.ignoreAllSSLErrors
import okhttp3.Cache
import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit




private val appCache = Cache(File("cacheDir", "okhttpcache"), 10 * 1024 * 1024)
private fun OkHttpClient.Builder.addCloudFlareDns() = (
        addGenericDns(
            "https://cloudflare-dns.com/dns-query",
            // https://www.cloudflare.com/ips/
            listOf(
                "1.1.1.1",
                "1.0.0.1",
                "2606:4700:4700::1111",
                "2606:4700:4700::1001"
            )
        ))
private val baseClient = OkHttpClient.Builder()
    .followRedirects(true)
    .followSslRedirects(true)
    .ignoreAllSSLErrors()
    .cache(
        appCache
    ).addCloudFlareDns().build()

fun retrofitBuilder (): Retrofit
{
   return Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create()).client(baseClient)
        .build()
}

//fun getHiID(id:String) : String{
//
//}

fun dateToUnixTime(dateStr: String): Long {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = sdf.parse(dateStr)
        return date?.time ?: 0L
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0L
}

fun createNumberList(n: Int): List<Int> {
    return List(n) { it + 1 }
}

fun yearExtract(releaseDate : String) : String{
    return releaseDate.substringBefore("-")

}
fun play(movie : Movie) : NavDirections{
    return  MoviesFragmentDirections.actionMoviesFragmentToMoviePlayActivity(movie.id, "movie",title = movie.title)

}
fun playShow(tvShow : TVshow, position: Int, name: String) : NavDirections{
    return TVShowFragmentDirections.actionTVShowFragmentToTVShowDetails(tvShow.id,position,name)
}

fun dropDownMenu(seasonNumbers: Int): List<String>{
    val resultList = mutableListOf<String>()
    for (i in 1..seasonNumbers) {
        resultList.add("Season $i")
    }
    return resultList
}

@SuppressLint("NotifyDataSetChanged")
fun addRecyclerAnimation(view :RecyclerView, adapter : ContinueWatchingAdapter){
    view.adapter = adapter
    val context = view.context
    val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
    view.layoutAnimation = controller
    adapter.notifyDataSetChanged()
    view.scheduleLayoutAnimation()
}

fun addRecyclerAnimation(view :RecyclerView, adapter : AnimeAdapter){
    view.adapter = adapter
    val context = view.context
    val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
    view.layoutAnimation = controller
    adapter.notifyDataSetChanged()
    view.scheduleLayoutAnimation()
}

@SuppressLint("NotifyDataSetChanged")
fun addRecyclerAnimation(view :RecyclerView, adapter : TVShowAdapter){
    view.adapter = adapter
    val context = view.context
    val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
    view.layoutAnimation = controller
    adapter.notifyDataSetChanged()
    view.scheduleLayoutAnimation()
}

fun addRecyclerAnimation(view :RecyclerView, adapter : TVShowAdapter2){
    view.adapter = adapter
    val context = view.context
    val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
    view.layoutAnimation = controller
    adapter.notifyDataSetChanged()
    view.scheduleLayoutAnimation()
}

@SuppressLint("NotifyDataSetChanged")
fun addRecyclerAnimation(view :RecyclerView, adapter : MovieAdapter){
    view.adapter = adapter
    val context = view.context
    val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
    view.layoutAnimation = controller
    adapter.notifyDataSetChanged()
    view.scheduleLayoutAnimation()
}

@SuppressLint("NotifyDataSetChanged")
fun addRecyclerAnimation(view :RecyclerView, adapter : MovieAdapter2){
    view.adapter = adapter
    val context = view.context
    val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
    view.layoutAnimation = controller
    adapter.notifyDataSetChanged()
    view.scheduleLayoutAnimation()
}


 fun passData(data : Movie,context : Context) : Intent{
    val bundle = Bundle()
    bundle.putSerializable("Data", data)
    val intent = Intent(context, MoviePlayActivity::class.java)
    intent.putExtras(bundle)
     intent.putExtra("type","movie")
    return intent
}

fun passData(data: GogoAnime.AnimeDetails,context : Context,id: String,ep: Int,title: String): Intent{
    val bundle = Bundle()
    bundle.putParcelable("Data", data)
    val intent = Intent(context, MoviePlayActivity::class.java)
    intent.putExtras(bundle)
    intent.putExtra("type","anime")
    intent.putExtra("id",id)
    intent.putExtra("ep",ep)
    return intent
}


fun passData(data : Episode,context : Context,title : String, imgLink : String,id : String) : Intent{
    val bundle = Bundle()
    bundle.putSerializable("Data", data)
    val intent = Intent(context, MoviePlayActivity::class.java)
    intent.putExtras(bundle)
    intent.putExtra("type","tvshow")
    intent.putExtra("id",id)
    intent.putExtra("showTitle",title)
    intent.putExtra("poster",imgLink)
    return intent
}

fun passData(data : ContinueWatching,context : Context) : Intent{
    val bundle = Bundle()
    bundle.putParcelable("Data", data)
    val intent = Intent(context, MoviePlayActivity::class.java)
    intent.putExtras(bundle)
    intent.putExtra("type","continue")
    return intent
}

fun encodeStringToInt(input : String) : Int{
    return if(input == "") 0
        else input.hashCode()

}


fun passVideoData(data: VideoData, context: Context) : Intent{
    val bundle = Bundle()
    bundle.putParcelable("VidData", data)
    val intent =  Intent(context, VideoPlayActivity::class.java)
    intent.putExtras(bundle)
    return intent
}

fun setSeekBarTime(rem: Long) : String{
    return String.format(
        "%02d:%02d:%02d",
        TimeUnit.MILLISECONDS.toHours(rem),
        TimeUnit.MILLISECONDS.toMinutes(rem) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(rem)),
        TimeUnit.MILLISECONDS.toSeconds(rem) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(rem))
    )
}
fun fixHtml(html : String) : String
{
    return html.replace("\u003C", "<")
}


fun superStreamRetrofitBuilder(): Retrofit{
    return Retrofit.Builder()
        .baseUrl("https://loon-neat-troll.ngrok-free.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun getBaseClient(): OkHttpClient {
    fun OkHttpClient.Builder.addCloudFlareDns() = (
            addGenericDns(
                "https://cloudflare-dns.com/dns-query",
                // https://www.cloudflare.com/ips/
                listOf(
                    "1.1.1.1",
                    "1.0.0.1",
                    "2606:4700:4700::1111",
                    "2606:4700:4700::1001"
                )
            ))


    val appCache = Cache(File("cacheDir", "okhttpcache"), 10 * 1024 * 1024)
//    private val bootstrapClient = OkHttpClient.Builder().cache(appCache).build()
//
//    val dns = DnsOverHttps.Builder().client(bootstrapClient)
//        .url("https://1.1.1.1/dns-query".toHttpUrl())
//        .build()

    val baseClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .ignoreAllSSLErrors()
        .cache(
            appCache
        )
        .apply {
           addCloudFlareDns()
        }.build()
    return baseClient
}
//fun dynamicRetrofitBuilder (id :String): Retrofit
//{
//    return Retrofit.Builder()
//        .baseUrl("https://api.themoviedb.org/3/tv/$id")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//}
