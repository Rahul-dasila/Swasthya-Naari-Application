package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.VideoView

class splashscreen : AppCompatActivity() {
    private lateinit var vv : VideoView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)
        vv = findViewById(R.id.videoView)
        val txt = findViewById<TextView>(R.id.textView)
        txt.setBackgroundColor(Color.WHITE)
        vv.setBackgroundColor(Color.TRANSPARENT)

        val uri = Uri.parse("android.resource://"+packageName+"/"+R.raw.splash)
        vv.setVideoURI(uri)
        vv.setOnCompletionListener {
         startActivity(Intent(this,loginscreen::class.java))
            finish()
        }
        vv.start()
        val hand = Handler(Looper.getMainLooper())
        val toRun = Runnable {
            txt.setBackgroundColor(Color.TRANSPARENT)
        }
        hand.postDelayed(toRun,400)

    }
}