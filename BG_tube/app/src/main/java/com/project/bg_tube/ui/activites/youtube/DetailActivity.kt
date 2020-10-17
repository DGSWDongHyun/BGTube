package com.project.bg_tube.ui.activites.youtube

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.project.bg_tube.R
import com.project.bg_tube.ui.services.BGTubeService

class DetailActivity : AppCompatActivity() {

    var videoId : String ?= null
    var videoLength : Float ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_detail)

        var intent : Intent = intent

        videoId = intent.getStringExtra("valueVideoID")!!.substring(32, intent.getStringExtra("valueVideoID")!!.length)
        videoLength = intent.getFloatExtra("second",0F)

        Log.d("link", intent.getStringExtra("valueVideoID")!!.substring(32,intent.getStringExtra("valueVideoID")!!.length))
        val youTubePlayerView: YouTubePlayerView = findViewById(R.id.youtube_player_view)
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(
                    intent.getStringExtra("valueVideoID")!!
                        .substring(32, intent.getStringExtra("valueVideoID")!!.length), intent.getFloatExtra("second",0F))
            }
        })
    }

    override fun onStop() {
        val intent : Intent = Intent(this, BGTubeService::class.java)
        intent.putExtra("videoID", videoId)
        intent.putExtra("secondValue", videoLength)
        startService(intent)
        super.onStop()
    }
}
