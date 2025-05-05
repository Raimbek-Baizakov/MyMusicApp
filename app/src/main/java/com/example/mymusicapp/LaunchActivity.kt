package com.example.mymusicapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация SharedPreferences
        val sharedPreferences = getSharedPreferences("MyMusicAppPrefs", MODE_PRIVATE)

        // Сброс состояния верификации (временная мера)
        sharedPreferences.edit().apply {
            putBoolean("isVerified", false) // В реальном приложении не должно быть постоянного сброса
            apply()
        }

        // Проверка состояния верификации
        val isVerified = sharedPreferences.getBoolean("isVerified", false)
        val nextActivity = if (isVerified) {
            MainActivity::class.java
        } else {
            SignUpActivity::class.java
        }

        // Запуск соответствующей активности
        startActivity(Intent(this, nextActivity))
        finish()
    }
}
