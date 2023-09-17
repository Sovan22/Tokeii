package com.demomiru.tokeiv2.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.view.animation.AnimationUtils

import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.Episode
import com.demomiru.tokeiv2.Movie
import com.demomiru.tokeiv2.MovieAdapter
import com.demomiru.tokeiv2.MoviePlayActivity

import com.demomiru.tokeiv2.MoviesFragmentDirections
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.TVShowAdapter
import com.demomiru.tokeiv2.TVShowFragmentDirections
import com.demomiru.tokeiv2.TVshow
import com.demomiru.tokeiv2.watching.ContinueWatching
import com.demomiru.tokeiv2.watching.ContinueWatchingAdapter

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


fun retrofitBuilder (): Retrofit
{
   return Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

//fun getHiID(id:String) : String{
//
//}

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
@SuppressLint("NotifyDataSetChanged")
fun addRecyclerAnimation(view :RecyclerView, adapter : TVShowAdapter){
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

 fun passData(data : Movie,context : Context) : Intent{
    val bundle = Bundle()
    bundle.putSerializable("Data", data)
    val intent = Intent(context, MoviePlayActivity::class.java)
    intent.putExtras(bundle)
     intent.putExtra("type","movie")
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
    bundle.putSerializable("Data", data)
    val intent = Intent(context, MoviePlayActivity::class.java)
    intent.putExtras(bundle)
    intent.putExtra("type","continue")
    return intent
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
//fun dynamicRetrofitBuilder (id :String): Retrofit
//{
//    return Retrofit.Builder()
//        .baseUrl("https://api.themoviedb.org/3/tv/$id")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//}
