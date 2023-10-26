@file:OptIn(DelicateCoroutinesApi::class)

package com.demomiru.tokeiv2


import android.os.Bundle
import android.view.KeyEvent

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.EditText
import android.widget.ProgressBar

import android.widget.RadioButton
import android.widget.RadioGroup

import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.anime.AnimeAdapter
import com.demomiru.tokeiv2.history.QueryRepository

import com.demomiru.tokeiv2.history.SearchDatabase
import com.demomiru.tokeiv2.history.SearchHistory
import com.demomiru.tokeiv2.history.SearchHistoryAdapter2
import com.demomiru.tokeiv2.utils.GogoAnime

import com.demomiru.tokeiv2.utils.addRecyclerAnimation
import com.demomiru.tokeiv2.utils.encodeStringToInt
import com.demomiru.tokeiv2.utils.passData

import com.demomiru.tokeiv2.utils.retrofitBuilder

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {
//    private lateinit var searchHistoryList: List<String>
    private lateinit var searchHistoryRC: RecyclerView

    private lateinit var deleteAll : TextView
    private var isClicked = true
    private lateinit var adapter: SearchHistoryAdapter2
    private lateinit var searchResultsRc : RecyclerView
    private lateinit var searchEt: EditText
    private lateinit var movieChoice : RadioButton
    private lateinit var tvChoice : RadioButton
    private lateinit var animeChoice: RadioButton
    private lateinit var choice : RadioGroup
    private lateinit var searchLoading : ProgressBar
    private val database by lazy { SearchDatabase.getInstance(requireContext()) }
    private val searchHistoryDao by lazy { database.searchDao() }
    private lateinit var queryRepository:QueryRepository



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_search, container, false)
        queryRepository = QueryRepository(searchHistoryDao)
        deleteAll = view.findViewById(R.id.delete_all_button)
        searchHistoryRC = view.findViewById(R.id.search_history_rc)
        searchResultsRc = view.findViewById(R.id.search_results_rc)
        searchLoading = view.findViewById(R.id.search_loading)
        movieChoice = view.findViewById(R.id.movies_search)
        tvChoice = view.findViewById(R.id.tvShows_search)
        animeChoice = view.findViewById(R.id.anime_search)
        choice = view.findViewById(R.id.choice)

        searchResultsRc.layoutManager = GridLayoutManager(requireContext(),2)
        searchHistoryRC.layoutManager = LinearLayoutManager(requireContext())

        adapter = SearchHistoryAdapter2{it,search->
            if (search){
                searchEt.setText(it.query)
                (activity as MainActivity).triggerSearchKeyPress()
            }
            else {
                GlobalScope.launch(Dispatchers.IO) {
                    queryRepository.deleteRecord(it)
                    queryRepository.loadData()
                }
            }
        }
        searchHistoryRC.adapter = adapter
        searchHistoryRC.visibility = View.VISIBLE
        GlobalScope.launch (Dispatchers.IO){
            queryRepository.loadData()
        }

        val searchHistoryObserver = Observer<List<SearchHistory>>{
            if (it.isNotEmpty()) {

                adapter.submitList(it)

            } else {
                searchHistoryRC.visibility = View.GONE
                deleteAll.visibility = View.GONE
                isClicked = true
            }

        }

        queryRepository.allQueries.observe(viewLifecycleOwner,searchHistoryObserver)


        deleteAll.setOnClickListener{
            GlobalScope.launch (Dispatchers.IO){
                queryRepository.deleteAll()
                queryRepository.loadData()
            }
        }

        searchEt = view.findViewById(R.id.search_et)
        searchEt.setOnClickListener{
            if(isClicked) {
                deleteAll.visibility = View.VISIBLE
                searchHistoryRC.visibility = View.VISIBLE
                isClicked = false
            }
            else{
                deleteAll.visibility = View.GONE
                searchHistoryRC.visibility = View.GONE
                isClicked = true
            }

        }
        searchEt.setOnKeyListener { _, actionId, event ->
            if (actionId == KeyEvent.ACTION_DOWN || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // The "Search" button on the keyboard was clicked
                if(movieChoice.isChecked)
                {
                    performMovieSearch()
                }
                else if(tvChoice.isChecked){
                    performShowSearch()
                }
                else{
                    performAnimeSearch()
                }

                choice.setOnCheckedChangeListener { _, _: Int ->
                    searchResultsRc.visibility = View.GONE
                    searchHistoryRC.visibility = View.VISIBLE
                }
                return@setOnKeyListener true
            }
            false
        }

        return view
    }

    private fun performAnimeSearch()
    {
        searchResultsRc.visibility = View.GONE
        searchHistoryRC.visibility = View.GONE
        isClicked = true
        deleteAll.visibility = View.GONE
        searchLoading.visibility = View.VISIBLE
        val gogoSrc = GogoAnime()

        val query = searchEt.text.toString()

        val adapter = AnimeAdapter(requireContext()){
            lifecycleScope.launch {

//                val action = SearchFragmentDirections.actionSearchFragmentToTVShowDetails(
//                    encodeStringToInt(it.name).toString(), title = "",animeUrl = it.url)
//                findNavController().navigate(action)

               val action =  SearchFragmentDirections.actionSearchFragmentToAnimeDetailsFragment(it.name,it.url)
                findNavController().navigate(action)

            }
        }
        addRecyclerAnimation(searchResultsRc, adapter)
        lifecycleScope.launch(Dispatchers.IO) {
            val history = SearchHistory(query = query)
            queryRepository.insert(history)
            queryRepository.loadData()
            val animeList = gogoSrc.search(query)

            withContext(Dispatchers.Main)
            {
                if (animeList.isEmpty()) Toast.makeText(requireContext(),"No Matches",Toast.LENGTH_LONG).show()
                adapter.submitList(animeList)
                searchLoading.visibility = View.GONE
                searchResultsRc.visibility = View.VISIBLE
            }
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun performMovieSearch()
    {
        searchResultsRc.visibility = View.GONE
        searchHistoryRC.visibility = View.GONE
        isClicked = true
        deleteAll.visibility = View.GONE
        searchLoading.visibility = View.VISIBLE

        val query = searchEt.text.toString()

        GlobalScope.launch(Dispatchers.IO) {

            val history = SearchHistory(query = query)
            queryRepository.insert(history)
            queryRepository.loadData()
        }

        val retrofit = retrofitBuilder()

        val movieService = retrofit.create(MovieService::class.java)

        GlobalScope.launch (Dispatchers.Main){

            val searchResults = movieService.searchMovie(
                query,
                BuildConfig.TMDB_API_KEY,
                "en-US"
            )

            if (searchResults.isSuccessful)
            {
                val movies = searchResults.body()?.results ?: emptyList()
                if (movies.isEmpty()) Toast.makeText(requireContext(),"No Matches",Toast.LENGTH_LONG).show()
                val adapter = MovieAdapter(movies){
//                    val action = SearchFragmentDirections.actionSearchFragmentToMoviePlayActivity(it.id,"movie",title = it.title)
//                    findNavController().navigate(action)
                    startActivity(passData(it,requireContext()))
                }
                addRecyclerAnimation(searchResultsRc,adapter)
            }

            withContext(Dispatchers.Main) {
                searchLoading.visibility = View.GONE
                searchResultsRc.visibility = View.VISIBLE
            }
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun performShowSearch()
    {
        searchResultsRc.visibility = View.GONE
        searchHistoryRC.visibility = View.GONE
        isClicked = true
        searchLoading.visibility = View.VISIBLE
        deleteAll.visibility = View.GONE

        val query = searchEt.text.toString()
        GlobalScope.launch(Dispatchers.IO) {
//            searchHistoryDao.deleteAll()
            val history = SearchHistory(query = query)
            queryRepository.insert(history)
            queryRepository.loadData()
        }

        val retrofit = retrofitBuilder()

        val tvService = retrofit.create(TMDBService::class.java)

        GlobalScope.launch (Dispatchers.Main){

            val searchResults = tvService.searchShow(
                query,
                BuildConfig.TMDB_API_KEY,
                "en-US"
            )

            if (searchResults.isSuccessful)
            {
                val tvShows = searchResults.body()?.results ?: emptyList()
                if (tvShows.isEmpty()) Toast.makeText(requireContext(),"No Matches",Toast.LENGTH_LONG).show()
                val adapter = TVShowAdapter(tvShows){it, _ ->
                    val action = SearchFragmentDirections.actionSearchFragmentToTVShowDetails(it.id, title = it.name)
                    findNavController().navigate(action)
                }
                addRecyclerAnimation(searchResultsRc,adapter)
            }
            withContext(Dispatchers.Main) {
                searchLoading.visibility = View.GONE
                searchResultsRc.visibility = View.VISIBLE
            }
        }

    }

}