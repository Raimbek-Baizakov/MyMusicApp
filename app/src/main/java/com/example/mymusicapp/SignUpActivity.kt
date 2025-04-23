package com.example.mymusicapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val buttonNext = findViewById<Button>(R.id.buttonNextSignUp)
        buttonNext.setOnClickListener {
            val intent = Intent(this, SignUp2Activity::class.java)
            startActivity(intent)
        }
    }
}
