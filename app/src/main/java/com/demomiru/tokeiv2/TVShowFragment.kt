package com.demomiru.tokeiv2

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.findNavController
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.demomiru.tokeiv2.utils.addRecyclerAnimation
import com.demomiru.tokeiv2.utils.playShow
import com.demomiru.tokeiv2.utils.retrofitBuilder

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TVShowFragment : Fragment() {

    private lateinit var popTvRc: RecyclerView
    private lateinit var trenTvRc: RecyclerView
    private lateinit var topTvRc : RecyclerView




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

        lifecycleScope.launch(Dispatchers.IO) {
            val tvPopularShows = Pager(PagingConfig(1)){TvShowPagingSource(1)}.flow.cachedIn(lifecycleScope)


            val tvTrendingShows = Pager(PagingConfig(1)){TvShowPagingSource(2)}.flow.cachedIn(lifecycleScope)

            val tvTopShows = Pager(PagingConfig(1)){TvShowPagingSource(3)}.flow.cachedIn(lifecycleScope)

            val topAdapter = TVShowAdapter2{ it, position->
//                    val action = TVShowFragmentDirections.actionTVShowFragmentToTVShowDetails(it.id)
                    findNavController().navigate(playShow(it,position,it.name))
            }



            val trenAdapter = TVShowAdapter2{it, position->
//                    val action = TVShowFragmentDirections.actionTVShowFragmentToTVShowDetails(it.id)
                    findNavController().navigate(playShow(it,position,it.name))
                }



            val popAdapter = TVShowAdapter2{it, position->
//                    val action = TVShowFragmentDirections.actionTVShowFragmentToTVShowDetails(it.id)
                    findNavController().navigate(playShow(it,position,it.name))
            }



            withContext(Dispatchers.Main){
                addRecyclerAnimation(popTvRc,popAdapter)
                addRecyclerAnimation(trenTvRc,trenAdapter)
                addRecyclerAnimation(topTvRc,topAdapter)
                lifecycleScope.launch {
                    tvTrendingShows.collect{
                        trenAdapter.submitData(it)
                    }
                }

                lifecycleScope.launch {
                    tvPopularShows.collect{
                        popAdapter.submitData(it)
                    }
                }

                lifecycleScope.launch {
                    topAdapter.addLoadStateListener {
                        val state = it.refresh
                        val visible = state is LoadState.Loading
                        if(visible)
                        {
                            view.findViewById<ProgressBar>(R.id.loading_tvshow).visibility = View.VISIBLE
                        }else{
                            view.findViewById<ProgressBar>(R.id.loading_tvshow).visibility = View.GONE
                            view.findViewById<TextView>(R.id.trending_text).visibility = View.VISIBLE
                            view.findViewById<TextView>(R.id.popular_text).visibility = View.VISIBLE
                            view.findViewById<TextView>(R.id.topShows_text).visibility = View.VISIBLE
                        }
                    }
                    tvTopShows.collect{
                        topAdapter.submitData(it)
                    }
                }

            }
        }
        return view
    }
}