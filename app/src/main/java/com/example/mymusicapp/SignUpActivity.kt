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

class SignUpActivity : AppCompatActivity() {
    private var isPhoneMode = true // Переменная состояния (телефон/email)
    private lateinit var inputField: TextInputEditText
    private lateinit var inputLayout: TextInputLayout
    private lateinit var toggleButton: MaterialButton
    private lateinit var buttonNextSignUp: Button
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = getSharedPreferences("MyMusicAppPrefs", MODE_PRIVATE)

        if (preferences.getBoolean("isVerified", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_sign_up)

        // Инициализация элементов
        toggleButton = findViewById(R.id.toggleAuthMethod)
        inputField = findViewById(R.id.inputField)
        inputLayout = findViewById(R.id.inputLayout)
        buttonNextSignUp = findViewById(R.id.buttonNextSignUp)

        // Обработка переключения режима
        toggleButton.setOnClickListener {
            isPhoneMode = !isPhoneMode
            updateAuthMethod()
        }

        buttonNextSignUp.setOnClickListener {
            validateAndProceed()
        }
    }

    private fun updateAuthMethod() {
        if (isPhoneMode) {
            toggleButton.text = "Use Email"
            toggleButton.icon = ContextCompat.getDrawable(this, R.drawable.ic_email)
            inputLayout.hint = "Phone Number"
            buttonNextSignUp.text = "Sign up with phone"
            inputField.inputType = InputType.TYPE_CLASS_PHONE
        } else {
            toggleButton.text = "Use Phone Number"
            toggleButton.icon = ContextCompat.getDrawable(this, R.drawable.ic_phone)
            inputLayout.hint = "Email Address"
            buttonNextSignUp.text = "Sign up with email"
            inputField.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        inputField.text?.clear()
    }

    private fun validateAndProceed() {
        val input = inputField.text.toString().trim()

        if (input.isEmpty()) {
            inputField.error = if (isPhoneMode) "Введите номер телефона" else "Введите email"
            return
        }

        if (isPhoneMode && !android.util.Patterns.PHONE.matcher(input).matches()) {
            inputField.error = "Неверный формат номера"
            return
        }

        if (!isPhoneMode && !android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            inputField.error = "Неверный формат email"
            return
        }

        val user = if (isPhoneMode) {
            ExposedMusicUser(phone = input, email = null)
        } else {
            ExposedMusicUser(phone = null, email = input)
        }

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val statusCode = UserService().addUser(user)
                if (statusCode == 201) {
                    val intent = Intent(this@SignUpActivity, SignUp2Activity::class.java)
                    intent.putExtra("authMethod", if (isPhoneMode) "phone" else "email")
                    intent.putExtra("authValue", input)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignUpActivity,
                    "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}