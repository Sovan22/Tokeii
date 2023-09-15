package com.demomiru.tokeiv2.history

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchHistory: SearchHistory)

    @Delete
    suspend fun delete(searchHistory: SearchHistory)

//    @Query("DELETE FROM search_history WHERE `query`=:query")
//    fun deleteRecord(query: String)

    @Query("SELECT COUNT(*) FROM search_history")
    fun getLastID() : Int

    @Query("SELECT `query` FROM search_history ORDER BY id DESC")
    fun getSearchHistory(): List<String>

    @Query("DELETE FROM search_history")
    fun deleteAll()
}