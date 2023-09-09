package com.demomiru.tokeiv2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.transition.platform.MaterialContainerTransform

import kotlinx.coroutines.DelicateCoroutinesApi


class MainActivity : AppCompatActivity() {

//    private lateinit var tvRc: RecyclerView
//    private lateinit var movieRc: RecyclerView
    private lateinit var tvCardRc: RecyclerView
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val options = NavOptions.Builder()
            .setEnterAnim(R.anim.enter_from_bottom)
            .setExitAnim(R.anim.exit_to_top)
            .build()

        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNavigationView : BottomNavigationView = findViewById(R.id.bottom_nav_bar)
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
        bottomNavigationView.setOnNavigationItemReselectedListener { item ->
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
}