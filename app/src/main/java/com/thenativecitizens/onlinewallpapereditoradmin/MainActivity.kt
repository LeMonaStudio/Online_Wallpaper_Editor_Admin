package com.thenativecitizens.onlinewallpapereditoradmin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //SubScribe to FCM Topic
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/wallpapers")
    }
}