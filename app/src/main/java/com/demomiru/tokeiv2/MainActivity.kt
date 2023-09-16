package com.demomiru.tokeiv2

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.demomiru.tokeiv2.utils.superStreamRetrofitBuilder


import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

//    private lateinit var tvRc: RecyclerView
//    private lateinit var movieRc: RecyclerView
//    private lateinit var tvCardRc: RecyclerView
//    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val options = NavOptions.Builder()
            .setEnterAnim(R.anim.enter_from_bottom)
            .setExitAnim(R.anim.exit_to_top)
            .build()

        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNavigationView : BottomNavigationView = findViewById(R.id.bottom_nav_bar)



//        val retrofit = superStreamRetrofitBuilder()
//        val apiService = retrofit.create(MovieService::class.java)
//
//        GlobalScope.launch (Dispatchers.Main){
//            val response = apiService.fetchDataFromServer()
//            if (response.isSuccessful) {
//                val serverResponse = response.body()
//                if (serverResponse != null) {
//                    // Handle the server response here
//                    val videoLink = serverResponse.videoLink
//                    // Do something with videoLinks
//                    Log.i("link",videoLink)
//                } else {
//                    Toast.makeText(this@MainActivity, "Response body is empty", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(this@MainActivity, "Request failed", Toast.LENGTH_SHORT).show()
//            }
//        }


       bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.moviesFragment -> {
                    navController.navigate(R.id.moviesFragment, null, options)
                }
                R.id.searchFragment -> {
                    navController.navigate(R.id.searchFragment, null, options)
                }
                R.id.TVShowFragment -> {
                    navController.navigate(R.id.TVShowFragment, null, options)
                }
            }
            true
        }
        bottomNavigationView.setOnNavigationItemReselectedListener {
            return@setOnNavigationItemReselectedListener
        }
//        findViewById<BottomNavigationView>(R.id.bottom_nav_bar).setupWithNavController(navController)

//        window.sharedElementEnterTransition = MaterialContainerTransform()

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
    fun triggerSearchKeyPress() {
        val enterKeyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        dispatchKeyEvent(enterKeyEvent)
    }


}