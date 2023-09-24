package com.demomiru.tokeiv2.watching

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContinueWatchingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(continueWatching: ContinueWatching)

    @Delete
    suspend fun delete(continueWatching: ContinueWatching)

    @Query("DELETE FROM continue_watching WHERE `tmdbID`=:id")
    fun deleteRecord(id:Int)

    @Query("SELECT * FROM continue_watching")
    fun getContinueWatching(): List<ContinueWatching>
    @Query("SELECT * FROM continue_watching")
    fun getContinueWatchingTest(): LiveData<List<ContinueWatching>>

    @Query("DELETE FROM continue_watching")
    fun deleteAll()

    @Query("SELECT * FROM continue_watching WHERE `tmdbID`=:id")
    fun getProgress(id:Int) : LiveData<ContinueWatching?>
}