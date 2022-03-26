package com.thenativecitizens.onlinewallpapereditoradmin.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*



class Delay : ViewModel(){

    //CoroutineScope and Job
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _isDelayFinished = MutableLiveData<Boolean>()
    val isDelayFinished: LiveData<Boolean> get() = _isDelayFinished

    init {
        _isDelayFinished.value = false
        beginDelay()
    }

    private fun beginDelay() {
        uiScope.launch {
            delay(3_000L)
            _isDelayFinished.value = true
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}