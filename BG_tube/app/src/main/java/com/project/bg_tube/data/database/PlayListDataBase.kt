package com.project.bg_tube.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.bg_tube.data.dao.PlayListDAO
import com.project.bg_tube.data.request.PlayList

@Database(entities = arrayOf(PlayList::class), version = 1)
abstract class PlayListDataBase : RoomDatabase() {
    abstract fun playListDAO(): PlayListDAO
    companion object {

        private var INSTANCE: PlayListDataBase? = null

        fun getInstance(context: Context): PlayListDataBase? {
            if(INSTANCE == null) {
                synchronized(PlayListDataBase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, PlayListDataBase::class.java, "PlayListDB").build()
                }
            }
            return INSTANCE
        }
    }
}