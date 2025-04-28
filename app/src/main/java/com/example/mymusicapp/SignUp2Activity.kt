package com.example.mymusicapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.textview.MaterialTextView

class SignUp2Activity : AppCompatActivity() {

    private lateinit var code1: EditText
    private lateinit var code2: EditText
    private lateinit var code3: EditText
    private lateinit var code4: EditText
    private lateinit var verifyButton: Button
    private lateinit var errorText: TextView
    private lateinit var resendSmsButton: MaterialTextView

    // Установите конкретный код
    private val generatedCode = "1234"

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем, подтвержден ли код
        val sharedPreferences = getSharedPreferences("MyMusicAppPrefs", MODE_PRIVATE)
        val isVerified = sharedPreferences.getBoolean("isVerified", false)

        if (isVerified) {
            // Если код подтвержден, открываем MainActivity и завершаем текущую активность
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_sign_up_2)

        // Инициализация элементов
        code1 = findViewById(R.id.code1)
        code2 = findViewById(R.id.code2)
        code3 = findViewById(R.id.code3)
        code4 = findViewById(R.id.code4)
        verifyButton = findViewById(R.id.verifyButton)
        errorText = findViewById(R.id.errorText)
        resendSmsButton = findViewById(R.id.resendSmsButton)

        // Настройка кнопки "Resend SMS"
        setupResendSmsButton()

        setupCodeInputFields()

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

                // Сохранение статуса подтверждения
                sharedPreferences.edit().putBoolean("isVerified", true).apply()

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
    private fun setupCodeInputFields() {
        val codeFields = arrayOf(code1, code2, code3, code4)

        // Обработка автоматического перехода
        for (i in 0 until codeFields.size - 1) {
            codeFields[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        codeFields[i + 1].requestFocus()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        // Обработка удаления (переход на предыдущее поле)
        for (i in 1 until codeFields.size) {
            codeFields[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    codeFields[i].text.isEmpty()) {

                    codeFields[i - 1].requestFocus()
                    codeFields[i - 1].text.clear()
                    true
                } else {
                    false
                }
            }
        }
    }
    private fun setupResendSmsButton() {

        val fullText = "Resend SMS"
        val spannable = SpannableString(fullText)

        // Делаем "Resend SMS" кликабельным
        val clickableSpan = object : ClickableSpan() {
            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            override fun onClick(widget: View) {
                // Действие при клике на "Resend SMS"
                Toast.makeText(this@SignUp2Activity, "SMS resent", Toast.LENGTH_SHORT).show()
                sendNotification(generatedCode)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = getColor(R.color.purple_500) // Цвет из ресурсов
                ds.isUnderlineText = false // Убираем подчеркивание
            }
        }

        // Применяем стиль к части текста
        spannable.setSpan(
            clickableSpan,
            fullText.indexOf("Resend SMS"),
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        resendSmsButton.text = spannable
        resendSmsButton.movementMethod = LinkMovementMethod.getInstance()
    }

    // Функция для отправки уведомления
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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