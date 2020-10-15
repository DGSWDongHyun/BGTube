package com.project.bg_tube.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.bg_tube.databinding.FragmentFirstBinding
import com.project.bg_tube.ui.adapters.FragmentAdapter
import com.project.bg_tube.ui.adapters.listener.OnItemClickListener
import com.project.bg_tube.ui.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.bg_tube_service.*


class FirstFragment : Fragment() {

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

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        adapterView = FragmentAdapter(requireContext(),  object : OnItemClickListener {
            override fun OnItemClick(position: Int) {

            }
        })

        homeFragmentBinding!!.recyclerPlayList.layoutManager = LinearLayoutManager(requireContext())
        mainViewModel!!.dataAdapter.value = adapterView
        homeFragmentBinding!!.recyclerPlayList.adapter = adapterView




    }
}