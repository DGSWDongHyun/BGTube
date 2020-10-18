package com.project.bg_tube.ui.activites

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.project.bg_tube.R
import com.project.bg_tube.data.database.PlayListDataBase
import com.project.bg_tube.data.request.PlayList
import com.project.bg_tube.ui.adapters.FragmentAdapter
import com.project.bg_tube.ui.services.BGTubeService
import com.project.bg_tube.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private var databaseP : PlayListDataBase ?= null
    private var mainViewModel : MainViewModel ?= null
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE : Int = 1
    private var fragmentAdapter : FragmentAdapter ?= null
    private var list : ArrayList<PlayList> ?= arrayListOf()
    private val string_regex : String = "[https:]+\\:+\\/+[www]+\\.+[youtube]+\\.+[com]+\\/+[ watch ]+\\?+[v]+\\=+[a-z A-Z 0-9 _ \\- ? !]+"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        databaseP = PlayListDataBase.getInstance(this)

        checkPermission()
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        GlobalScope.launch (Dispatchers.Main) {
            fragmentAdapter = mainViewModel?.dataAdapter?.value
            mainViewModel?.dataAdapter?.value = fragmentAdapter
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            GlobalScope.launch (Dispatchers.Main) {
                if(mainViewModel?.dataAdapter?.value == null){
                }else{
                    fragmentAdapter = mainViewModel?.dataAdapter?.value
                    makeAlert()
                    list = mainViewModel?.dataAdapter?.value!!.getData()


                }
            }
        }

    }
    private fun isServiceRunningCheck(): Boolean {
        val manager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if ("com.project.bg_tube.ui.services.BGTubeService" == service.service.className) {
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(123)
                return true
            }
        }
        return false
    }
    override fun onStart() {
        super.onStart()
        if(isServiceRunningCheck()) run {
            stopService(Intent(this, BGTubeService::class.java))
        }

    }


    fun makeAlert() {
        val editText: EditText = EditText(this)
        editText.hint = "유튜브 링크를 입력하세요."

        val alertDialog: AlertDialog? = AlertDialog.Builder(this)
            .setTitle("유튜브 링크를 입력하세요.")
            .setView(editText)
            .setPositiveButton("확인") { dialogInterface, i ->
                if(Pattern.matches(string_regex, editText.text.toString())){
                    try{
                        GlobalScope.launch(Dispatchers.Main) {

                            list?.add(PlayList(0,editText.text.toString()))
                            mainViewModel?.dataAdapter?.value!!.setData(list!!)


                            GlobalScope.async {
                                PlayListDataBase.getInstance(applicationContext!!)?.playListDAO()?.insertAll(PlayList(
                                    (mainViewModel!!.dataAdapter.value?.getData()?.size?.minus(1)) ,editText.text.toString()))
                            }.await()

                        }
                    }catch (e : FileNotFoundException){
                        Log.d("null", e.message.toString())
                    }
                }else{
                    Toast.makeText(applicationContext,"유튜브 링크 형식에 어긋납니다.", Toast.LENGTH_SHORT).show()
                }

            }
            .setNegativeButton("취소") { dialogInterface, i ->
            }.show()
    }
    override fun onDestroy() {
        startService(Intent(this, BGTubeService::class.java))
        super.onDestroy()
    }
    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // TODO 동의를 얻지 못했을 경우의 처리
            }
        }
    }
}