@file:OptIn(DelicateCoroutinesApi::class)

package com.demomiru.tokeiv2


import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.anime.AnimeAdapter
import com.demomiru.tokeiv2.history.QueryRepository
import com.demomiru.tokeiv2.history.SearchDatabase
import com.demomiru.tokeiv2.history.SearchHistory
import com.demomiru.tokeiv2.history.SearchHistoryAdapter2
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModel2
import com.demomiru.tokeiv2.utils.SearchVMFactory
import com.demomiru.tokeiv2.utils.SearchViewModel
import com.demomiru.tokeiv2.utils.addRecyclerAnimation
import com.demomiru.tokeiv2.utils.passData
import kotlinx.coroutines.DelicateCoroutinesApi

class SearchFragment : Fragment() {
    //    private lateinit var searchHistoryList: List<String>
    private lateinit var searchHistoryRC: RecyclerView
    private lateinit var searchView : SearchView
    private val activityViewModel: ContinueWatchingViewModel2 by activityViewModels()
    private lateinit var searchHistoryFl : LinearLayout
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
    private lateinit var viewModelFactory: SearchVMFactory
    private lateinit var mAdapter : MovieAdapter
    private lateinit var tvAdapter : TVShowAdapter
    private lateinit var aAdapter: AnimeAdapter
    private val viewModel: SearchViewModel by viewModels(
        factoryProducer = {
            viewModelFactory
        }
    )



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_search, container, false)
        searchHistoryFl = view.findViewById(R.id.search_history_ll)
        searchView = view.findViewById(R.id.searchView)
        queryRepository = QueryRepository(searchHistoryDao)
        viewModelFactory = SearchVMFactory(queryRepository)
        deleteAll = view.findViewById(R.id.delete_all_button)
        searchHistoryRC = view.findViewById(R.id.search_history_rc)
        searchResultsRc = view.findViewById(R.id.search_results_rc)
        searchLoading = view.findViewById(R.id.search_loading)
        movieChoice = view.findViewById(R.id.movies_search)
        tvChoice = view.findViewById(R.id.tvShows_search)
        animeChoice = view.findViewById(R.id.anime_search)
        choice = view.findViewById(R.id.choice)
        activityViewModel.currentFragment.value = R.id.searchFragment
//        println(activityViewModel.test)
        mAdapter = MovieAdapter{
//                    val action = SearchFragmentDirections.actionSearchFragmentToMoviePlayActivity(it.id,"movie",title = it.title)
//                    findNavController().navigate(action)
            startActivity(passData(it, requireContext()))
        }

        tvAdapter = TVShowAdapter{it, _ ->
            val action = SearchFragmentDirections.actionSearchFragmentToTVShowDetails(it.id, title = it.name)
            findNavController().navigate(action)
        }

        aAdapter = AnimeAdapter(requireContext()){

//                val action = SearchFragmentDirections.actionSearchFragmentToTVShowDetails(
//                    encodeStringToInt(it.name).toString(), title = "",animeUrl = it.url)
//                findNavController().navigate(action)
            val action =  SearchFragmentDirections.actionSearchFragmentToAnimeDetailsFragment(it.name,it.url)
            findNavController().navigate(action)

        }

//        var start = true
//        viewModel.queryText.observe(viewLifecycleOwner){
//            if(it.isNotBlank() && start)searchView.setQuery(it,false)
//        }

//        searchView.setQuery(viewModel.queryText.value,false)



        val width = Resources.getSystem().displayMetrics.widthPixels
        val dpi = Resources.getSystem().displayMetrics.densityDpi
        val grid = (width*160)/(165*dpi)
        println(grid)
        searchResultsRc.layoutManager = GridLayoutManager(requireContext(),grid)
        searchHistoryRC.layoutManager = LinearLayoutManager(requireContext())
//        start   = false
        adapter = SearchHistoryAdapter2{it,search->
            if (search){

                searchView.setQuery(it.query,true)
                viewModel.queryText.value = it.query
                viewModel.searchClicked.value = true
//                (activity as MainActivity).triggerSearchKeyPress()
            }
            else {
                viewModel.deleteRecord(it)
            }
        }
        searchHistoryRC.adapter = adapter
        searchHistoryRC.visibility = View.VISIBLE
