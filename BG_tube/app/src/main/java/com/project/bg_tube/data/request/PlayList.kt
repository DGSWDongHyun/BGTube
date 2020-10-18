package com.project.bg_tube.data.request

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlayList(
    @PrimaryKey var id: Int?,
    @ColumnInfo(name = "video_url") val videoUrl: String?,
    @ColumnInfo(name = "title") val title : String?,
    @ColumnInfo(name = "thumbnail_url") val thumbnail_url : String?,
    @ColumnInfo(name = "author_name") val author_name : String?
){
    constructor(): this(0,"", "", "", "")
}
