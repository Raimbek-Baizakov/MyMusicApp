package com.example.mymusicapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class SignUp2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_2)

        val buttonNext = findViewById<Button>(R.id.buttonNextHome)
        buttonNext.setOnClickListener {
            // Сохраняем статус регистрации
            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("isRegistered", true).apply()

            // Переходим в MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Закрываем текущую Activity
        }

        // Убрали автоматический переход в MainActivity
    }
}