//        GlobalScope.launch (Dispatchers.IO){
//            queryRepository.loadData()
//        }

        val searchHistoryObserver = Observer<List<SearchHistory>>{
            adapter.submitList(it)
            if (it.isEmpty()){
//                searchHistoryRC.visibility = View.GONE
//                deleteAll.visibility = View.GONE
                searchHistoryFl.visibility = View.GONE
                activityViewModel.searchOpen.value = false
                isClicked = true
            }

        }
        viewModel.searchClicked.observe(viewLifecycleOwner){
            if(it){
//                deleteAll.visibility = View.GONE
//                searchHistoryRC.visibility = View.GONE
                searchHistoryFl.visibility = View.GONE
                activityViewModel.searchOpen.value = false
            }
            else{
//                deleteAll.visibility = View.VISIBLE
//                searchHistoryRC.visibility = View.VISIBLE
                searchHistoryFl.visibility = View.VISIBLE
                activityViewModel.searchOpen.value = true
            }
        }

//        queryRepository.allQueries.observe(viewLifecycleOwner,searchHistoryObserver)
        viewModel.queries.observe(viewLifecycleOwner,searchHistoryObserver)


        deleteAll.setOnClickListener{
            viewModel.deleteAll()
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
//        searchView.queryHint = "Search"

//        searchView.setOnCloseListener {
//            deleteAll.visibility = View.GONE
//            searchHistoryRC.visibility = View.GONE
//            false
//        }
        searchView.setOnClickListener {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(it.findFocus(), 0)
        }

//        viewModel.queryText.observe(viewLifecycleOwner){
//            if(it.isNotBlank())searchView.setQuery(it,false)
//        }

        searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if(hasFocus) {
//                deleteAll.visibility = View.VISIBLE
//                searchHistoryRC.visibility = View.VISIBLE
                view.postDelayed({
                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(view.findFocus(), 0)
                }, 200)
                searchHistoryFl.visibility = View.VISIBLE
                activityViewModel.searchOpen.value = true

            }
//            else{
//
//                deleteAll.visibility = View.GONE
//                viewModel.searchClicked.value = true
//                searchHistoryRC.visibility = View.GONE
//
//            }
        }

        activityViewModel.searchOpen.observe(viewLifecycleOwner){
            if(it == false){
                searchHistoryFl.visibility = View.GONE
                searchView.clearFocus()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Logic for when search button is clicked
                if(query.isNullOrEmpty())return false
                viewModel.searchClicked.value = true
                if(movieChoice.isChecked)
                {
                    viewModel.choice.value = 1
                    performMovieSearch(query)
                }
                else if(tvChoice.isChecked){
                    viewModel.choice.value = 2
                    performShowSearch(query)
                }
                else{
                    viewModel.choice.value = 3
                    performAnimeSearch(query)
                }
//                searchView.isIconified = true
//                searchView.queryHint = query
                viewModel.queryText.value = query
                searchHistoryFl.visibility = View.GONE

                searchView.clearFocus()

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Logic for when text in search view changes
                //add filter in searchHistory Rc
//                deleteAll.visibility = View.VISIBLE
//                searchHistoryRC.visibility = View.VISIBLE
                searchHistoryFl.visibility = View.VISIBLE
                viewModel.queryText.value = newText?:""
                return false
            }
        })

//        searchView.setOnClickListener {
//            searchView.isIconified = false
//        }

