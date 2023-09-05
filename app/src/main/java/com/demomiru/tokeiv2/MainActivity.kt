package com.demomiru.tokeiv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DelicateCoroutinesApi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class MainActivity : AppCompatActivity() {

    private lateinit var tvRc: RecyclerView
    private lateinit var tvCardRc: RecyclerView
    private lateinit var movieRc: RecyclerView
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Toolbar
        val myToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(myToolbar)
        supportActionBar?.title = "Tokei"


        tvCardRc = findViewById(R.id.card_container)
        tvRc = findViewById(R.id.tv_recycler_view)
        movieRc = findViewById(R.id.movie_recycler_view)

        tvCardRc.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        tvRc.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        movieRc.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val tvService = retrofit.create(TMDBService::class.java)
        val movieService = retrofit.create(MovieService::class.java)

        GlobalScope.launch(Dispatchers.Main) {
            val tvPopularResponse = tvService.getPopularTVShows(
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )

            val tvTrendingResponse = tvService.getTrendingTVShows(
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )

            val movieResponse = movieService.getPopularMovies(
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )


            if (tvTrendingResponse.isSuccessful) {
                val tvShows = tvTrendingResponse.body()?.results ?: emptyList()
                Log.i("Trending Shows Size", tvShows.size.toString())
                val trendingTVShows = tvShows.subList(0,18).chunked(3)
                tvCardRc.adapter = TVShowCardAdapter(trendingTVShows)
            }

            if (movieResponse.isSuccessful) {
                val movies = movieResponse.body()?.results ?: emptyList()
                movieRc.adapter = MovieAdapter(movies)
            }

            if (tvPopularResponse.isSuccessful) {
                val tvShows = tvPopularResponse.body()?.results ?: emptyList()
                tvRc.adapter = TVShowAdapter(tvShows)
            }

        }
    }
}