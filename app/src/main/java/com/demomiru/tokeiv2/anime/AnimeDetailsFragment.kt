package com.demomiru.tokeiv2.anime

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.TVShowDetailsArgs

import com.demomiru.tokeiv2.databinding.FragmentAnimeDetailsBinding
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModel
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModel2
import com.demomiru.tokeiv2.utils.ContinueWatchingViewModelFactory
import com.demomiru.tokeiv2.utils.GogoAnime
import com.demomiru.tokeiv2.utils.encodeStringToInt
import com.demomiru.tokeiv2.utils.passData
import com.demomiru.tokeiv2.watching.ContinueWatching
import com.demomiru.tokeiv2.watching.ContinueWatchingDatabase
import com.google.common.collect.Iterators.getNext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeDetailsFragment : Fragment() {
    private val activityViewModel: ContinueWatchingViewModel2 by activityViewModels()
    private lateinit var viewModelFactory: ContinueWatchingViewModelFactory
    private val viewModel: ContinueWatchingViewModel by viewModels(
        factoryProducer = {
            viewModelFactory
        }
    )
    private lateinit var episodeProgress : ContinueWatching
    private val database by lazy { ContinueWatchingDatabase.getInstance(requireContext()) }
    private val watchHistoryDao by lazy { database.watchDao() }
    private val args : AnimeDetailsFragmentArgs by navArgs()

    private lateinit var binding: FragmentAnimeDetailsBinding
    private lateinit var sequelBtn : Button
    private lateinit var prequelBtn : Button
    private val gogoSrc = GogoAnime()
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val viewStateObserver = Observer<ContinueWatching?> {watchFrom ->
            val continueButton =  binding.continueButton
            if (watchFrom != null) {
                episodeProgress = watchFrom
                continueButton.visibility = View.VISIBLE
                continueButton.text =
                    "Continue Watching E${watchFrom.episode+1}"
            }

            continueButton.setOnClickListener {
                startActivity(passData(watchFrom!!, requireContext()))
            }
        }
        viewModel.watchFrom.observe(viewLifecycleOwner,viewStateObserver)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAnimeDetailsBinding.inflate(inflater, container, false)
        val animeInfo = AnimeInfo(requireContext())
        sequelBtn = binding.sequelBtn
        prequelBtn = binding.prequelBtn

        val title = args.title
        val url = args.url
        viewModelFactory = ContinueWatchingViewModelFactory(watchHistoryDao, encodeStringToInt(title))
        val progressBar = binding.progressCircular

        val episodesRc = binding.episodeDisplayRc
        episodesRc.layoutManager = LinearLayoutManager(requireContext())


        val titleTv =binding.titleShow
        val backdropImg =binding.showBackdrop
        val posterImg= binding.showPoster
        val overview = binding.overviewText

        overview.setAnimationDuration(750L)
        binding.expandText.setOnClickListener {
            if(overview.isExpanded){
                overview.collapse()
                binding.expandText.load(R.drawable.baseline_keyboard_arrow_down_24)
            }else{
                overview.expand()
                binding.expandText.load(R.drawable.baseline_keyboard_arrow_up_24)
            }
        }


        lifecycleScope.launch (Dispatchers.IO) {
            val details = animeInfo.getAnimeDetails(title)
            val gogoDetails: GogoAnime.AnimeDetails = try{gogoSrc.load(url)}catch (e:Exception){
                println(e.printStackTrace())
                GogoAnime.AnimeDetails()
            }

            withContext(Dispatchers.Main){
                println(details.related_anime)
                val sequel = sequelExist(details.related_anime)
                if(sequel.second){
                    sequelBtn.visibility = View.VISIBLE
                    sequelBtn.setOnClickListener {
                        if(sequel.first?.node?.title == null) Toast.makeText(requireContext(),"No sequel available",Toast.LENGTH_SHORT).show()
                        else getNext(sequel.first?.node?.title!!)
                    }
                }

                val prequel = prequelExist(details.related_anime)
                if(prequel.second) {
                    prequelBtn.visibility = View.VISIBLE
                    prequelBtn.setOnClickListener {

                        if(prequel.first?.node?.title == null) Toast.makeText(requireContext(),"No prequel available",Toast.LENGTH_SHORT).show()
                        else getNext(prequel.first?.node?.title!!)
                    }

                }

               binding.progressLayout.visibility = View.GONE
                posterImg.load(gogoDetails.poster)
                backdropImg.load(gogoDetails.poster)
                overview.text = details.synopsis.replace("[Written by MAL Rewrite]","")
                titleTv.text = gogoDetails.title
                val episodes = gogoDetails.episodes
                val adapter = AnimeEpisodeAdapter{ _, epPos->
                    startActivity(
                        passData(
                           gogoDetails,
                            requireContext(),
                            encodeStringToInt(gogoDetails.title?:"").toString(),
                            epPos,
                            gogoDetails.title?:""
                        )
                    )
                }
                episodesRc.adapter = adapter
                adapter.submitList(episodes)
                val context = episodesRc.context
                val controller = AnimationUtils.loadLayoutAnimation(
                    context,
                    R.anim.layout_animation
                )
                episodesRc.layoutAnimation = controller
                adapter.notifyDataSetChanged()
                episodesRc.scheduleLayoutAnimation()
                binding.episodesText.visibility =
                    View.VISIBLE
            }
        }
        return binding.root
    }

    private fun sequelExist(related: ArrayList<AnimeInfo.Anime>): Pair<AnimeInfo.Anime?, Boolean>
    {
        related.forEach {
            if (it.relation_type?.contains("sequel") == true){
                return Pair(it,true)
            }
        }
        return Pair(null,false)
    }

    private fun prequelExist(related: ArrayList<AnimeInfo.Anime>): Pair<AnimeInfo.Anime?, Boolean>
    {
        related.forEach {
            if (it.relation_type?.contains("prequel") == true){
                return Pair(it,true)
            }
        }
        return Pair(null,false)
    }

    private fun getNext(title: String){
        lifecycleScope.launch {
            val animeList = gogoSrc.search(title)
            println(animeList)
            withContext(Dispatchers.Main){
                val anime = animeList[0]
                val action = AnimeDetailsFragmentDirections.actionAnimeDetailsFragmentSelf(anime.name,anime.url)
                findNavController().navigate(action)
            }
        }
    }

    override fun onResume() {
        activityViewModel.currentFragment.value = R.id.animeDetailsFragment
        super.onResume()
    }



}