@file:Suppress("UNCHECKED_CAST")

package com.demomiru.tokeiv2.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.demomiru.tokeiv2.watching.ContinueWatching
import com.demomiru.tokeiv2.watching.ContinueWatchingDao


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
}
