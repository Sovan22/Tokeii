package com.demomiru.tokeiv2.watching

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ContinueWatchingRepository(private val continueWatchingDao : ContinueWatchingDao) {
    private val _allWatchHistory = MutableLiveData<List<ContinueWatching>>()
    val allWatchHistory : LiveData<List<ContinueWatching>> = _allWatchHistory

    fun loadData(){
        _allWatchHistory.postValue(continueWatchingDao.getContinueWatching())
    }

    @WorkerThread
    fun deleteRecord(query: ContinueWatching){
        continueWatchingDao.deleteRecord(query.tmdbID!!)
    }

    @WorkerThread
    suspend fun insert(query: ContinueWatching) {
        continueWatchingDao.insert(query)
    }

    @WorkerThread
    suspend fun delete(query: ContinueWatching) {
        continueWatchingDao.delete(query)
    }

    @WorkerThread
    fun deleteAll(){
        continueWatchingDao.deleteAll()
    }

//    fun getProgress(id:Int) : ContinueWatching?{
//       return continueWatchingDao.getProgress(id)
//    }
}