package com.example.mymusicapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers


class SignUpActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioPhone: RadioButton
    private lateinit var radioEmail: RadioButton
    private lateinit var inputField: TextInputEditText
    private lateinit var buttonNextSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Инициализация элементов
        radioGroup = findViewById(R.id.radioGroup)
        radioPhone = findViewById(R.id.radioPhone)
        radioEmail = findViewById(R.id.radioEmail)
        inputField = findViewById(R.id.inputField)
        buttonNextSignUp = findViewById(R.id.buttonNextSignUp)

        // Устанавливаем обработчик для кнопки перехода
        buttonNextSignUp.setOnClickListener {
            val input = inputField.text.toString().trim()

            // Проверка, что введено что-то
            if (input.isEmpty()) {
                inputField.error = "Поле не может быть пустым"
                return@setOnClickListener
            }

            // Проверка, какой тип данных выбрал пользователь
            val isPhoneSelected = radioPhone.isChecked
            val isEmailSelected = radioEmail.isChecked

            if (!isPhoneSelected && !isEmailSelected) {
                showErrorMessage("Выберите способ регистрации")
                return@setOnClickListener
            }

            val user = if (isPhoneSelected) {
                ExposedMusicUser(phone = input, email = null)
            } else {
                ExposedMusicUser(phone = null, email = input)
            }

            // Асинхронный вызов для отправки данных на сервер
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val statusCode = UserService().addUser(user)
                    // Обработка успешной отправки
                    if (statusCode == 201) {
                        // Переход на второй экран с успешной регистрацией
                        val intent = Intent(this@SignUpActivity, SignUp2Activity::class.java)
                        intent.putExtra("type", if (isPhoneSelected) "phone" else "email")
                        intent.putExtra("value", input)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    // Обработка ошибок
                    showErrorMessage("Ошибка при регистрации: ${e.message}")
                }
            }
        }
    }

    // Функция для отображения сообщения об ошибке
    private fun showErrorMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
