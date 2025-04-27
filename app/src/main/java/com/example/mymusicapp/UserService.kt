package com.example.mymusicapp

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.json
import io.ktor.http.Url

// Определение данных пользователя
@Serializable
data class ExposedMusicUser(val phone: String?, val email: String?) {
    init {
        require(phone != null || email != null) { "Должен быть указан phone или email" }
    }
}

// Класс ответа
@Serializable
data class UserResponse(val phone: String?, val email: String?)

// Исключения
class UserAdditionException(message: String) : Exception(message)
class UserNotFoundException(message: String) : Exception(message)
class NetworkException(message: String) : Exception(message)

class UserService {

    companion object {
        private const val BASE_URL = "http://192.168.127.38:8080" // Либо вынести в конфигурацию
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.ALL // Поменять на INFO для релиза
        }
    }

    suspend fun addUser(user: ExposedMusicUser): Int {
        Log.i("UserService", "Начало добавления пользователя: $user")
        try {
            val response = client.post("$BASE_URL/users") {
                Log.i("UserService", "Отправка запроса POST на адрес: $BASE_URL/users")
                contentType(ContentType.Application.Json)
                setBody(user)
            }
            val statusCode = response.status.value
            Log.i("UserService", "Пользователь добавлен. Статус ответа: $statusCode")
            return statusCode
        } catch (e: Exception) {
            Log.e("UserService", "Ошибка при добавлении пользователя: ${e.message}", e)
            throw UserAdditionException("Ошибка при добавлении пользователя: ${e.message}")
        }
    }

    suspend fun checkUser(phone: String?, email: String?): UserResponse? {
        Log.i("UserService", "Начало проверки пользователя. phone: $phone, email: $email")
        try {
            val url = buildString {
                append("$BASE_URL/users?")
                if (!phone.isNullOrEmpty()) append("phone=$phone")
                if (!email.isNullOrEmpty()) {
                    if (!phone.isNullOrEmpty()) append("&")
                    append("email=$email")
                }
            }
            Log.i("UserService", "Формируемый URL запроса: $url")

            val finalUrl = Url(url)
            Log.i("UserService", "Отправка запроса GET на адрес: $finalUrl")

            val response = client.get(finalUrl)
            Log.i("UserService", "Получен ответ от сервера. Статус: ${response.status.value}")

            if (response.status.isSuccess()) {
                val userResponse = response.body<UserResponse>()
                Log.i("UserService", "Пользователь найден: $userResponse")
                return userResponse
            } else {
                Log.w("UserService", "Пользователь не найден. Статус ответа: ${response.status.value}")
                throw UserNotFoundException("Пользователь не найден")
            }
        } catch (e: Exception) {
            Log.e("UserService", "Ошибка при запросе проверки пользователя: ${e.message}", e)
            throw NetworkException("Ошибка при запросе: ${e.message}")
        }
    }
}