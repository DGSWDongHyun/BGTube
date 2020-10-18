package com.project.bg_tube.ui.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.StrictMode
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.project.bg_tube.R
import com.project.bg_tube.data.database.PlayListDataBase
import com.project.bg_tube.data.request.PlayList
import com.project.bg_tube.ui.activites.youtube.DetailActivity
import com.project.bg_tube.ui.adapters.FragmentAdapter
import com.project.bg_tube.ui.adapters.listener.OnItemClickListener
import com.robertlevonyan.views.customfloatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import org.apache.commons.io.IOUtils
import org.json.JSONObject
import java.lang.reflect.Type
import java.net.URL


class BGTubeService : LifecycleService() {
    private var isViewing = false
    private var LongIsViewing = false;
    private var wm : WindowManager ?= null
    private var mView : View ?= null
    private var playList : ArrayList<PlayList> ?= arrayListOf()
    private var serviceAdapter : FragmentAdapter ?= null
    private var youTubePlayers : YouTubePlayer ?= null
    private var notificationManager : NotificationManager ?= null
    private var position = 0;
    private var floatSecond : Float = 0F
    private var cardViewGround : CardView ?= null
    private var cardViewLong : CardView ?= null
    private var recyclerPlayList : RecyclerView ?= null
    private var fab : FloatingActionButton ?= null
    private var intent : Intent ?= null

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onCreate() {
        super.onCreate()

        val listener_youtube: YouTubePlayerListener = object : YouTubePlayerListener {
            override fun onApiChange(youTubePlayer: YouTubePlayer) {
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                floatSecond = second
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
            }

            override fun onPlaybackQualityChange(
                youTubePlayer: YouTubePlayer,
                playbackQuality: PlayerConstants.PlaybackQuality
            ) {

            }

            override fun onPlaybackRateChange(
                youTubePlayer: YouTubePlayer,
                playbackRate: PlayerConstants.PlaybackRate
            ) {
            }

            override fun onReady(youTubePlayer: YouTubePlayer) {
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                if (state == PlayerConstants.PlayerState.ENDED) {
                    if (position < playList!!.size - 1) {
                        position++;
                    } else {
                        position = 0;
                    }
                    youTubePlayers?.loadVideo(
                        playList?.get(position)?.videoUrl!!.substring(
                            32,
                            playList!![position].videoUrl!!.length
                        ), 0F
                    )
                    notificationManager!!.cancel(123)
                    getQuietly(
                        playList?.get(position)?.videoUrl!!,
                        1
                    )?.let { notificationBuild(it) }
                }
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            }

            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
            }

            override fun onVideoLoadedFraction(
                youTubePlayer: YouTubePlayer,
                loadedFraction: Float
            ) {
            }

        }


        CoroutineScope(Dispatchers.Main).launch {
                async {
                    val inflate = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    wm = getSystemService(WINDOW_SERVICE) as WindowManager

                    val params = WindowManager.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        getSystemSDK(),
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT
                    )

                    params.gravity = Gravity.RIGHT or Gravity.TOP
                    mView = inflate.inflate(R.layout.bg_tube_service, null)

                    val youTubePlayerView: YouTubePlayerView = mView!!.findViewById(R.id.youtube_player_view)
                    lifecycle.addObserver(youTubePlayerView)

                    youTubePlayerView.addYouTubePlayerListener(listener_youtube)
                    notificationBuild("Nothing.")

                    fab = mView!!.findViewById(R.id.customFABL)
                    cardViewGround = mView!!.findViewById(R.id.cardViewGround)
                    cardViewLong = mView!!.findViewById(R.id.cardList)
                    recyclerPlayList = mView!!.findViewById(R.id.playListService)

                    GlobalScope.async {
                        playList = getListData() as ArrayList<PlayList>
                    }.await()

                    cardViewGround!!.setOnClickListener {
                        val intent = Intent(applicationContext, DetailActivity::class.java)
                        intent.putExtra("valueVideoID", playList?.get(position)?.videoUrl)
                        intent.putExtra("second", floatSecond)
                        intent.putExtra("position", position)
                        intent.putExtra("title", playList?.get(position)?.title)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent)
                        stopSelf()
                    }
                    serviceAdapter = FragmentAdapter(applicationContext, object : OnItemClickListener {
                        override fun OnItemClick(position: Int) {
                            this@BGTubeService.position = position
                            youTubePlayers?.loadVideo(
                                playList?.get(position)?.videoUrl!!.substring(
                                    32,
                                    playList!![position].videoUrl!!.length
                                ), 0F
                            )
                            cardViewLong!!.visibility = View.GONE;
                            LongIsViewing = false

                            cardViewGround!!.visibility = View.VISIBLE
                            isViewing = true
                            notificationManager!!.cancel(123)
                            getQuietly(
                                playList!![position].videoUrl.toString(),
                                1
                            )?.let { notificationBuild(it) }
                        }
                    })

