@file:Suppress("UNCHECKED_CAST")

package com.demomiru.tokeiv2.utils

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.room.util.query
import com.demomiru.tokeiv2.Movie
import com.demomiru.tokeiv2.MoviesPagingSource
import com.demomiru.tokeiv2.R
import com.demomiru.tokeiv2.TVshow
import com.demomiru.tokeiv2.TvShowPagingSource
import com.demomiru.tokeiv2.anime.AnimeInfo
import com.demomiru.tokeiv2.data.tmdbDataRepository
import com.demomiru.tokeiv2.extractors.SearchResponse
import com.demomiru.tokeiv2.history.QueryRepository
import com.demomiru.tokeiv2.history.SearchHistory
import com.demomiru.tokeiv2.subtitles.Sub
import com.demomiru.tokeiv2.watching.ContinueWatching
import com.demomiru.tokeiv2.watching.ContinueWatchingDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.checkerframework.checker.units.qual.s


class ContinueWatchingViewModelFactory(private val watchingDao: ContinueWatchingDao, private val id:Int) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContinueWatchingViewModel::class.java)){
            return ContinueWatchingViewModel(watchingDao,id) as T
        }
       throw IllegalArgumentException("Unknown ViewModel Class")
    }
}

class ContinueWatchingViewModel(watchingDao: ContinueWatchingDao, id:Int) : ViewModel(){
    val watchFrom : LiveData<ContinueWatching?> = watchingDao.getProgress(id)

    val season = MutableLiveData(0)

}

class ContinueWatchingViewModelFactory2(private val watchingDao: ContinueWatchingDao) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContinueWatchingViewModel2::class.java)){
            return ContinueWatchingViewModel2(watchingDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}

class ContinueWatchingViewModel2(watchingDao: ContinueWatchingDao) : ViewModel(){
    val allWatchHistory : LiveData<List<ContinueWatching>> = watchingDao.getContinueWatchingTest()

    val currentFragment = MutableLiveData(R.id.moviesFragment)
    val searchOpen = MutableLiveData(true)

    //movies
    val trenMovies = Pager(PagingConfig(1)){MoviesPagingSource(2)}.flow.cachedIn(viewModelScope)
    val topMovies = Pager(PagingConfig(1)){MoviesPagingSource(3)}.flow.cachedIn(viewModelScope)
    val popMovies = Pager(PagingConfig(1)){MoviesPagingSource(1)}.flow.cachedIn(viewModelScope)

    //shows
    val tvPopularShows = Pager(PagingConfig(1)){ TvShowPagingSource(1) }.flow.cachedIn(viewModelScope)
    val tvTrendingShows = Pager(PagingConfig(1)){ TvShowPagingSource(2) }.flow.cachedIn(viewModelScope)
    val tvTopShows = Pager(PagingConfig(1)){ TvShowPagingSource(3) }.flow.cachedIn(viewModelScope)

    //anime
    val winterList = MutableLiveData<List<GogoAnime.AnimeSearchResponse>>()
    val fallList = MutableLiveData<List<GogoAnime.AnimeSearchResponse>>()
    val springList = MutableLiveData<List<GogoAnime.AnimeSearchResponse>>()
    val summerList = MutableLiveData<List<GogoAnime.AnimeSearchResponse>>()
    fun getAnime(context: Context) {
        val animeInfo = AnimeInfo(context)
        viewModelScope.launch (Dispatchers.IO){
            winterList.postValue(animeInfo.getSeasonalAnimeInfo(2))
            fallList.postValue(animeInfo.getSeasonalAnimeInfo(1))
            springList.postValue(animeInfo.getSeasonalAnimeInfo(3))
            summerList.postValue(animeInfo.getSeasonalAnimeInfo(4))
        }
    }
}

//class FragmentsViewModel (private val tmdbDataRepo: tmdbDataRepository): ViewModel(){
//    val trMovies = Pager(PagingConfig(1)){ MoviesPagingSource(3) }.flow.cachedIn(viewModelScope)
//    val tMovies = Pager(PagingConfig(1)){MoviesPagingSource(2)}.flow.cachedIn(viewModelScope)
//    val pMovies = Pager(PagingConfig(1)){MoviesPagingSource(1)}.flow.cachedIn(viewModelScope)
//}

class SearchViewModel(private val queryRepository: QueryRepository): ViewModel(){
    val queries = MutableLiveData<List<SearchHistory>>()
    val movieList = MutableLiveData<List<Movie>>()
    val tvList = MutableLiveData<List<TVshow>>()
    val animeList = MutableLiveData<List<GogoAnime.AnimeSearchResponse>>()
    val noMatches = MutableLiveData(false)
    val choice = MutableLiveData(1)
    val queryText = MutableLiveData("")
    val fixTitleSelection = MutableLiveData<List<SearchResponse>>()
    init {
        viewModelScope.launch(Dispatchers.IO) {
            queries.postValue(queryRepository.loadData())
        }
    }

    fun searchMovie(query: String){
        viewModelScope.launch(Dispatchers.IO) {
            val movies = queryRepository.movieSearch(query)
            if (movies.isEmpty()) noMatches.postValue(true)
            movieList.postValue(movies)
        }
    }

    fun searchTv(query: String){
        viewModelScope.launch(Dispatchers.IO) {
            val shows = queryRepository.tvSearch(query)
            if(shows.isEmpty()) noMatches.postValue(true)
           tvList.postValue(shows)
        }
    }

    fun searchAnime(query:String){
        val gogoSrc = GogoAnime()
        viewModelScope.launch(Dispatchers.IO) {
            val anime = gogoSrc.search(query)
            if(anime.isEmpty())noMatches.postValue(true)
            animeList.postValue(anime)
        }
    }

    fun addToHistory(item: SearchHistory){
        viewModelScope.launch (Dispatchers.IO){
            queryRepository.insert(item)
            queries.postValue(queryRepository.loadData())
        }
    }

    fun deleteRecord(item:SearchHistory){
        viewModelScope.launch (Dispatchers.IO){
            queryRepository.deleteRecord(item)
            queries.postValue(queryRepository.loadData())
        }
    }

    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO) {
            queryRepository.deleteAll()
            queries.postValue(queryRepository.loadData())
        }
    }


    val searchClicked = MutableLiveData(false)
}

class SearchVMFactory(private val queryRepository: QueryRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)){
            return SearchViewModel(queryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}

class VideoViewModel(): ViewModel(){
    val langList = MutableLiveData<List<String>>()
    val langMap = MutableLiveData<Map<String,String>>()
    val subs = MutableLiveData<List<SubRest>>()
    val subUri = MutableLiveData<Uri?>()
    fun getLang(context: Context){
        viewModelScope.launch (Dispatchers.IO){
            val lM = OpenSubtitle(context).getLang()
            langMap.postValue(lM)
            val list = lM.map {
                it.key
            }
            langList.postValue(list)
        }
    }

    fun searchSubs(context: Context, id: String, langCode: String, s :Int, e: Int, isMovie: Boolean){
        viewModelScope.launch (Dispatchers.IO) {
           val s =  OpenSubtitle(context).searchSubs(id, langCode, s, e, isMovie)
            subs.postValue(s)
        }
    }

    fun getSub(context: Context, fileLink:String,lang: String, fileName: String){
        viewModelScope.launch (Dispatchers.IO){
            val oS = OpenSubtitle(context)
            oS.getSub2(fileLink,lang, fileName)
            subUri.postValue(oS.getSRT(fileName))
        }
    }


}


