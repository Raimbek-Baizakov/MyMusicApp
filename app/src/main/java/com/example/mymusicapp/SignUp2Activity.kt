package com.example.mymusicapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mymusicapp.R

class SignUp2Activity : AppCompatActivity() {

    private lateinit var code1: EditText
    private lateinit var code2: EditText
    private lateinit var code3: EditText
    private lateinit var code4: EditText
    private lateinit var verifyButton: Button
    private lateinit var errorText: TextView

    // Установите конкретный код
    private val generatedCode = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_2)

        // Инициализация элементов
        code1 = findViewById(R.id.code1)
        code2 = findViewById(R.id.code2)
        code3 = findViewById(R.id.code3)
        code4 = findViewById(R.id.code4)
        verifyButton = findViewById(R.id.verifyButton)
        errorText = findViewById(R.id.errorText)

        // Отправка уведомления с кодом
        sendNotification(generatedCode)

        // Обработчик для кнопки "Подтвердить"
        verifyButton.setOnClickListener {
            val enteredCode = code1.text.toString() + code2.text.toString() + code3.text.toString() + code4.text.toString()

            // Проверка на пустоту
            if (enteredCode.isEmpty()) {
                errorText.text = "Пожалуйста, введите код"
                errorText.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            // Проверка на совпадение с кодом
            if (enteredCode == generatedCode) {
                Toast.makeText(this, "Код подтвержден!", Toast.LENGTH_SHORT).show()

                // Переход в MainActivity при правильном коде
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()  // Закрываем текущую активность
            } else {
                errorText.text = "Неверный код"
                errorText.visibility = TextView.VISIBLE
            }
        }
    }

    // Функция для отправки уведомления
    private fun sendNotification(code: String) {
        // Создание канала уведомлений для Android 8.0 и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default", // Идентификатор канала
                "Default Notifications", // Название канала
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Ваш код подтверждения")
            .setContentText("Ваш код: $code")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, builder.build())
    }
}
