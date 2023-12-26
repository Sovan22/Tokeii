package com.demomiru.tokeiv2.anime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.databinding.FragmentAnimeBinding
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModel2
import com.demomiru.tokeiv2.utils.encodeStringToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeFragment : Fragment() {
    private val activityViewModel : ContinueWatchingViewModel2 by activityViewModels()
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

                activityViewModel.getAnime(requireContext())


                    activityViewModel.winterList.observe(viewLifecycleOwner){winterList->
                        wAdapter.submitList(winterList)
                        if(winterList.isNotEmpty()){
                            binding.loadingAnime.visibility = View.GONE
                            binding.winterText.visibility = View.VISIBLE
                            binding.fallText.visibility = View.VISIBLE
                            binding.springText.visibility = View.VISIBLE
                            binding.summerText.visibility = View.VISIBLE
                        }
                    }
                    activityViewModel.fallList.observe(viewLifecycleOwner){fallList->
                        fAdapter.submitList(fallList)
                    }

                    activityViewModel.springList.observe(viewLifecycleOwner){springList->
                        spAdapter.submitList(springList)
                    }

                    activityViewModel.summerList.observe(viewLifecycleOwner){summerList->
                        suAdapter.submitList(summerList)
                    }

        }catch (e:Exception){
            e.printStackTrace()
        }

        return binding.root
    }

    override fun onResume() {
        activityViewModel.currentFragment.value = R.id.animeFragment
        super.onResume()
    }



}