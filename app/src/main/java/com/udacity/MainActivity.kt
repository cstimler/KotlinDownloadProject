package com.udacity

import android.animation.ObjectAnimator
import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var radioGroup: RadioGroup
   // private lateinit var radioButton: RadioButton
  //  private var broadcastExtraId: Long? = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        radioGroup = findViewById<RadioGroup>(R.id.radio_group)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
           // download()
            val response = findOutWhichRadioButtonIsSelected()
            if (response == "NONE") {
              Toast.makeText(applicationContext, "Please select file to download", Toast.LENGTH_SHORT).show()
            }
            else
            {download(response)
                custom_button.buttonState = ButtonState.Loading}
            // This was helpful: https://knowledge.udacity.com/questions/420421
           // clickMade()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if ( id == downloadID) {
                Log.i("CHARLESS:", "download completed")
                custom_button.buttonState = ButtonState.Completed
            }
        }
    }

    private fun download(URL: String) {
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val glideURL =
            "https://github.com/bumptech/glide/releases/download/v3.6.0/glide-3.6.0.jar"
       // private const val udacityURL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        private const val udacityURL = "https://www.woopsywoopsy.com" // fake url
        private const val retrofitURL = "https://github.com/square/retrofit"
        private const val CHANNEL_ID = "channelId"
    }
/*
    private fun scaleButton() {
        val animator = ObjectAnimator.ofFloat(custom_button, View.SCALE_X, 0.1f)
        animator.duration = 1000
        animator.start()
    }
*/

    fun clickMade() {
       custom_button.animateView()
    }

    fun findOutWhichRadioButtonIsSelected() : String {
        val radioSelected = radioGroup.checkedRadioButtonId
        Log.i("CHARLESradio selected:", R.id.radio_udacity.toString())
        return when (radioSelected) {
            R.id.radio_glide -> glideURL
            R.id.radio_udacity -> udacityURL
            R.id.radio_retrofit -> retrofitURL
            else -> "NONE"
        }

    }
}
