package com.demomiru.tokeiv2.watching

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ContinueWatching::class], version = 2)
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
                    ).addMigrations(MIGRATION_1_2).build()
//                    fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE continue_watching ADD COLUMN origin TEXT")
    }
}

