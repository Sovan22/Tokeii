package com.demomiru.tokeiv2

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gargoylesoftware.htmlunit.javascript.host.WindowOrWorkerGlobalScopeMixin.atob
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.extract
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.attribute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils.waitFor
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.Base64


class MainActivity : AppCompatActivity() {

    private lateinit var tvRc: RecyclerView
    private lateinit var movieRc: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvRc = findViewById(R.id.tv_recycler_view)
        movieRc = findViewById(R.id.movie_recycler_view)
        tvRc.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        movieRc.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val tvService = retrofit.create(TMDBService::class.java)
        val movieService = retrofit.create(MovieService::class.java)

        GlobalScope.launch(Dispatchers.Main) {
            val tvResponse = tvService.getPopularTVShows(
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )
            val movieResponse = movieService.getPopularMovies("cab731891b28c5ad61c85cd993851ed7",
                "en-US")
            if (tvResponse.isSuccessful) {
                val tvShows = tvResponse.body()?.results ?: emptyList()
                tvRc.adapter = TVShowAdapter(tvShows)
            }
            if (movieResponse.isSuccessful) {
                val movies = movieResponse.body()?.results ?: emptyList()
                movieRc.adapter = MovieAdapter(movies)
            }
        }
    }
}