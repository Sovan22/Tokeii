package com.demomiru.tokeiv2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.utils.retrofitBuilder
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.DelicateCoroutinesApi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class MainActivity : AppCompatActivity() {

//    private lateinit var tvRc: RecyclerView
//    private lateinit var movieRc: RecyclerView
    private lateinit var tvCardRc: RecyclerView
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        findViewById<BottomNavigationView>(R.id.bottom_nav_bar).setupWithNavController(navController)

        //Toolbar
//        val myToolbar = findViewById<Toolbar>(R.id.toolbar)
//        setSupportActionBar(myToolbar)
//        supportActionBar?.title = "Tokei"
//
//        val button : Button = findViewById(R.id.check_button)
//        button.setOnClickListener {
//            val intent = Intent(this, MoviePlayActivity::class.java)
//            startActivity(intent)
//        }

//        tvCardRc = findViewById(R.id.card_container)
//        tvRc = findViewById(R.id.tv_recycler_view)
//        movieRc = findViewById(R.id.movie_recycler_view)

//        tvCardRc.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
//        tvRc.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
//        movieRc.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)


//        val retrofit = retrofitBuilder()
//
//        val tvService = retrofit.create(TMDBService::class.java)
//        val movieService = retrofit.create(MovieService::class.java)

//        GlobalScope.launch(Dispatchers.Main) {
//            val tvPopularResponse = tvService.getPopularTVShows(
//                "cab731891b28c5ad61c85cd993851ed7",
//                "en-US"
//            )

//            val tvTrendingResponse = tvService.getTrendingTVShows(
//                "cab731891b28c5ad61c85cd993851ed7",
//                "en-US"
//            )

//            val movieResponse = movieService.getPopularMovies(
//                "cab731891b28c5ad61c85cd993851ed7",
//                "en-US"
//            )


//            if (tvTrendingResponse.isSuccessful) {
//                val tvShows = tvTrendingResponse.body()?.results ?: emptyList()
//                Log.i("Trending Shows Size", tvShows.size.toString())
//                val trendingTVShows = tvShows.subList(0,18).chunked(3)
//                tvCardRc.adapter = TVShowCardAdapter(trendingTVShows)
//            }

//            if (movieResponse.isSuccessful) {
//                val movies = movieResponse.body()?.results ?: emptyList()
//                movieRc.adapter = MovieAdapter(movies)
//            }
//
//            if (tvPopularResponse.isSuccessful) {
//                val tvShows = tvPopularResponse.body()?.results ?: emptyList()
//                tvRc.adapter = TVShowAdapter(tvShows)
//            }


    }
}