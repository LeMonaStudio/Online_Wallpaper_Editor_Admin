package com.thenativecitizens.onlinewallpapereditoradmin.service


import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class FirebaseMessageReceiver : FirebaseMessagingService(){

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i("TAG", "New Token Generated")
    }
}