package com.rupamsaini.interviewprep

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class InterviewPrepApp : Application(){
    override fun onCreate() {
        super.onCreate()// Manually initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
