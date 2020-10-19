package com.project.bg_tube.ui.activites.youtube

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.project.bg_tube.R
import com.project.bg_tube.data.database.PlayListDataBase
import com.project.bg_tube.data.request.PlayList
import com.project.bg_tube.databinding.ActivityDetailBinding
import com.project.bg_tube.ui.adapters.FragmentAdapter
import com.project.bg_tube.ui.adapters.listener.OnItemClickListener
import com.project.bg_tube.ui.services.BGTubeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    var title : String ?= null
    var videoId : String ?= null
    var videoLength : Float ?= null
    var position : Int ?= null
    private var playList : ArrayList<PlayList> ?= null
    private var youTubePlayers : YouTubePlayer ?= null
    private var bindingDetail : ActivityDetailBinding ?= null
    private var adapter : FragmentAdapter ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        bindingDetail = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(bindingDetail?.root)

        var intent : Intent = intent

        videoId = intent.getStringExtra("valueVideoID")!!.substring(32, intent.getStringExtra("valueVideoID")!!.length)
        title = intent.getStringExtra("title")
        position = intent.getIntExtra("position",0);

        Log.d("link", intent.getStringExtra("valueVideoID")!!.substring(32,intent.getStringExtra("valueVideoID")!!.length))
        val youTubePlayerView: YouTubePlayerView = findViewById(R.id.youtube_player_view)
        lifecycle.addObserver(youTubePlayerView)

        GlobalScope.launch(Dispatchers.Main) {
            GlobalScope.async {
                playList = getListData() as ArrayList<PlayList>
            }.await()


            bindingDetail?.recyclerViewPlayLists?.layoutManager = LinearLayoutManager(applicationContext)
            bindingDetail?.recyclerViewPlayLists?.adapter = adapter
            adapter?.setData(playList!!)

            youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayers = youTubePlayer
                    youTubePlayer.loadVideo(
                       playList?.get(position!!)?.videoUrl!!.substring(32, playList?.get(position!!)?.videoUrl!!.length), intent.getFloatExtra("second",0F))
                }
            })
        }

        adapter = FragmentAdapter(applicationContext, object : OnItemClickListener{
            override fun OnItemClick(position: Int) {
                youTubePlayers?.loadVideo(
                    playList?.get(position!!)?.videoUrl!!.substring(32, playList?.get(position!!)?.videoUrl!!.length), 0F)
            }
        })

        youTubePlayerView.addYouTubePlayerListener(object : YouTubePlayerListener{
            override fun onApiChange(youTubePlayer: YouTubePlayer) { }
            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) { videoLength = second }
            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) { }
            override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) { }
            override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) { }
            override fun onReady(youTubePlayer: YouTubePlayer) { }
            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                if (state == PlayerConstants.PlayerState.ENDED) {
                    if (position!! < playList!!.size - 1) {
                        position?.plus(1);
                    } else {
                        position = 0;
                    }
                    youTubePlayers?.loadVideo(
                        playList?.get(position!!)?.videoUrl!!.substring(
                            32,
                            playList!![position!!].videoUrl!!.length
                        ), 0F
                    )
                }
            }
            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) { }
            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) { }
            override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
        })
    }
    private fun getListData(): List<PlayList> {
        val db = Room.databaseBuilder(
            applicationContext,
            PlayListDataBase::class.java, "PlayListDB"
        ).build()

        return db.playListDAO().getAll()
    }
    override fun onStop() {
        val intent : Intent = Intent(this, BGTubeService::class.java)
        intent.putExtra("videoID", videoId)
        intent.putExtra("secondValue", videoLength)
        intent.putExtra("titleValue", title)
        startService(intent)
        finish()
        super.onStop()
    }
}
