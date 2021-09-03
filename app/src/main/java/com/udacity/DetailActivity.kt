package com.udacity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import org.w3c.dom.Text

lateinit var intent: Intent



class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        // getIntent() retrieves the intent that opened this view:
        intent = getIntent()
        val fileName = findViewById<TextView>(R.id.file_name)
        val statusType = findViewById<TextView>(R.id.status)
        // obtain the passed information:
        val thisFile = intent.getStringExtra("file")
        val thisStatus = intent.getStringExtra("status")
        // place the passed information in the proper locations in the layout file:
        fileName.text = thisFile
        statusType.text = thisStatus
        if (thisStatus == "Fail") {
           // statusType.setTextColor(resources.getColor(R.color.colorAccent))
               // resources.getColor is depreciated, should use this:
                   //https://stackoverflow.com/questions/45401813/how-to-use-new-version-of-getresources-getcolor
            statusType.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
        }
        supportActionBar?.setTitle("Download Results")
    }
// This is called by the OK button's "onClick" method in activity_detail.xml
    fun openMainActivity(view: View) {
        val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)
        startActivity(mainActivityIntent)
    }
}

