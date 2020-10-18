package com.project.bg_tube.data.request

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlayList(
    @PrimaryKey var id: Int?,
    @ColumnInfo(name = "video_url") val videoUrl: String?
){
    constructor(): this(0,"")
}
