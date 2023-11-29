package com.demomiru.tokeiv2.anime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.databinding.FragmentAnimeBinding
import com.demomiru.tokeiv2.utils.encodeStringToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeFragment : Fragment() {

    private lateinit var winterAnimeRc: RecyclerView
    private lateinit var fallAnimeRc: RecyclerView
    private lateinit var springAnimeRc : RecyclerView
    private lateinit var summerAnimeRc : RecyclerView
    private lateinit var binding: FragmentAnimeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentAnimeBinding.inflate(inflater, container, false)
        val animeInfo = AnimeInfo(requireContext())

        winterAnimeRc = binding.winterAnimeRc
        fallAnimeRc = binding.fallAnimeRc
        springAnimeRc = binding.springAnimeRc
        summerAnimeRc = binding.summerAnimeRc

        winterAnimeRc.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL,false)
        fallAnimeRc.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL,false)
        springAnimeRc.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL,false)
        summerAnimeRc.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL,false)

//        val action = AnimeFragmentDirections.actionAnimeFragmentToTVShowDetails(
//            encodeStringToInt(it.name).toString(), title = "",animeUrl = it.url)
//        findNavController().navigate(action)


        val wAdapter = AnimeAdapter(requireContext()){
            val action = AnimeFragmentDirections.actionAnimeFragmentToAnimeDetailsFragment(
                it.name,it.url)
            findNavController().navigate(action)
        }
        winterAnimeRc.adapter = wAdapter

        val fAdapter = AnimeAdapter(requireContext()){
            val action = AnimeFragmentDirections.actionAnimeFragmentToAnimeDetailsFragment(
                it.name,it.url)
            findNavController().navigate(action)
        }
        fallAnimeRc.adapter = fAdapter

        val spAdapter = AnimeAdapter(requireContext()){
            val action = AnimeFragmentDirections.actionAnimeFragmentToAnimeDetailsFragment(
                it.name,it.url)
            findNavController().navigate(action)
        }
       springAnimeRc.adapter = spAdapter

        val suAdapter = AnimeAdapter(requireContext()){
            val action = AnimeFragmentDirections.actionAnimeFragmentToAnimeDetailsFragment(
                it.name,it.url)
            findNavController().navigate(action)
        }
        summerAnimeRc.adapter = suAdapter
        try {
            lifecycleScope.launch(Dispatchers.IO) {


                val winterList = animeInfo.getSeasonalAnimeInfo(2)
                val fallList = animeInfo.getSeasonalAnimeInfo(1)
                val springList = animeInfo.getSeasonalAnimeInfo(3)
                val summerList = animeInfo.getSeasonalAnimeInfo(4)

                withContext(Dispatchers.Main) {
                    wAdapter.submitList(winterList)
                    fAdapter.submitList(fallList)
                    spAdapter.submitList(springList)
                    suAdapter.submitList(summerList)
                    binding.loadingAnime.visibility = View.GONE
                    binding.winterText.visibility = View.VISIBLE
                    binding.fallText.visibility = View.VISIBLE
                    binding.springText.visibility = View.VISIBLE
                    binding.summerText.visibility = View.VISIBLE
                }

            }
        }catch (e:Exception){
            e.printStackTrace()
        }

        return binding.root
    }





}