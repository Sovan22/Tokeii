package com.demomiru.tokeiv2

import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.demomiru.tokeiv2.utils.createNumberList
import com.demomiru.tokeiv2.utils.dropDownMenu

import com.demomiru.tokeiv2.utils.retrofitBuilder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.create

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
    private lateinit var episodesRc: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var dropdownMenu: AutoCompleteTextView
    private lateinit var hintTil : TextInputLayout
    private lateinit var episodeImg: ImageView
    private lateinit var episodeOverview : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view =  inflater.inflate(R.layout.fragment_tv_show_details, container, false)
        val id = args.tmdbID

        episodeImg = view.findViewById(R.id.episode_img)
        episodeOverview = view.findViewById(R.id.episode_overview_text)


        hintTil = view.findViewById(R.id.dropdown_menu)
        progressBar = view.findViewById(R.id.progress_circular)
        episodesRc = view.findViewById(R.id.episode_display_rc)
        episodesRc.layoutManager = LinearLayoutManager(requireContext())

        val backdropImg : ImageView = view.findViewById(R.id.show_backdrop)
        val posterImg : ImageView = view.findViewById(R.id.show_poster)
        val overview : TextView = view.findViewById(R.id.overview)
        val retrofit = retrofitBuilder()

        val bottomSheet = view.findViewById<CardView>(R.id.bottom_sheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN


        val tvService = retrofit.create(TMDBService::class.java)
        GlobalScope.launch(Dispatchers.Main) {
            val tvDetailsResponse = tvService.getTVShowDetails(
                id,
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )


            if(tvDetailsResponse.isSuccessful){
                val tvShows = tvDetailsResponse.body()

                posterImg.load("https://image.tmdb.org/t/p/w500${tvShows?.poster_path}")
                backdropImg.load("https://image.tmdb.org/t/p/original${tvShows?.backdrop_path}")
                overview.text = tvShows?.overview

                val episodeNumber = createNumberList(tvShows?.number_of_episodes!!)

                Log.i("Seasons", "${tvShows.number_of_seasons}")

                val seasons = dropDownMenu(tvShows.number_of_seasons.toInt()) // Fetch the data
                val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, seasons) // Create an ArrayAdapter
                dropdownMenu = view.findViewById(R.id.autoCompleteTextView) // Get the AutoCompleteTextView
                dropdownMenu.setAdapter(arrayAdapter) // Set the adapter


                var seasonNumber = "1"

                dropdownMenu.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
//                        hintTil.hint = ""
                    val selectedItem = parent.getItemAtPosition(position) as String
                    seasonNumber = selectedItem.substringAfter(" ")
                    Log.i("Season Number", seasonNumber)

                    GlobalScope.launch(Dispatchers.Main) {
                        val episodeResponse = tvService.getEpisodeDetails(
                            id, seasonNumber,
                            "cab731891b28c5ad61c85cd993851ed7",
                            "en-US"
                        )

                        if (episodeResponse.isSuccessful) {
                            val episodes = episodeResponse.body()?.episodes ?: emptyList()
                            episodesRc.adapter = EpisodeAdapter2(episodes) {


//                                episodeImg.load("https://image.tmdb.org/t/p/w500${it.still_path}")
//                                episodeOverview.text = it.overview
//                                view.findViewById<LinearLayout>(R.id.episode_card_ll).visibility = View.VISIBLE
//                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED


                                val action =
                                    TVShowDetailsDirections.actionTVShowDetailsToMoviePlayActivity(
                                        id,
                                        "show",
                                        it.episode_number.toInt(),
                                        it.season_number
                                    )
                                findNavController().navigate(action)

                            }

                        }
                    }
                }




//                episodesRc.adapter = EpisodeAdapter(episodeNumber){
//                    val action = TVShowDetailsDirections.actionTVShowDetailsToMoviePlayActivity(id,"show",it)
//                    findNavController().navigate(action)
//                }

                withContext(Dispatchers.Main) {

                    view.findViewById<LinearLayout>(R.id.progress_layout).visibility = View.GONE
                    view.findViewById<TextView>(R.id.episodes_text).visibility = View.VISIBLE
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