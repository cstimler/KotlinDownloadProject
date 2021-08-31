package com.udacity

import android.animation.ObjectAnimator
import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    // id for EXTRA_DOWNLOAD_ID
    private var id: Long? = -1
   // private lateinit var radioButton: RadioButton
  //  private var broadcastExtraId: Long? = -1

    private lateinit var downloadManager: DownloadManager
    // flag to shut down looper after the download completed
    private var flag = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        radioGroup = findViewById<RadioGroup>(R.id.radio_group)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
           // download()
            // check for internet connectivity:
            // https://developer.android.com/training/monitoring-device-state/connectivity-status-type

            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

            // if no connection provide warning and return:

            if (!isConnected) {
                Toast.makeText(applicationContext, "No internet connectivity, try again later.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val response = findOutWhichRadioButtonIsSelected()
            if (response == "NONE") {
              Toast.makeText(applicationContext, "Please select file to download", Toast.LENGTH_SHORT).show()
            }
            else
            {download(response)
                custom_button.buttonState = ButtonState.Loading
                Log.i("CHARLESS", "at top of else in click listener")
                // https://stackoverflow.com/questions/65488410/handler-postdelayed-is-now-deprecated-what-function-to-call-instead
                // if there is no completion after 30 seconds, cancel download and ask user to try again:
                // flag logic will effectively cancel a prior loop that had not been canceled -
                // the flag is false by default so that flag==true will be true UNLESS the loop was
                // previously entered but not completed (say the last download happened quickly and this
                // loop is still running when we request the next download).  In that case the flag will still be "true" and the next loop to execute
                // will have flag = false and will not print out, leaving the current loop to complete.
                flag = !flag
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!(downloadID == id) && (flag == true)) {
                            // signal to the custom button that download still not completed
                            custom_button.buttonState = ButtonState.Indeterminate
                            //https://stackoverflow.com/questions/39271027/cancel-download-in-downloadmanager
                            // stop the download, its taking too long for such a small file
                            downloadManager.remove(downloadID)
                            downloadID = -10
                            Log.i("CHARLESS", "in Main Activity Handler")
                            flag = false
                        }
                        else {flag = !flag}
                    }, 30000)
                }
            }
            // This was helpful: https://knowledge.udacity.com/questions/420421
           // clickMade()
        }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
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

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
      //  https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip


        private const val glideURL =
            "https://github.com/bumptech/glide/archive/master.zip"
       // private const val udacityURL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val udacityURL = "https://www.fakeurlblahblah.com"
        private const val retrofitURL = "https://github.com/square/retrofit/archive/master.zip"
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
