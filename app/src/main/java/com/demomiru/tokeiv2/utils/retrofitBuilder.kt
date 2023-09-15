package com.demomiru.tokeiv2.utils

import android.view.animation.AnimationUtils
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.Movie
import com.demomiru.tokeiv2.MovieAdapter
import com.demomiru.tokeiv2.MoviesFragmentDirections
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.TVShowAdapter
import com.demomiru.tokeiv2.TVShowFragmentDirections
import com.demomiru.tokeiv2.TVshow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.FieldPosition

fun retrofitBuilder (): Retrofit
{
   return Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun createNumberList(n: Int): List<Int> {
    return List(n) { it + 1 }
}

fun yearExtract(releaseDate : String) : String{
    return releaseDate.substringBefore("-")

}
fun play(movie : Movie) : NavDirections{
    return  MoviesFragmentDirections.actionMoviesFragmentToMoviePlayActivity(movie.id, "movie")

}
fun playShow(tvShow : TVshow, position: Int) : NavDirections{
    return TVShowFragmentDirections.actionTVShowFragmentToTVShowDetails(tvShow.id,position)
}

fun dropDownMenu(seasonNumbers: Int): List<String>{
    val resultList = mutableListOf<String>()
    for (i in 1..seasonNumbers) {
        resultList.add("Season $i")
    }
    return resultList
}

fun addRecyclerAnimation(view :RecyclerView,adapter : TVShowAdapter){
    view.adapter = adapter
    val context = view.context
    val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
    view.layoutAnimation = controller
    adapter.notifyDataSetChanged()
    view.scheduleLayoutAnimation()
}

fun addRecyclerAnimation(view :RecyclerView,adapter : MovieAdapter){
    view.adapter = adapter
    val context = view.context
    val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
    view.layoutAnimation = controller
    adapter.notifyDataSetChanged()
    view.scheduleLayoutAnimation()
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