                    recyclerPlayList!!.layoutManager = LinearLayoutManager(applicationContext)

                    serviceAdapter?.setData(playList!!)
                    recyclerPlayList!!.adapter = serviceAdapter


                    val listener = View.OnLongClickListener {
                        if (LongIsViewing) {
                            cardViewLong!!.visibility = View.GONE
                            LongIsViewing = false
                        } else if (!LongIsViewing && !isViewing) {
                            cardViewLong!!.visibility = View.VISIBLE
                            LongIsViewing = true
                        }
                        true
                    }

                    fab!!.setOnClickListener {
                        if (isViewing) {
                            cardViewGround!!.visibility = View.GONE
                            isViewing = false
                        } else if (!LongIsViewing && !isViewing) {
                            cardViewGround!!.visibility = View.VISIBLE
                            isViewing = true
                        }
                    }

                    fab!!.setOnLongClickListener(listener)
                    youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayers = youTubePlayer

                            if(intent != null && intent!!.hasExtra("videoID") && intent!!.hasExtra("secondValue") && intent!!.hasExtra("titleValue")) {

                                cardViewLong!!.visibility = View.GONE;
                                LongIsViewing = false

                                cardViewGround!!.visibility = View.VISIBLE
                                isViewing = true


                                youTubePlayers?.loadVideo(intent?.getStringExtra("videoID").toString(),
                                    intent?.getFloatExtra("secondValue", 0F)!!.toFloat())

                                notificationBuild(intent?.getStringExtra("titleValue")!!)

                            }
                        }
                    })

                    wm!!.addView(mView, params)
                }.await()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        super.onStartCommand(intent, flags, startId)
        if(intent!!.hasExtra("videoID")){
            Log.d("ResultOfHasExtra", intent?.getStringExtra("videoID").toString());
            this.intent = intent
        }
        return START_REDELIVER_INTENT
    }

    fun getQuietly(youtubeUrl: String?, requestCode: Int): String? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        var embededURL : URL? = null

        embededURL = URL("http://www.youtube.com/oembed?url=$youtubeUrl&format=json")

        when(requestCode){
            1 -> {
                 return JSONObject(IOUtils.toString(embededURL)).getString("title")
            }
            2 -> {
                return JSONObject(IOUtils.toString(embededURL)).getString("author_name")
            }
            3 -> {
                return JSONObject(IOUtils.toString(embededURL)).getString("thumbnail_url")
            }
        }
        return null
    }
    fun notificationBuild(strTitle : String){
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(applicationContext, "0")
            .setOngoing(true)
            .setContentText("Now Playing - $strTitle")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName: CharSequence = "channel"
            notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground) //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("0", channelName, importance)
            assert(notificationManager != null)
            notificationManager!!.createNotificationChannel(channel)
        } else notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)

        notificationManager!!.notify(123, notificationBuilder.build())
    }

    private fun getListData(): List<PlayList> {
        val db = Room.databaseBuilder(
            applicationContext,
            PlayListDataBase::class.java, "PlayListDB"
        ).build()

        return db.playListDAO().getAll()
    }
    private fun getSystemSDK() : Int {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            return  WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }else{
            return  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(wm != null) {
            if(mView != null) {
                wm!!.removeView(mView);
                mView = null;
            }
            wm = null;
        }
    }
}
