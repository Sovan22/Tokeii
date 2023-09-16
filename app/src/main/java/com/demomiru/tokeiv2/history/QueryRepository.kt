package com.demomiru.tokeiv2.history

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.demomiru.tokeiv2.history.SearchHistory
import com.demomiru.tokeiv2.history.SearchHistoryDao
import kotlinx.coroutines.flow.Flow

class QueryRepository(private val queryDao: SearchHistoryDao) {

    private val _allQueries = MutableLiveData<List<SearchHistory>>()
    val allQueries : LiveData<List<SearchHistory>> = _allQueries


    fun loadData(){
        _allQueries.postValue(queryDao.getSearchHistory())
    }

    @WorkerThread
    suspend fun deleteRecord(query: SearchHistory){
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
}
