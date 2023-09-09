package com.demomiru.tokeiv2.utils

import androidx.navigation.NavDirections
import com.demomiru.tokeiv2.Movie
import com.demomiru.tokeiv2.MoviesFragmentDirections
import com.demomiru.tokeiv2.TVShowFragmentDirections
import com.demomiru.tokeiv2.TVshow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
fun playShow(tvShow : TVshow) : NavDirections{
    return TVShowFragmentDirections.actionTVShowFragmentToTVShowDetails(tvShow.id)
}

fun dropDownMenu(seasonNumbers: Int): List<String>{
    val resultList = mutableListOf<String>()
    for (i in 1..seasonNumbers) {
        resultList.add("Season $i")
    }
    return resultList
}

//fun dynamicRetrofitBuilder (id :String): Retrofit
//{
//    return Retrofit.Builder()
//        .baseUrl("https://api.themoviedb.org/3/tv/$id")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//}
