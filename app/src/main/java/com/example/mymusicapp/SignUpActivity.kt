package com.example.mymusicapp

import ApiService
import User
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Инициализация Retrofit
        apiService = RetrofitClient.instance

        val buttonNextSignUp = findViewById<Button>(R.id.buttonNextSignUp)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val inputField = findViewById<TextInputEditText>(R.id.inputField)

        buttonNextSignUp.setOnClickListener {
            val selectedOptionId = radioGroup.checkedRadioButtonId
            val inputText = inputField.text.toString()

            if (inputText.isNotEmpty()) {
                // Проверка email на наличие "@" и точки
                if (selectedOptionId == R.id.radioEmail && !isValidEmail(inputText)) {
                    Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val user = if (selectedOptionId == R.id.radioPhone) {
                    User(id = 0, phone = inputText, email = null)
                } else {
                    User(id = 0, phone = null, email = inputText)
                }

                // Вызов Retrofit для добавления пользователя
                lifecycleScope.launch {
                    try {
                        // Проверка существования пользователя по номеру или почте
                        val existsResponse = apiService.checkUserExists(user.phone, user.email)
                        if (existsResponse.isSuccessful) {
                            val existsList = existsResponse.body()
                            if (!existsList.isNullOrEmpty() && existsList[0].exists) {
                                Toast.makeText(this@SignUpActivity, "User already exists", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                        } else {
                            Toast.makeText(this@SignUpActivity, "Error checking user existence", Toast.LENGTH_SHORT).show()
                        }


                        // Создание нового пользователя
                        val createUserResponse = apiService.createUser(user)
                        if (createUserResponse.isSuccessful) {
                            val userId = createUserResponse.body()?.user_id
                            if (userId != null) {
                                Toast.makeText(this@SignUpActivity, "User created successfully! ID: $userId", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@SignUpActivity, SignUp2Activity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@SignUpActivity, "Unexpected response from server", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@SignUpActivity, "Error: ${createUserResponse.body()?.error}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        // Обработка ошибок сети
                        Log.e("API_ERROR", "Error: ${e.message}", e)
                        Toast.makeText(this@SignUpActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter phone or email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Функция для проверки корректности email
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}")
        return emailPattern.matcher(email).matches()
    }
}