//        searchEt.setOnKeyListener { _, actionId, event ->
//            if(searchEt.text.toString().isEmpty()) return@setOnKeyListener true
//            if (actionId == KeyEvent.ACTION_DOWN || event.keyCode == KeyEvent.KEYCODE_ENTER) {
//                // The "Search" button on the keyboard was clicked
//                viewModel.searchClicked.value = true
//                if(movieChoice.isChecked)
//                {
//                    viewModel.choice.value = 1
//                    performMovieSearch()
//                }
//                else if(tvChoice.isChecked){
//                    viewModel.choice.value = 2
//                    performShowSearch()
//                }
//                else{
//                    viewModel.choice.value = 3
//                    performAnimeSearch()
//                }
//                return@setOnKeyListener true
//            }
//            false
//        }

        choice.setOnCheckedChangeListener { _, _: Int ->
            if(movieChoice.isChecked)
            {
                viewModel.choice.value = 1

            }
            else if(tvChoice.isChecked){
                viewModel.choice.value = 2

            }
            else{
                viewModel.choice.value = 3
            }
            searchResultsRc.visibility = View.GONE
//            searchHistoryRC.visibility = View.VISIBLE
            searchHistoryFl.visibility = View.VISIBLE
            viewModel.searchClicked.value = false
        }

        when(viewModel.choice.value){
            1 -> viewModel.movieList.observe(viewLifecycleOwner) { movies ->
                if (movies.isNotEmpty()) {
                    mAdapter.submitList(movies)
                    addRecyclerAnimation(searchResultsRc,mAdapter)
                    searchLoading.visibility = View.GONE
                    searchResultsRc.visibility = View.VISIBLE
                }
            }
            2 -> viewModel.tvList.observe(viewLifecycleOwner){shows->
                if(shows.isNotEmpty()){
                    tvAdapter.submitList(shows)
                    addRecyclerAnimation(searchResultsRc,tvAdapter)
                    searchLoading.visibility = View.GONE
                    searchResultsRc.visibility = View.VISIBLE
                }
            }
            3 -> viewModel.animeList.observe(viewLifecycleOwner){ anime->
                if(anime.isNotEmpty()){
                    aAdapter.submitList(anime)
                    addRecyclerAnimation(searchResultsRc,aAdapter)
                    searchLoading.visibility = View.GONE
                    searchResultsRc.visibility = View.VISIBLE
                }
            }
            else -> println(viewModel.choice.value)
        }

        viewModel.noMatches.observe(viewLifecycleOwner){
            if(it) {
                Toast.makeText(requireContext(), "No Matches", Toast.LENGTH_SHORT).show()
                viewModel.noMatches.value = false
            }
        }

        return view
    }


    private fun performAnimeSearch(query: String)
    {
        searchResultsRc.visibility = View.GONE
        searchHistoryFl.visibility = View.GONE
        isClicked = true
//        deleteAll.visibility = View.GONE
        searchLoading.visibility = View.VISIBLE
//        val gogoSrc = GogoAnime()

//        val query = searchEt.text.toString()
        val history = SearchHistory(query = query)
        viewModel.addToHistory(history)
        viewModel.searchAnime(query)
//        val adapter = AnimeAdapter(requireContext()){
//            lifecycleScope.launch {
//
////                val action = SearchFragmentDirections.actionSearchFragmentToTVShowDetails(
////                    encodeStringToInt(it.name).toString(), title = "",animeUrl = it.url)
////                findNavController().navigate(action)
//
//               val action =  SearchFragmentDirections.actionSearchFragmentToAnimeDetailsFragment(it.name,it.url)
//                findNavController().navigate(action)
//
//            }
//        }





        viewModel.animeList.observe(viewLifecycleOwner){anime->
            if(anime.isNotEmpty()){
                aAdapter.submitList(anime)
                addRecyclerAnimation(searchResultsRc, aAdapter)
                searchLoading.visibility = View.GONE
                searchResultsRc.visibility = View.VISIBLE
                searchResultsRc.requestFocus()
            }
            else{
//                Toast.makeText(requireContext(),"No Matches",Toast.LENGTH_SHORT).show()
                searchLoading.visibility = View.GONE
                searchResultsRc.visibility = View.GONE
            }
        }
//        addRecyclerAnimation(searchResultsRc, aAdapter)
//        lifecycleScope.launch(Dispatchers.IO) {
//            val history = SearchHistory(query = query)
//            queryRepository.insert(history)
//            queryRepository.loadData()
//            val animeList = gogoSrc.search(query)
//
//            withContext(Dispatchers.Main)
//            {
//                if (animeList.isEmpty()) Toast.makeText(requireContext(),"No Matches",Toast.LENGTH_LONG).show()
//                adapter.submitList(animeList)
//                searchLoading.visibility = View.GONE
//                searchResultsRc.visibility = View.VISIBLE
//            }
//        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun performMovieSearch(query:String)
    {
        searchResultsRc.visibility = View.GONE
//        searchHistoryRC.visibility = View.GONE
        isClicked = true
//        deleteAll.visibility = View.GONE
        searchHistoryFl.visibility = View.GONE
        searchLoading.visibility = View.VISIBLE

//        val query = searchEt.text.toString()

//        GlobalScope.launch(Dispatchers.IO) {
//
//            val history = SearchHistory(query = query)
//            queryRepository.insert(history)
//            queryRepository.loadData()
//        }
        val history = SearchHistory(query = query)
        viewModel.addToHistory(history)

//        val retrofit = retrofitBuilder()
//
//        val movieService = retrofit.create(MovieService::class.java)

        viewModel.searchMovie(query)
        viewModel.movieList.observe(viewLifecycleOwner){movies->
            if(movies.isNotEmpty()){
                mAdapter.submitList(movies)
                addRecyclerAnimation(searchResultsRc,mAdapter)
                searchLoading.visibility = View.GONE
                searchResultsRc.visibility = View.VISIBLE
                searchResultsRc.requestFocus()
            }
            else{
//                Toast.makeText(requireContext(),"No Matches",Toast.LENGTH_SHORT).show()
                searchLoading.visibility = View.GONE
                searchResultsRc.visibility = View.GONE
            }
        }

//        GlobalScope.launch (Dispatchers.Main){
//
//            val searchResults = movieService.searchMovie(
//                query,
//                BuildConfig.TMDB_API_KEY,
//                "en-US"
//            )
//
//            if (searchResults.isSuccessful)
//            {
//                val movies = searchResults.body()?.results ?: emptyList()
//                if (movies.isEmpty()) Toast.makeText(requireContext(),"No Matches",Toast.LENGTH_LONG).show()
//                val adapter = MovieAdapter(movies){
////                    val action = SearchFragmentDirections.actionSearchFragmentToMoviePlayActivity(it.id,"movie",title = it.title)
////                    findNavController().navigate(action)
//                    startActivity(passData(it,requireContext()))
//                }
//                addRecyclerAnimation(searchResultsRc,adapter)
//            }
//
//            withContext(Dispatchers.Main) {
//                searchLoading.visibility = View.GONE
//                searchResultsRc.visibility = View.VISIBLE
//            }
//        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun performShowSearch(query: String)
    {
        searchResultsRc.visibility = View.GONE
//        searchHistoryRC.visibility = View.GONE
        searchHistoryFl.visibility = View.GONE
        isClicked = true
        searchLoading.visibility = View.VISIBLE
//        deleteAll.visibility = View.GONE

//        val query = searchEt.text.toString()
//        GlobalScope.launch(Dispatchers.IO) {
////            searchHistoryDao.deleteAll()
//            val history = SearchHistory(query = query)
//            queryRepository.insert(history)
//            queryRepository.loadData()
//        }

        val history = SearchHistory(query = query)
        viewModel.addToHistory(history)

        viewModel.searchTv(query)
        viewModel.tvList.observe(viewLifecycleOwner){shows->
            if(shows.isNotEmpty()){
                tvAdapter.submitList(shows)
                addRecyclerAnimation(searchResultsRc,tvAdapter)
                searchLoading.visibility = View.GONE
                searchResultsRc.visibility = View.VISIBLE
                searchResultsRc.requestFocus()
            }
            else{
//                Toast.makeText(requireContext(),"No Matches",Toast.LENGTH_SHORT).show()
                searchLoading.visibility = View.GONE
                searchResultsRc.visibility = View.GONE
            }

        }

//        val retrofit = retrofitBuilder()
//
//        val tvService = retrofit.create(TMDBService::class.java)

//        GlobalScope.launch (Dispatchers.Main){
//
//            val searchResults = tvService.searchShow(
//                query,
//                BuildConfig.TMDB_API_KEY,
//                "en-US"
//            )
//
//            if (searchResults.isSuccessful)
//            {
//                val tvShows = searchResults.body()?.results ?: emptyList()
//                if (tvShows.isEmpty()) Toast.makeText(requireContext(),"No Matches",Toast.LENGTH_LONG).show()
//                val adapter = TVShowAdapter(tvShows){it, _ ->
//                    val action = SearchFragmentDirections.actionSearchFragmentToTVShowDetails(it.id, title = it.name)
//                    findNavController().navigate(action)
//                }
//                addRecyclerAnimation(searchResultsRc,adapter)
//            }
//            withContext(Dispatchers.Main) {
//                searchLoading.visibility = View.GONE
//                searchResultsRc.visibility = View.VISIBLE
//            }
//        }

    }



}