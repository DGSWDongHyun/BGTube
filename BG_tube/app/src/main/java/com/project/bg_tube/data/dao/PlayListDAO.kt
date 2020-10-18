package com.project.bg_tube.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.project.bg_tube.data.request.PlayList

@Dao
interface PlayListDAO {
    @Query("SELECT * FROM PlayList")
     fun getAll(): List<PlayList>

    @Query("SELECT * FROM PlayList WHERE id IN (:playlistID)")
    fun loadAllByIds(playlistID: IntArray): List<PlayList>

    @Insert
    fun insertAll(vararg playList: PlayList)

    @Delete
    fun delete(playList: PlayList)
}