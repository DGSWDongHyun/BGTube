package com.project.bg_tube.ui.activites

import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.project.bg_tube.R
import com.project.bg_tube.data.request.Playlist
import com.project.bg_tube.ui.adapters.FragmentAdapter
import com.project.bg_tube.ui.services.BGTubeService
import com.project.bg_tube.ui.viewmodel.MainViewModel
import org.apache.commons.io.IOUtils
import java.io.FileNotFoundException
import java.lang.reflect.Type
import java.net.URL
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private var mainViewModel : MainViewModel ?= null
    private val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE : Int = 1
    private var fragmentAdapter : FragmentAdapter ?= null
    private var list : ArrayList<Playlist> ?= arrayListOf()
    private val string_regex : String = "[https:]+\\:+\\/+[www]+\\.+[youtube]+\\.+[com]+\\/+[ watch ]+\\?+[v]+\\=+[a-z A-Z 0-9 _ \\- ? !]+"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        fragmentAdapter = mainViewModel?.dataAdapter?.value


        mainViewModel?.dataAdapter?.value = fragmentAdapter


        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            if(mainViewModel?.dataAdapter?.value == null){
                Log.d("null", "null")
            }else{
                fragmentAdapter = mainViewModel?.dataAdapter?.value
                Log.d("added", "added")
                makeAlert()

                list = mainViewModel?.dataAdapter?.value!!.getData()


            }
        }
    }
    private fun isServiceRunningCheck(): Boolean {
        val manager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if ("com.project.bg_tube.ui.services.BGTubeService" == service.service.className) {
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
                        list?.add(Playlist(editText.text.toString()))
                        mainViewModel?.dataAdapter?.value!!.setData(list!!)
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