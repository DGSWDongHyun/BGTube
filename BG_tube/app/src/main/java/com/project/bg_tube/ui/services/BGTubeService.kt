package com.project.bg_tube.ui.services

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleService
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.project.bg_tube.R
import com.robertlevonyan.views.customfloatingactionbutton.FloatingActionButton
import mbanje.kurt.fabbutton.*


class BGTubeService : LifecycleService() {

    private var isViewing = false
    private var LongIsViewing = false;
    private var wm : WindowManager ?= null
    private var mView : View ?= null


    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onCreate() {
        super.onCreate()

        val inflate = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
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

        val fab : FloatingActionButton = mView!!.findViewById(R.id.customFABL)
        val cardViewGround : CardView = mView!!.findViewById(R.id.cardViewGround)
        val cardViewLong : CardView = mView!!.findViewById(R.id.cardList)

        val listener = View.OnLongClickListener {
            if(LongIsViewing){
                cardViewLong.visibility = View.GONE
                LongIsViewing = false
            }else if(!LongIsViewing && !isViewing){
                cardViewLong.visibility = View.VISIBLE
                LongIsViewing = true
            }
            true
        }

        fab.setOnClickListener {
            if(isViewing){
                cardViewGround.visibility = View.GONE
                isViewing = false
            }else if(!LongIsViewing && !isViewing){
                cardViewGround.visibility = View.VISIBLE
                isViewing = true
            }
        }

        fab.setOnLongClickListener(listener)
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = "wKyMIrBClYw"
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })

        wm!!.addView(mView, params)
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
