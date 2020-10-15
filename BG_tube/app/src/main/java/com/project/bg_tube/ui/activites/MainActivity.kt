package com.project.bg_tube.ui.activites

import android.annotation.TargetApi
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.project.bg_tube.R
import com.project.bg_tube.data.request.Playlist
import com.project.bg_tube.ui.adapters.FragmentAdapter
import com.project.bg_tube.ui.services.BGTubeService
import com.project.bg_tube.ui.viewmodel.MainViewModel


class MainActivity : AppCompatActivity() {

    private var mainViewModel : MainViewModel ?= null
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE : Int = 1
    private var fragmentAdapter : FragmentAdapter ?= null
    var list : ArrayList<Playlist> ?= arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)


        fragmentAdapter = mainViewModel?.dataAdapter?.value

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            if(mainViewModel?.dataAdapter?.value == null){
                Log.d("null", "null")
            }else{
                fragmentAdapter = mainViewModel?.dataAdapter?.value
                Log.d("added", "added")
                makeAlert()


            }
        }
    }

    fun makeAlert() {
        val editText: EditText = EditText(this)
        editText.hint = "유튜브 링크를 입력하세요."

        val alertDialog: AlertDialog? = AlertDialog.Builder(this)
            .setTitle("유튜브 링크를 입력하세요.")
            .setView(editText)
            .setPositiveButton("확인") { dialogInterface, i ->
                list?.add(Playlist(editText.text.toString()))
                fragmentAdapter?.setData(list!!)

            }
            .setNegativeButton("취소") { dialogInterface, i ->
            }.show()
    }

    override fun onDestroy() {
        saveListData(mainViewModel?.dataAdapter?.value!!.getData())
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
    private fun saveListData(playlist: ArrayList<Playlist>) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor: SharedPreferences.Editor = preferences.edit()
        val gson = Gson()
        val json = gson.toJson(playlist)
        editor.putString("Playlist", json)
        editor.commit()
    }
}