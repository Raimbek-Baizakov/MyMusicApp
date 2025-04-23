package com.example.mymusicapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val buttonNextTrial = findViewById<Button>(R.id.buttonNextTrial)
        buttonNextTrial.setOnClickListener {
            val intent = Intent(this, TrialActivity::class.java)
            startActivity(intent)
        }
    }
}
