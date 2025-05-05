package com.example.mymusicapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Активность для регистрации/авторизации пользователя.
 * Поддерживает два режима:
 * 1. Регистрация по номеру телефона
 * 2. Регистрация по email
 */
class SignUpActivity : AppCompatActivity() {
    // Флаг текущего режима (телефон/email)
    private var isPhoneMode = true

    // UI элементы
    private lateinit var inputField: TextInputEditText // Поле ввода телефона/email
    private lateinit var inputLayout: TextInputLayout // Контейнер для поля ввода
    private lateinit var toggleButton: MaterialButton // Кнопка переключения режима
    private lateinit var buttonNextSignUp: Button // Кнопка продолжения
    private lateinit var preferences: SharedPreferences // Хранилище настроек

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация SharedPreferences
        preferences = getSharedPreferences("MyMusicAppPrefs", MODE_PRIVATE)

        // Проверка, не авторизован ли уже пользователь
        if (preferences.getBoolean("isVerified", false)) {
            // Если да - переход в MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_sign_up)

        // Привязка UI элементов
        toggleButton = findViewById(R.id.toggleAuthMethod)
        inputField = findViewById(R.id.inputField)
        inputLayout = findViewById(R.id.inputLayout)
        buttonNextSignUp = findViewById(R.id.buttonNextSignUp)

        // Обработчик кнопки переключения режима (телефон/email)
        toggleButton.setOnClickListener {
            isPhoneMode = !isPhoneMode // Инвертируем текущий режим
            updateAuthMethod() // Обновляем UI
        }

        // Обработчик кнопки "Далее"
        buttonNextSignUp.setOnClickListener {
            validateAndProceed() // Валидация и переход
        }
    }

    /**
     * Обновляет UI в зависимости от выбранного режима (телефон/email)
     */
    private fun updateAuthMethod() {
        if (isPhoneMode) {
            // Настройки для режима телефона
            toggleButton.text = "Use Email"
            toggleButton.icon = ContextCompat.getDrawable(this, R.drawable.ic_email)
            inputLayout.hint = "Phone Number"
            buttonNextSignUp.text = "Sign up with phone"
            inputField.inputType = InputType.TYPE_CLASS_PHONE
        } else {
            // Настройки для режима email
            toggleButton.text = "Use Phone Number"
            toggleButton.icon = ContextCompat.getDrawable(this, R.drawable.ic_phone)
            inputLayout.hint = "Email Address"
            buttonNextSignUp.text = "Sign up with email"
            inputField.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        inputField.text?.clear() // Очищаем поле ввода
    }

    /**
     * Проверяет введенные данные и переходит к следующему экрану
     */
    private fun validateAndProceed() {
        val input = inputField.text.toString().trim()

        // Проверка на пустое поле
        if (input.isEmpty()) {
            inputField.error = if (isPhoneMode) "Введите номер телефона" else "Введите email"
            return
        }

        // Валидация номера телефона
        if (isPhoneMode && !android.util.Patterns.PHONE.matcher(input).matches()) {
            inputField.error = "Неверный формат номера"
            return
        }

        // Валидация email
        if (!isPhoneMode && !android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            inputField.error = "Неверный формат email"
            return
        }

        // Создание Intent для перехода к подтверждению кода
        val intent = Intent(this, SignUp2Activity::class.java)
        // Передача метода авторизации (телефон/email)
        intent.putExtra("authMethod", if (isPhoneMode) "phone" else "email")
        // Передача введенного значения (номер/email)
        intent.putExtra("authValue", input)
        startActivity(intent)
    }
}