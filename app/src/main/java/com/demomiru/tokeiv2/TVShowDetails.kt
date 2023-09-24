package com.demomiru.tokeiv2

import android.annotation.SuppressLint

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonDisposableHandle.parent
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TVShowDetails.newInstance] factory method to
 * create an instance of this fragment.
 */
class TVShowDetails : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val args : TVShowDetailsArgs by navArgs()
    private lateinit var id: String

    private lateinit var viewModelFactory: ContinueWatchingViewModelFactory
    private val viewModel: ContinueWatchingViewModel by viewModels(
        factoryProducer = {
            viewModelFactory
        }
    )

    private val database by lazy { ContinueWatchingDatabase.getInstance(requireContext()) }
    private val watchHistoryDao by lazy { database.watchDao() }
    private lateinit var episodesRc: RecyclerView
    private lateinit var progressBar: ProgressBar
//    private lateinit var dropdownMenu: AutoCompleteTextView
//    private lateinit var hintTil : TextInputLayout
    private lateinit var dropDownSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

//        sharedElementEnterTransition = MaterialContainerTransform().apply {
//            drawingViewId = R.id.nav_host_fragment
//            scrimColor = Color.TRANSPARENT
//            duration = 750
//        }

    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewStateObserver = Observer<ContinueWatching?> {watchFrom ->
            val continueButton =  view.findViewById<Button>(R.id.continue_button)
            if (watchFrom != null) {
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
        val continueButton  = view.findViewById<Button>(R.id.continue_button)
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TVShowDetails.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TVShowDetails().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}