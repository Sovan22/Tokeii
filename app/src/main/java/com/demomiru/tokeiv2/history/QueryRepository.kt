package com.demomiru.tokeiv2.history

import android.view.View
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.demomiru.tokeiv2.BuildConfig
import com.demomiru.tokeiv2.Movie
import com.demomiru.tokeiv2.MovieAdapter
import com.demomiru.tokeiv2.MovieService
import com.demomiru.tokeiv2.TMDBService
import com.demomiru.tokeiv2.TVshow
import com.demomiru.tokeiv2.history.SearchHistory
import com.demomiru.tokeiv2.history.SearchHistoryDao
import com.demomiru.tokeiv2.utils.addRecyclerAnimation
import com.demomiru.tokeiv2.utils.passData
import com.demomiru.tokeiv2.utils.retrofitBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.create

class QueryRepository(private val queryDao: SearchHistoryDao) {

    private val _allQueries = MutableLiveData<List<SearchHistory>>()
    val allQueries : LiveData<List<SearchHistory>> = _allQueries


//    fun loadData(){
//        _allQueries.postValue(queryDao.getSearchHistory())
//    }
    private val retrofit = retrofitBuilder()

    private val movieService = retrofit.create(MovieService::class.java)
    private val tvService = retrofit.create(TMDBService::class.java)

    fun loadData(): List<SearchHistory>{
        return queryDao.getSearchHistory()
    }

    @WorkerThread
    fun deleteRecord(query: SearchHistory){
        queryDao.deleteRecord(query.query)
    }

    @WorkerThread
    suspend fun insert(query: SearchHistory) {
        queryDao.insert(query)
    }

    @WorkerThread
    suspend fun delete(query: SearchHistory) {
        queryDao.delete(query)
    }

    @WorkerThread
    fun deleteAll(){
        queryDao.deleteAll()
    }

    @WorkerThread
    suspend fun movieSearch(query:String): List<Movie>{
        val searchResults = movieService.searchMovie(
            query,
            BuildConfig.TMDB_API_KEY,
            "en-US"
        )

        return if (searchResults.isSuccessful) {
            searchResults.body()?.results ?: emptyList()
        } else
            emptyList()
    }

    @WorkerThread
    suspend fun tvSearch(query:String): List<TVshow>{
        val searchResults = tvService.searchShow(
            query,
            BuildConfig.TMDB_API_KEY,
            "en-US"
        )

        return if (searchResults.isSuccessful) {
            searchResults.body()?.results ?: emptyList()
        } else
            emptyList()
    }
}
