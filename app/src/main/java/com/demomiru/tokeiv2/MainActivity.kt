@file:OptIn(DelicateCoroutinesApi::class)

package com.demomiru.tokeiv2
import android.os.Bundle

import android.view.KeyEvent

import android.view.View
import android.widget.ImageView
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer

import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.demomiru.tokeiv2.utils.addRecyclerAnimation
import com.demomiru.tokeiv2.utils.passData

import com.demomiru.tokeiv2.watching.ContinueWatching
import com.demomiru.tokeiv2.watching.ContinueWatchingAdapter
import com.demomiru.tokeiv2.watching.ContinueWatchingDatabase
import com.demomiru.tokeiv2.watching.ContinueWatchingRepository


import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.DelicateCoroutinesApi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch



@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var watchHistoryRc : RecyclerView
    private val database by lazy { ContinueWatchingDatabase.getInstance(this) }
    private val watchHistoryDao by lazy { database.watchDao() }
    private lateinit var continueText: TextView
    private lateinit var imageback: ImageView
    private lateinit var continueWatchingObserver: Observer<List<ContinueWatching>>
    private var nestedScrollView : NestedScrollView? = null
    private lateinit var adapter: ContinueWatchingAdapter
    private lateinit var continueWatchingRepository: ContinueWatchingRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val options = NavOptions.Builder()
            .setEnterAnim(R.anim.enter_from_bottom)
            .setExitAnim(R.anim.exit_to_top)
            .build()

        nestedScrollView = findViewById(R.id.nestedScrollView)
        val navController = findNavController(R.id.nav_host_fragment)
        val bottomNavigationView : BottomNavigationView = findViewById(R.id.bottom_nav_bar)
        imageback = findViewById(R.id.back_listener)
        continueWatchingRepository = ContinueWatchingRepository(watchHistoryDao)
        watchHistoryRc = findViewById(R.id.watch_history_rc)
        watchHistoryRc.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        continueText = findViewById(R.id.continue_watching_text)
        adapter = ContinueWatchingAdapter{it,delete->
            if(delete){
                GlobalScope.launch  (Dispatchers.IO) {
                    continueWatchingRepository.delete(it)
                    continueWatchingRepository.loadData()
                }
            }
            else {
                startActivity(passData(it, this))
            }

        }


        watchHistoryRc.adapter = adapter

        GlobalScope.launch  (Dispatchers.IO) {
            continueWatchingRepository.loadData()
        }



        continueWatchingObserver = Observer{
            if(it.isNotEmpty()){
                watchHistoryRc.visibility = View.VISIBLE
                continueText.visibility = View.VISIBLE
                adapter.submitList(it)
                addRecyclerAnimation(watchHistoryRc,adapter)
            }
            else{
                watchHistoryRc.visibility = View.GONE
                continueText.visibility = View.GONE
            }
        }

        continueWatchingRepository.allWatchHistory.observe(this,continueWatchingObserver)

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

        imageback.setOnClickListener {
            GlobalScope.launch  (Dispatchers.IO) {
                continueWatchingRepository.loadData()
            }
        }

       bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.moviesFragment -> {
                    navController.navigate(R.id.moviesFragment, null, options)
                    GlobalScope.launch  (Dispatchers.IO) {
                        continueWatchingRepository.loadData()
                    }
                }
                R.id.searchFragment -> {
                    navController.navigate(R.id.searchFragment, null, options)
                    watchHistoryRc.visibility = View.GONE
                    continueText.visibility = View.GONE
                }
                R.id.TVShowFragment -> {
                    navController.navigate(R.id.TVShowFragment, null, options)
                    GlobalScope.launch  (Dispatchers.IO) {
                        continueWatchingRepository.loadData()
                    }
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

//    override fun onPause() {
//        continueWatchingRepository.allWatchHistory.removeObserver(continueWatchingObserver)
//        super.onPause()
//    }

    override fun onStart() {

//        if(nestedScrollView !=null) {
//
//
//            nestedScrollView?.post {
//                nestedScrollView?.scrollTo(0, 0)
//            }
//
//            nestedScrollView?.postDelayed({
                GlobalScope.launch(Dispatchers.IO) {
                    continueWatchingRepository.loadData()
                }
//                nestedScrollView?.scrollTo(0, 0)
//            }, 0)
//        }
//        imageback.performClick()
        super.onStart()
    }

}