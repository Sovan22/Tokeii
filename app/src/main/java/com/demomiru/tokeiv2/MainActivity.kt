package com.demomiru.tokeiv2

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModel2
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModelFactory2
import com.demomiru.tokeiv2.utils.addRecyclerAnimation
import com.demomiru.tokeiv2.utils.passData
import com.demomiru.tokeiv2.watching.ContinueWatching
import com.demomiru.tokeiv2.watching.ContinueWatchingAdapter
import com.demomiru.tokeiv2.watching.ContinueWatchingDatabase
import com.demomiru.tokeiv2.watching.ContinueWatchingRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.primitives.UnsignedBytes.toInt
import com.google.gson.Gson
import com.lagradost.nicehttp.Requests
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.jsoup.Jsoup


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private val version = 113
    private val gson = Gson()
    private lateinit var watchHistoryRc : RecyclerView
    private val database by lazy { ContinueWatchingDatabase.getInstance(this) }
    private val watchHistoryDao by lazy { database.watchDao() }
    private val app = Requests()
    private lateinit var viewModelFactory: ContinueWatchingViewModelFactory2
    private val viewModel: ContinueWatchingViewModel2 by viewModels(
        factoryProducer = {
            viewModelFactory
        }
    )
    private var currentFragment = MutableLiveData(R.id.moviesFragment)
    private lateinit var continueText: TextView
    private lateinit var bottomNavigationView : BottomNavigationView

    private var nestedScrollView : NestedScrollView? = null
    private lateinit var adapter: ContinueWatchingAdapter
    private lateinit var continueWatchingRepository: ContinueWatchingRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModelFactory = ContinueWatchingViewModelFactory2(watchHistoryDao)
        val options = NavOptions.Builder()
            .setEnterAnim(R.anim.enter_from_bottom)
            .setExitAnim(R.anim.exit_to_top)
            .build()

        nestedScrollView = findViewById(R.id.nestedScrollView)
        val navController = findNavController(R.id.nav_host_fragment)
        bottomNavigationView = findViewById(R.id.bottom_nav_bar)
        continueWatchingRepository = ContinueWatchingRepository(watchHistoryDao)
        watchHistoryRc = findViewById(R.id.watch_history_rc)
        watchHistoryRc.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        continueText = findViewById(R.id.continue_watching_text)






        adapter = ContinueWatchingAdapter{it,delete->
            if(delete){
                lifecycleScope.launch  (Dispatchers.IO) {
                    continueWatchingRepository.delete(it)
//                    continueWatchingRepository.loadData()
                }
            }
            else {
                startActivity(passData(it, this))
            }

        }


        watchHistoryRc.adapter = adapter
       bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.moviesFragment -> {
                    navController.navigate(R.id.moviesFragment, null, options)
                    if (viewModel.allWatchHistory.value?.size !=0){
                        watchHistoryRc.visibility = View.VISIBLE
                        continueText.visibility = View.VISIBLE
                        currentFragment.value = R.id.moviesFragment
                    }

                }
                R.id.searchFragment -> {
                    navController.navigate(R.id.searchFragment, null, options)
                    watchHistoryRc.visibility = View.GONE
                    continueText.visibility = View.GONE
                    currentFragment.value = R.id.searchFragment
                }
                R.id.TVShowFragment -> {
                    navController.navigate(R.id.TVShowFragment, null, options)
                    if (viewModel.allWatchHistory.value?.size !=0){
                        watchHistoryRc.visibility = View.VISIBLE
                        continueText.visibility = View.VISIBLE
                        currentFragment.value = R.id.TVShowFragment
                    }

                }
                R.id.animeFragment ->{
                    navController.navigate(R.id.animeFragment,null,options)
                    if (viewModel.allWatchHistory.value?.size !=0){
                        watchHistoryRc.visibility = View.VISIBLE
                        continueText.visibility = View.VISIBLE
                        currentFragment.value = R.id.animeFragment
                    }
                }
            }
            true
        }
        bottomNavigationView.setOnNavigationItemReselectedListener {
            return@setOnNavigationItemReselectedListener
        }
//        bottomNavigationView.selectedItemId = R.id.animeFragment

        lifecycleScope.launch (Dispatchers.IO){
//            val update = app.get("https://github.com/Sovan22/Tokeii/").document.select("article.markdown-body.entry-content.container-lg .anchor")[2].attr("href").substringAfter("v").toInt()
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.github.com/repos/Sovan22/Tokeii/releases").build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val releases = JSONArray(response.body.string())
                val update = gson.fromJson(releases.getJSONObject(0).toString(),Release::class.java).tag_name.replace(".","").replace("v","").replace("-tokei","").toInt()
//                [2].attr("href").substringAfter("v").toInt()
                println(update)
                if (version < update)
                    withContext(Dispatchers.Main) {
                        showDialog()
                    }
            }
        }



    }

    data class Release(val tag_name:String)
    fun triggerSearchKeyPress() {
        val enterKeyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        dispatchKeyEvent(enterKeyEvent)

    }


    @SuppressLint("SetTextI18n")
    override fun onResume() {
//        viewModel.currentFragment.observe(this){
//            if(it == R.id.searchFragment ){
//                watchHistoryRc.visibility = View.GONE
//                continueText.visibility = View.GONE
//            }
//            else{
//                watchHistoryRc.visibility = View.VISIBLE
//                continueText.visibility = View.VISIBLE
//            }
//        }

//        viewModel.showContinue.observe(this){
//            if(!it){
//                watchHistoryRc.visibility = View.GONE
//                continueText.visibility = View.GONE
//            }
//            else{
//                watchHistoryRc.visibility = View.VISIBLE
//                continueText.visibility = View.VISIBLE
//            }
//        }
            val viewStateObserver = Observer<List<ContinueWatching>> {watchFrom ->
            if(watchFrom.isNotEmpty()){
                watchHistoryRc.visibility = View.VISIBLE
                continueText.visibility = View.VISIBLE

                adapter.submitList(watchFrom)
                addRecyclerAnimation(watchHistoryRc,adapter)
                viewModel.currentFragment.observe(this){

                    if(it == R.id.searchFragment || it == R.id.TVShowDetails || it == R.id.animeDetailsFragment ){
                        watchHistoryRc.visibility = View.GONE
                        continueText.visibility = View.GONE
                    }
                    else{
                        watchHistoryRc.visibility = View.VISIBLE
                        continueText.visibility = View.VISIBLE
                    }
                }
//                if(currentFragment.value  == R.id.searchFragment){
//                    watchHistoryRc.visibility = View.GONE
//                    continueText.visibility = View.GONE
//                }else
//                {
//                    watchHistoryRc.visibility = View.VISIBLE
//                    continueText.visibility = View.VISIBLE
//                }
            }
            else{

                watchHistoryRc.visibility = View.GONE
                continueText.visibility = View.GONE
            }

        }
        viewModel.allWatchHistory.observe(this,viewStateObserver)
        viewModel.currentFragment.observe(this){
            bottomNavigationView.selectedItemId = it
        }
        super.onResume()
    }

    private fun showDialog(){
        val builder = AlertDialog.Builder(this)


        builder.setMessage("There is an update available to this app")
            .setTitle("Update Found")


        builder.setPositiveButton("Download"){ dialog, _ ->
            // User clicked OK button
            val url = "https://github.com/Sovan22/Tokeii/releases/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel"){ _, _ ->
            // User cancelled the dialog
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onBackPressed() {
        if(viewModel.currentFragment.value == R.id.searchFragment && viewModel.searchOpen.value == true) {

            viewModel.searchOpen.value = false

        }
        else
            super.onBackPressed()
    }

}