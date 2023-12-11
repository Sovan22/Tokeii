package com.demomiru.tokeiv2



import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.utils.addRecyclerAnimation
import com.demomiru.tokeiv2.utils.passData

import com.demomiru.tokeiv2.utils.retrofitBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MoviesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MoviesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private lateinit var popMovieRc: RecyclerView
    private lateinit var trenMovieRc : RecyclerView
    private lateinit var topMovieRc : RecyclerView
    private var loading = MutableLiveData(true)
    private val adapter = MovieAdapter2{
        println(it.release_date)
        startActivity(passData(it,requireContext()))
    }
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch (Dispatchers.IO){

        }
        super.onViewCreated(view, savedInstanceState)
    }

    @DelicateCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_movies, container, false)
        popMovieRc = view.findViewById(R.id.movie_recycler_view)
        trenMovieRc = view.findViewById(R.id.trending_movie_rc)
        topMovieRc = view.findViewById(R.id.topRatedMovie_recycler_view)


        topMovieRc.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
        trenMovieRc.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
        popMovieRc.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)

        val retrofit = retrofitBuilder()
        val movieService = retrofit.create(MovieService::class.java)

        lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){

                val trmovies =   Pager(PagingConfig(1)){MoviesPagingSource(3)}.flow.cachedIn(lifecycleScope)
                val tradapter = MovieAdapter2{
//                    val action = MoviesFragmentDirections.actionMoviesFragmentToMoviePlayActivity(it.id, "movie")
//                    findNavController().navigate(play(it))
                    println(it.release_date)
                    startActivity(passData(it, requireContext()))

                }

                val tmovies =  Pager(PagingConfig(1)){MoviesPagingSource(2)}.flow.cachedIn(lifecycleScope)
                val tadapter = MovieAdapter2 {
//                    findNavController().navigate(play(it))
                    println(it.release_date)
                    startActivity(passData(it, requireContext()))

                }

                val movies = Pager(PagingConfig(1)){MoviesPagingSource(1)}.flow.cachedIn(lifecycleScope)
                val adapter = MovieAdapter2 {
                    println(it.release_date)
                    startActivity(passData(it, requireContext()))

                }
                withContext(Dispatchers.Main) {
                    addRecyclerAnimation(topMovieRc, tradapter)
                    addRecyclerAnimation(trenMovieRc, tadapter)
                    addRecyclerAnimation(popMovieRc, adapter)


                    lifecycleScope.launch {
                        trmovies.collect {
                            tradapter.submitData(it)
                        }
                    }
                    lifecycleScope.launch {
                        movies.collect{
                            adapter.submitData(it)
                        }
                    }
                    lifecycleScope.launch {
                        tadapter.addLoadStateListener {
                            val state = it.refresh
                            val visible = state is LoadState.Loading
                            if(visible){
                                view.findViewById<ProgressBar>(R.id.loading_movies).visibility = View.VISIBLE
                            }
                            else{
                                view.findViewById<TextView>(R.id.trending_text).visibility = View.VISIBLE
                                view.findViewById<TextView>(R.id.movies_text).visibility = View.VISIBLE
                                view.findViewById<TextView>(R.id.topmovies_text).visibility = View.VISIBLE
                                view.findViewById<ProgressBar>(R.id.loading_movies).visibility = View.GONE
                            }
                        }
//                        view.findViewById<ProgressBar>(R.id.loading_movies).visibility = View.GONE
//                        view.findViewById<TextView>(R.id.trending_text).visibility = View.VISIBLE
//                        view.findViewById<TextView>(R.id.movies_text).visibility = View.VISIBLE
//                        view.findViewById<TextView>(R.id.topmovies_text).visibility = View.VISIBLE
                        tmovies.collect {
                            tadapter.submitData(it)
                        }
                    }


                }
//

        }

//            withContext(Dispatchers.Main) {

//                view.findViewById<ProgressBar>(R.id.loading_movies).visibility = View.GONE
//                view.findViewById<TextView>(R.id.trending_text).visibility = View.VISIBLE
//                view.findViewById<TextView>(R.id.movies_text).visibility = View.VISIBLE
//                view.findViewById<TextView>(R.id.topmovies_text).visibility = View.VISIBLE
//            }
        }


        return view
    }

//    private inline fun CombinedLoadStates.decideOnState(
//        showLoading: (Boolean) -> Unit,
//        showEmptyState: (Boolean) -> Unit,
//        showError: (String) -> Unit
//    ) {
//        showLoading(refresh is LoadState.Loading)
//
//        showEmptyState(
//            source.append is LoadState.NotLoading
//                    && source.append.endOfPaginationReached
//                    && adapter.itemCount == 0
//        )
//
//        val errorState = source.append as? LoadState.Error
//            ?: source.prepend as? LoadState.Error
//            ?: source.refresh as? LoadState.Error
//            ?: append as? LoadState.Error
//            ?: prepend as? LoadState.Error
//            ?: refresh as? LoadState.Error
//
//        errorState?.let { showError(it.error.toString()) }
//    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MoviesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MoviesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}
