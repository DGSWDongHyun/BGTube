package com.project.bg_tube.ui.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.project.bg_tube.R
import com.project.bg_tube.data.request.Playlist
import com.project.bg_tube.databinding.FragmentFirstBinding
import com.project.bg_tube.ui.adapters.FragmentAdapter
import com.project.bg_tube.ui.adapters.listener.OnItemClickListener
import com.project.bg_tube.ui.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.bg_tube_service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.reflect.Type


class PlayListFragment : Fragment() {

    var homeFragmentBinding : FragmentFirstBinding ?= null
    var adapterView : FragmentAdapter ?= null
    var mainViewModel : MainViewModel ?= null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeFragmentBinding = FragmentFirstBinding.inflate(inflater, container, false)

        return homeFragmentBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkList(view)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        adapterView = FragmentAdapter(requireContext(),  object : OnItemClickListener {
            override fun OnItemClick(position: Int) {

            }
        })


        GlobalScope.launch(Dispatchers.Main) {
            homeFragmentBinding!!.recyclerPlayList.layoutManager = LinearLayoutManager(requireContext())
            getListData()?.let { adapterView!!.setData(it) }
            mainViewModel!!.dataAdapter.value = adapterView
            homeFragmentBinding!!.recyclerPlayList.adapter = adapterView
        }




    }
    private fun getListData(): ArrayList<Playlist>? {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val gson = Gson()
        val json = sharedPrefs.getString("Playlist", "")
        val type: Type = object : TypeToken<ArrayList<Playlist?>?>() {}.type
        return gson.fromJson(json, type)
    }
    private fun checkList(view: View){
        val nothingLayout : LinearLayout = view?.findViewById(R.id.nothingLayout)
        if(getListData() != null){
            nothingLayout.visibility = View.INVISIBLE
        }else{
            nothingLayout.visibility = View.VISIBLE
        }
    }
}