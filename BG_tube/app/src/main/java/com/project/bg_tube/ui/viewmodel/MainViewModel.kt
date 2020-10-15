package com.project.bg_tube.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project.bg_tube.ui.adapters.FragmentAdapter

class MainViewModel : ViewModel() {
    val dataAdapter : MutableLiveData<FragmentAdapter> = MutableLiveData()
}