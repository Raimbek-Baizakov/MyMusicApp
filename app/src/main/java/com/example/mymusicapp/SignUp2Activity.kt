package com.example.mymusicapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Активность для подтверждения кода верификации (SMS/email)
class SignUp2Activity : AppCompatActivity() {

    // Поля для ввода 4-значного кода
    private lateinit var code1: EditText
    private lateinit var code2: EditText
    private lateinit var code3: EditText
    private lateinit var code4: EditText

    // Кнопка подтверждения
    private lateinit var verifyButton: Button

    // Текст для отображения ошибок
    private lateinit var errorText: TextView

    // Кнопка повторной отправки SMS
    private lateinit var resendSmsButton: MaterialTextView

    // Фиксированный код для демонстрации (в реальном приложении должен генерироваться сервером)
    private val generatedCode = "1234"

    // Основной метод создания активности
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверка, не верифицирован ли уже пользователь
        val sharedPreferences = getSharedPreferences("MyMusicAppPrefs", MODE_PRIVATE)
        val isVerified = sharedPreferences.getBoolean("isVerified", false)

        if (isVerified) {
            // Если верифицирован - переход в MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_sign_up_2)

        // Инициализация полей ввода
        code1 = findViewById(R.id.code1)
        code2 = findViewById(R.id.code2)
        code3 = findViewById(R.id.code3)
        code4 = findViewById(R.id.code4)

        // Инициализация кнопок и текста
        verifyButton = findViewById(R.id.verifyButton)
        errorText = findViewById(R.id.errorText)
        resendSmsButton = findViewById(R.id.resendSmsButton)

        // Настройка кликабельной кнопки "Resend SMS"
        setupResendSmsButton()

        // Настройка автоматического перехода между полями ввода
        setupCodeInputFields()

        // Отправка тестового уведомления с кодом
        sendNotification(generatedCode)

        // Проверка включены ли уведомления (для отладки)
        val areNotificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
        Toast.makeText(this, "Уведомления ${if (areNotificationsEnabled) "включены" else "выключены"}", Toast.LENGTH_SHORT).show()

        // Обработчик кнопки подтверждения
        verifyButton.setOnClickListener {
            // Сборка введенного кода из 4 полей
            val enteredCode = code1.text.toString() + code2.text.toString() +
                    code3.text.toString() + code4.text.toString()

            // Проверка на пустой код
            if (enteredCode.isEmpty()) {
                errorText.text = "Пожалуйста, введите код"
                errorText.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            // Проверка совпадения кодов
            if (enteredCode == generatedCode) {
                // Получение метода авторизации (телефон/email)
                val authMethod = intent.getStringExtra("authMethod")
                val authValue = intent.getStringExtra("authValue")

                // Создание объекта пользователя
                val user = if (authMethod == "phone") {
                    ExposedMusicUser(phone = authValue, email = null)
                } else {
                    ExposedMusicUser(phone = null, email = authValue)
                }

                // Запуск корутины для работы с сетью
                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        val userService = UserService()

                        // Поиск существующего пользователя
                        val existingUser = userService.findUser(phone = user.phone, email = user.email)

                        if (existingUser != null) {
                            // Если пользователь найден - вход
                            sharedPreferences.edit().apply {
                                putBoolean("isVerified", true)
                                putInt("userId", existingUser.id)
                                putString("userRole", existingUser.role)
                                apply()
                            }

                            Toast.makeText(
                                this@SignUp2Activity,
                                "Добро пожаловать!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Если пользователь не найден - регистрация
                            val newUser = userService.addUser(user)
                            sharedPreferences.edit().apply {
                                putBoolean("isVerified", true)
                                putInt("userId", newUser.id)
                                putString("userRole", newUser.role)
                                apply()
                            }

                            Toast.makeText(
                                this@SignUp2Activity,
                                "Регистрация прошла успешно!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        // Переход в главную активность
                        val intent = Intent(this@SignUp2Activity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        // Обработка ошибок сети/сервера
                        Toast.makeText(
                            this@SignUp2Activity,
                            "Ошибка: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                // Неверный код
                errorText.text = "Неверный код подтверждения"
                errorText.visibility = TextView.VISIBLE
            }
        }
    }

    // Настройка автоматического перехода между полями ввода кода
    private fun setupCodeInputFields() {
        val codeFields = arrayOf(code1, code2, code3, code4)

        // Автопереход при вводе символа
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

        // Обработка удаления символа (возврат к предыдущему полю)
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

    // Настройка кликабельной кнопки "Resend SMS"
    private fun setupResendSmsButton() {
        val fullText = "Resend SMS"
        val spannable = SpannableString(fullText)

        // Создание кликабельного текста
        val clickableSpan = object : ClickableSpan() {
            @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
            override fun onClick(widget: View) {
                // Действие при клике - повторная отправка кода
                Toast.makeText(this@SignUp2Activity, "SMS resent", Toast.LENGTH_SHORT).show()
                sendNotification(generatedCode)
            }

            // Стиль кликабельного текста
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = getColor(R.color.purple_500)
                ds.isUnderlineText = false
            }
        }

        // Применение стиля к тексту
        spannable.setSpan(
            clickableSpan,
            fullText.indexOf("Resend SMS"),
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        resendSmsButton.text = spannable
        resendSmsButton.movementMethod = LinkMovementMethod.getInstance()
    }

    // Отправка уведомления с кодом подтверждения
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendNotification(code: String) {
        // Создание канала уведомлений (для Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                "Default Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Построение уведомления
        val builder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Ваш код подтверждения")
            .setContentText("Ваш код: $code")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Отправка уведомления
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, builder.build())
    }
}