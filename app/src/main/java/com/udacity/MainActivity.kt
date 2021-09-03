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
//fileDownloaded is global variable that holds filename to be passed to detail view:
private var fileDownloaded = " "

class MainActivity  : AppCompatActivity() {

    private var downloadID: Long = 0
    private lateinit var radioGroup: RadioGroup
    private lateinit var downloadManager: DownloadManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        supportActionBar?.setTitle("Android Download App - Udacity Project 3")
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

            // if no internet connection provide warning and return:

            if (!isConnected) {
                Toast.makeText(
                    applicationContext,
                    "No internet connectivity, try again later.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val response = findOutWhichRadioButtonIsSelected()
            // if no file selected, alert user:
            if (response == "NONE") {
                Toast.makeText(
                    applicationContext,
                    "Please select a file to download",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                download(response)
                // ButtonState.Loading signals to custom button its current state:
                custom_button.buttonState = ButtonState.Loading
               // CountDownTimer allows 30 seconds for download which should be enough for files
                // of this size.  If it needs more time then it is considered a "fail".  I
                // discussed this approach with a mentor @ https://knowledge.udacity.com/questions/681012
                object : CountDownTimer(30000, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        if (custom_button.buttonState == ButtonState.Completed) {
                            // prevent timer from reaching onFinish() if the download completed
                            cancel()
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
                        // send "fail" notification to detail activity:
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
            // if successfully downloaded a file:
            if (id == downloadID) {
                // let custon button know to show downloading is completed:
                custom_button.buttonState = ButtonState.Completed
                val notificationManager = ContextCompat.getSystemService(
                    context!!,
                    NotificationManager::class.java
                ) as NotificationManager
                // send "success" notification to detail activity:
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
        // by changing the below urls, it is possible to see the response of the app
        // when it cannot download a file:
        private const val glideURL = "https://github.com/bumptech/glide/archive/master.zip"
        private const val udacityURL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val retrofitURL = "https://github.com/square/retrofit/archive/master.zip"
    }

    fun findOutWhichRadioButtonIsSelected(): String {
        val radioSelected = radioGroup.checkedRadioButtonId
        Log.i("CHARLESradio selected:", R.id.radio_udacity.toString())
        // load the name of the file into "fileDownloaded" so this can be passed to
        // the detail activity:
            fileDownloaded = when (radioSelected) {
                R.id.radio_glide -> getString(R.string.glide_file)
                R.id.radio_udacity -> getString(R.string.udacity_file)
                R.id.radio_retrofit -> getString(R.string.retrofit_file)
                else -> "Unknown File"
            }
        // return the proper url so that it can be downloaded:
            return when (radioSelected) {
                R.id.radio_glide -> glideURL
                R.id.radio_udacity -> udacityURL
                R.id.radio_retrofit -> retrofitURL
                else -> "NONE"
            }
    }
// must create a channel if Android version is "O" or later:
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

    // Boilerplate to set up notification to detail activity:
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
            .addAction(
                R.drawable.ic_launcher_foreground,
                applicationContext.getString(R.string.notification_button),
                detailPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
             notify(NOTIFICATION_ID, builder.build())
    }
}

