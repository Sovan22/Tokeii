package com.demomiru.tokeiv2.watching

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ContinueWatching::class], version = 1)
@TypeConverters(EpisodeListTypeConverter::class)
abstract class ContinueWatchingDatabase : RoomDatabase(){

    abstract fun watchDao() : ContinueWatchingDao

    companion object{

        @Volatile
        private var INSTANCE: ContinueWatchingDatabase? = null

        fun getInstance(context : Context) : ContinueWatchingDatabase{
            synchronized(this){
                var instance = INSTANCE

                if (instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                       ContinueWatchingDatabase::class.java,
                        "continue_watching_database"
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}