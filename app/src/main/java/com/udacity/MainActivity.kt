package com.udacity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import com.udacity.Utils.NotificationUtils.*

private const val CHANNEL_ID = "download_channel"
private const val CHANNEL_NAME = "download_result"
private var fileDownloaded = " "

class MainActivity  : AppCompatActivity() {

    private var downloadID: Long = 0
/*
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
*/
    private lateinit var radioGroup: RadioGroup

    // id for EXTRA_DOWNLOAD_ID
 //   private var id: Long? = -1
    // private lateinit var radioButton: RadioButton
    //  private var broadcastExtraId: Long? = -1

    private lateinit var downloadManager: DownloadManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        radioGroup = findViewById<RadioGroup>(R.id.radio_group)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // get notification channel created early
        createChannel(CHANNEL_ID, CHANNEL_NAME)

        custom_button.setOnClickListener {
            // download()
            // check for internet connectivity:
            // https://developer.android.com/training/monitoring-device-state/connectivity-status-type

            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

            // if no connection provide warning and return:

            if (!isConnected) {
                Toast.makeText(
                    applicationContext,
                    "No internet connectivity, try again later.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val response = findOutWhichRadioButtonIsSelected()
            if (response == "NONE") {
                Toast.makeText(
                    applicationContext,
                    "Please select a file to download",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                download(response)
                custom_button.buttonState = ButtonState.Loading
                Log.i("CHARLESS", "at top of else in click listener")
                // https://stackoverflow.com/questions/65488410/handler-postdelayed-is-now-deprecated-what-function-to-call-instead
                // if there is no completion after 30 seconds, cancel download and ask user to try again:
                // flag logic will effectively cancel a prior loop that had not been canceled -
                // the flag is false by default so that flag==true will be true UNLESS the loop was
                // previously entered but not completed (say the last download happened quickly and this
                // loop is still running when we request the next download).  In that case the flag will still be "true" and the next loop to execute
                // will have flag = false and will not print out, leaving the current loop to complete.
                object : CountDownTimer(30000, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        if (custom_button.buttonState == ButtonState.Completed) {
                            // prevent timer from reaching onFinish() if the download completed
                            cancel()
                            Log.i("CHARLESS", "countdowntimer has been cancelled")
                        }
                    }

                    override fun onFinish() {
                        custom_button.buttonState = ButtonState.Indeterminate
                        //https://stackoverflow.com/questions/39271027/cancel-download-in-downloadmanager
                        // stop the download, its taking too long for such a small file
                        downloadManager.remove(downloadID)
                        downloadID = -10
                        val notificationManager = ContextCompat.getSystemService(
                            applicationContext,
                            NotificationManager::class.java
                        ) as NotificationManager
                        notificationManager.sendNotification(applicationContext, fileDownloaded, "Fail")

                    }
                }.start()
            }
        }
        // This was helpful: https://knowledge.udacity.com/questions/420421
        // clickMade()
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) {
                Log.i("CHARLESS:", "download completed")
                custom_button.buttonState = ButtonState.Completed
                val notificationManager = ContextCompat.getSystemService(
                    context!!,
                    NotificationManager::class.java
                ) as NotificationManager
                notificationManager.sendNotification(applicationContext, fileDownloaded, "Success")

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


        private const val glideURL = "https://github.com/bumptech/glide/archive/master.zip"
        private const val udacityURL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val retrofitURL = "https://github.com/square/retrofit/archive/master.zip"
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

    fun findOutWhichRadioButtonIsSelected(): String {
        val radioSelected = radioGroup.checkedRadioButtonId
        Log.i("CHARLESradio selected:", R.id.radio_udacity.toString())
            fileDownloaded = when (radioSelected) {
                R.id.radio_glide -> getString(R.string.glide_file)
                R.id.radio_udacity -> getString(R.string.udacity_file)
                R.id.radio_retrofit -> getString(R.string.retrofit_file)
                else -> "Unknown File"
            }
            return when (radioSelected) {
                R.id.radio_glide -> glideURL
                R.id.radio_udacity -> udacityURL
                R.id.radio_retrofit -> retrofitURL
                else -> "NONE"
            }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(true)
                }
            notificationChannel.description = "downloaded status"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private val NOTIFICATION_ID = 0
    private val REQUEST_CODE = 0
   // private val FLAGS = 0

    @SuppressLint("WrongConstant")
    fun NotificationManager.sendNotification(applicationContext: Context, fileExtra: String, statusExtra: String) {
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val detailIntent = Intent(applicationContext, DetailActivity::class.java)
        detailIntent.putExtra("file", fileExtra)
        detailIntent.putExtra("status", statusExtra)
        val detailPendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE,
            detailIntent,
            Intent.FLAG_ACTIVITY_NEW_TASK)
        val builder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.download_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext
                .getString(R.string.notification_title))
            .setContentText("The Project 3 Respository is Downloaded")
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
          //  .setChannelId(getString(R.string.download_notification_channel_id))
            .addAction(
                R.drawable.ic_launcher_foreground,
                applicationContext.getString(R.string.notification_button),
                detailPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
             notify(NOTIFICATION_ID, builder.build())
        Log.i("CHARLESX", "completed sendNotification")
    }
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}

