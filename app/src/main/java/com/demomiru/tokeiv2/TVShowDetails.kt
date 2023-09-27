package com.demomiru.tokeiv2

import android.annotation.SuppressLint

import android.os.Bundle

import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter

import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer

import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModel
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModelFactory
import com.demomiru.tokeiv2.utils.dropDownMenu
import com.demomiru.tokeiv2.utils.passData
import com.demomiru.tokeiv2.utils.retrofitBuilder
import com.demomiru.tokeiv2.watching.ContinueWatching
import com.demomiru.tokeiv2.watching.ContinueWatchingDatabase

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class TVShowDetails : Fragment() {
    private val args : TVShowDetailsArgs by navArgs()
    private lateinit var id: String

    private lateinit var viewModelFactory: ContinueWatchingViewModelFactory
    private val viewModel: ContinueWatchingViewModel by viewModels(
        factoryProducer = {
            viewModelFactory
        }
    )
    private lateinit var episodeProgress : ContinueWatching
    private val database by lazy { ContinueWatchingDatabase.getInstance(requireContext()) }
    private val watchHistoryDao by lazy { database.watchDao() }
    private lateinit var episodesRc: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var dropDownSpinner: Spinner


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewStateObserver = Observer<ContinueWatching?> {watchFrom ->
            val continueButton =  view.findViewById<Button>(R.id.continue_button)
            if (watchFrom != null) {
                episodeProgress = watchFrom
                continueButton.visibility = View.VISIBLE
               continueButton.text =
                    "Continue Watching \t S${watchFrom.season} E${watchFrom.episode}"
            }

            continueButton.setOnClickListener {
                startActivity(passData(watchFrom!!, requireContext()))
            }
        }
        viewModel.watchFrom.observe(viewLifecycleOwner,viewStateObserver)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        postponeEnterTransition()
        // Inflate the layout for this fragment
       val view =  inflater.inflate(R.layout.fragment_tv_show_details, container, false)

        id = args.tmdbID
        viewModelFactory = ContinueWatchingViewModelFactory(watchHistoryDao,id.toInt())
        val title = args.title

        val expandView = view.findViewById<ConstraintLayout>(R.id.expand_tvshow_view)
        val titleTv = view.findViewById<TextView>(R.id.title_show)

        val position = args.position
//        val continueButton  = view.findViewById<Button>(R.id.continue_button)
        expandView.transitionName = "image_$position"


//        hintTil = view.findViewById(R.id.dropdown_menu)
        progressBar = view.findViewById(R.id.progress_circular)
        episodesRc = view.findViewById(R.id.episode_display_rc)
        episodesRc.layoutManager = LinearLayoutManager(requireContext())

        val backdropImg : ImageView = view.findViewById(R.id.show_backdrop)
        val posterImg : ImageView = view.findViewById(R.id.show_poster)
        val overview : TextView = view.findViewById(R.id.overview)
        val retrofit = retrofitBuilder()


//    lifecycleScope.launch (Dispatchers.IO) {
//        val watchFrom = watchHistoryDao.getProgress(id.toInt())
//        withContext(Dispatchers.Main) {
//            if (watchFrom != null) {
//                continueButton.visibility = View.VISIBLE
//                continueButton.text =
//                    "Continue Watching \t S${watchFrom.season} E${watchFrom.episode}"
//            }
//
//            continueButton.setOnClickListener {
//                startActivity(passData(watchFrom!!, requireContext()))
//            }
//        }
//    }


        val tvService = retrofit.create(TMDBService::class.java)
        GlobalScope.launch(Dispatchers.Main) {
            val tvDetailsResponse = tvService.getTVShowDetails(
                id,
                BuildConfig.TMDB_API_KEY,
                "en-US"
            )


            if(tvDetailsResponse.isSuccessful){
                val tvShows = tvDetailsResponse.body()

                posterImg.load("https://image.tmdb.org/t/p/w500${tvShows?.poster_path}")
                backdropImg.load("https://image.tmdb.org/t/p/original${tvShows?.backdrop_path}")
                overview.text = tvShows?.overview
                titleTv.text = title


                val seasons = dropDownMenu(tvShows!!.number_of_seasons.toInt()) // Fetch the data
                val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, seasons) // Create an ArrayAdapter
//                dropdownMenu = view.findViewById(R.id.autoCompleteTextView)
//                dropdownMenu.setAdapter(arrayAdapter) // Set the adapter
                dropDownSpinner = view.findViewById(R.id.dropdown_spinner)
                dropDownSpinner.adapter = arrayAdapter


                var seasonNumber: String
                dropDownSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                        val selectedItem = parent?.getItemAtPosition(position) as String
                        seasonNumber = selectedItem.substringAfter(" ")
                        Log.i("Season Number", seasonNumber)

                        GlobalScope.launch(Dispatchers.Main) {
                            val episodeResponse = tvService.getEpisodeDetails(
                                id, seasonNumber,
                                BuildConfig.TMDB_API_KEY,
                                "en-US"
                            )

                            if (episodeResponse.isSuccessful) {
                                val episodes = episodeResponse.body()?.episodes ?: emptyList()
                                val adapter = EpisodeAdapter2(episodes) {
                                    startActivity(passData(it,requireContext(),title,tvShows.poster_path,id))

                                }
                                episodesRc.adapter = adapter
                                val context = episodesRc.context
                                val controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation)
                                episodesRc.layoutAnimation = controller
                                adapter.notifyDataSetChanged()
                                episodesRc.scheduleLayoutAnimation()
                                view.findViewById<TextView>(R.id.episodes_text).visibility = View.VISIBLE

                            }
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }

                withContext(Dispatchers.Main) {

                    view.findViewById<LinearLayout>(R.id.progress_layout).visibility = View.GONE
//                    view.findViewById<TextView>(R.id.episodes_text).visibility = View.VISIBLE
                }

            }
        }



        return view
    }
}