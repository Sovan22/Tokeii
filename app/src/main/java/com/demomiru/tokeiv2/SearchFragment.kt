package com.demomiru.tokeiv2


import android.os.Bundle
import android.view.KeyEvent

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.EditText

import android.widget.RadioButton
import android.widget.RadioGroup

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.utils.retrofitBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null

    private var param2: String? = null
    private lateinit var searchResultsRc : RecyclerView
    private lateinit var searchEt: EditText
    private lateinit var movieChoice : RadioButton
    private lateinit var tvChoice : RadioButton
    private lateinit var choice : RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_search, container, false)

        searchResultsRc = view.findViewById(R.id.search_results_rc)

        movieChoice = view.findViewById(R.id.movies_search)
        tvChoice = view.findViewById(R.id.tvShows_search)
        choice = view.findViewById(R.id.choice)


        searchResultsRc.layoutManager = GridLayoutManager(requireContext(),2)

        searchEt = view.findViewById(R.id.search_et)
        searchEt.setOnKeyListener { _, actionId, event ->
            if (actionId == KeyEvent.ACTION_DOWN || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // The "Search" button on the keyboard was clicked
                if(movieChoice.isChecked)
                {
                    performMovieSearch()
                }
                else{
                    performShowSearch()
                }

                choice.setOnCheckedChangeListener { _, _: Int ->
                    searchResultsRc.visibility = View.GONE
                }
                return@setOnKeyListener true
            }
            false
        }

        return view
    }

    private fun performMovieSearch()
    {
        searchResultsRc.visibility = View.GONE
        val query = searchEt.text.toString()
//        Log.i("Search query", query)
        val retrofit = retrofitBuilder()

        val movieService = retrofit.create(MovieService::class.java)

        GlobalScope.launch (Dispatchers.Main){
            val searchResults = movieService.searchMovie(
                query,
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )

            if (searchResults.isSuccessful)
            {
                val movies = searchResults.body()?.results ?: emptyList()
                searchResultsRc.adapter = MovieAdapter(movies){
                    val action = SearchFragmentDirections.actionSearchFragmentToMoviePlayActivity(it.id,"movie")
                    findNavController().navigate(action)
                }
            }

            withContext(Dispatchers.Main) {
                searchResultsRc.visibility = View.VISIBLE
            }
        }

    }

    private fun performShowSearch()
    {
        searchResultsRc.visibility = View.GONE
        val query = searchEt.text.toString()
//        Log.i("Search query", query)
        val retrofit = retrofitBuilder()

        val tvService = retrofit.create(TMDBService::class.java)

        GlobalScope.launch (Dispatchers.Main){
            val searchResults = tvService.searchShow(
                query,
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )

            if (searchResults.isSuccessful)
            {
                val tvShows = searchResults.body()?.results ?: emptyList()
                searchResultsRc.adapter = TVShowAdapter(tvShows){
                    val action = SearchFragmentDirections.actionSearchFragmentToTVShowDetails(it.id)
                    findNavController().navigate(action)
                }
            }
            withContext(Dispatchers.Main) {
                searchResultsRc.visibility = View.VISIBLE
            }
        }

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}