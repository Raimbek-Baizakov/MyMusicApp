package com.example.mymusicapp

import ApiService
import UpdateVerificationRequest
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*

class SignUp2Activity : AppCompatActivity() {

    private lateinit var code: String
    private lateinit var preferences: SharedPreferences
    private lateinit var apiService: ApiService
    private var userId: Int = 0 // Инициализируем переменную для userId

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_2)

        // Получаем SharedPreferences для сохранения состояния
        preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Инициализация Retrofit
        apiService = RetrofitClient.instance // Получаем объект ApiService через RetrofitClient

        // Получаем userId из SharedPreferences или другого источника
        userId = preferences.getInt("user_id", 0)

        // Генерируем случайный 4-значный код
        code = generateRandomCode()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default"
            val channelName = "Default Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        // Отправляем уведомление с кодом
        sendNotification(code)

        val code1 = findViewById<EditText>(R.id.code1)
        val code2 = findViewById<EditText>(R.id.code2)
        val code3 = findViewById<EditText>(R.id.code3)
        val code4 = findViewById<EditText>(R.id.code4)
        val verifyButton = findViewById<Button>(R.id.verifyButton)
        val errorText = findViewById<TextView>(R.id.errorText)

        verifyButton.setOnClickListener {
            val enteredCode = "${code1.text}${code2.text}${code3.text}${code4.text}"

            if (enteredCode == code) {
                // Код верный, устанавливаем флаг is_verified на сервере
                lifecycleScope.launch {
                    try {
                        val updateRequest = UpdateVerificationRequest(userId, true)  // Параметры: userId и is_verified = true
                        val response = apiService.updateVerificationStatus(updateRequest)
                        if (response.isSuccessful) {
                            // Переход в MainActivity
                            val editor = preferences.edit()
                            editor.putBoolean("is_verified", true)
                            editor.apply()

                            val intent = Intent(this@SignUp2Activity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@SignUp2Activity, "Ошибка при обновлении данных на сервере", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@SignUp2Activity, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Код неверный, показываем ошибку и отправляем новый код
                errorText.text = "Неверный код, попробуйте снова."
                errorText.visibility = TextView.VISIBLE

                // Генерация нового кода и отправка уведомления
                code = generateRandomCode()
                sendNotification(code)
            }
        }
    }

    // Функция для генерации случайного 4-значного кода
    private fun generateRandomCode(): String {
        val random = Random()
        return String.format("%04d", random.nextInt(10000))
    }

    // Функция для отправки уведомления
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendNotification(code: String) {
        val builder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Ваш код подтверждения")
            .setContentText("Ваш код: $code")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, builder.build())
    }
}
