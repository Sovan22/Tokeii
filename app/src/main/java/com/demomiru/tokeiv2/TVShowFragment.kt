package com.demomiru.tokeiv2

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.findNavController
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.utils.addRecyclerAnimation
import com.demomiru.tokeiv2.utils.playShow
import com.demomiru.tokeiv2.utils.retrofitBuilder
import com.google.android.material.transition.Hold
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
 * Use the [TVShowFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TVShowFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var popTvRc: RecyclerView
    private lateinit var trenTvRc: RecyclerView
    private lateinit var topTvRc : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
//        exitTransition = Hold()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_tv_show, container, false)
        popTvRc = view.findViewById(R.id.popular_tvshow_rc)
        trenTvRc = view.findViewById(R.id.trending_tvshow_rc)
        topTvRc = view.findViewById(R.id.toprated_tvshow_rc)

        val retrofit = retrofitBuilder()
        topTvRc.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        popTvRc.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        trenTvRc.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)

        val tvService = retrofit.create(TMDBService::class.java)

        GlobalScope.launch(Dispatchers.Main) {
            val tvPopularResponse = tvService.getPopularTVShows(
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )

            val tvTrendingResponse = tvService.getTrendingTVShows(
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )

            val tvTopResponse = tvService.getTopRatedTVShows(
                "cab731891b28c5ad61c85cd993851ed7",
                "en-US"
            )

            if(tvTopResponse.isSuccessful){
                val tvShows = tvTopResponse.body()?.results ?: emptyList()
                val adapter = TVShowAdapter(tvShows){it, position->
//                    val action = TVShowFragmentDirections.actionTVShowFragmentToTVShowDetails(it.id)
                    findNavController().navigate(playShow(it,position))
                }
                addRecyclerAnimation(topTvRc,adapter)
            }

            if (tvTrendingResponse.isSuccessful) {
                val tvShows = tvTrendingResponse.body()?.results ?: emptyList()
                val adapter = TVShowAdapter(tvShows){it, position->
//                    val action = TVShowFragmentDirections.actionTVShowFragmentToTVShowDetails(it.id)
                    findNavController().navigate(playShow(it,position))
                }
                addRecyclerAnimation(trenTvRc,adapter)
            }

            if (tvPopularResponse.isSuccessful) {
                val tvShows = tvPopularResponse.body()?.results ?: emptyList()
                val adapter = TVShowAdapter(tvShows){it, position->
//                    val action = TVShowFragmentDirections.actionTVShowFragmentToTVShowDetails(it.id)
                    findNavController().navigate(playShow(it,position))
                }
                addRecyclerAnimation(popTvRc,adapter)
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
         * @return A new instance of fragment TVShowFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TVShowFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}