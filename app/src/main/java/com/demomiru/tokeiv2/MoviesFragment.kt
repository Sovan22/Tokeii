package com.demomiru.tokeiv2


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.utils.play
import com.demomiru.tokeiv2.utils.retrofitBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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

        GlobalScope.launch(Dispatchers.Main) {
            val pmovieResponse = movieService.getPopularMovies(
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )

            val tmovieResponse = movieService.getTrendingMovies(
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )

            val topMovieResponse = movieService.getTopRatedMovies(
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )

            if (topMovieResponse.isSuccessful)
            {
                val movies = tmovieResponse.body()?.results?: emptyList()
                topMovieRc.adapter = MovieAdapter(movies){
//                    val action = MoviesFragmentDirections.actionMoviesFragmentToMoviePlayActivity(it.id, "movie")
                    findNavController().navigate(play(it))
                }

            }

            if(tmovieResponse.isSuccessful){
                val movies = tmovieResponse.body()?.results?: emptyList()
                trenMovieRc.adapter = MovieAdapter(movies){
                    findNavController().navigate(play(it))
                }

            }


            if (pmovieResponse.isSuccessful) {
                val movies = pmovieResponse.body()?.results ?: emptyList()
                popMovieRc.adapter = MovieAdapter(movies){
//                    val action = MoviesFragmentDirections.actionMoviesFragmentToMoviePlayActivity(it.id, "movie")
//                    findNavController().navigate(action)
                    findNavController().navigate(play(it))
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