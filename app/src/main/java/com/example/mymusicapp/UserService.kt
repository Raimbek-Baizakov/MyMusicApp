package com.example.mymusicapp

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.json

// Сериализуемые данные для пользователя
@Serializable
data class ExposedMusicUser(val phone: String?, val email: String?) {
    // Проверяем, что указан хотя бы один параметр (phone или email)
    init {
        require(phone != null || email != null) { "Должен быть указан phone или email" }
    }
}

// Ответ от сервера при запросе пользователя
@Serializable
data class UserResponse(
    val id: Int,
    val phone: String?,
    val email: String?,
    val role: String // Роль пользователя (например, "admin", "user" и т. д.)
)

// Исключение при ошибке добавления пользователя
class UserAdditionException(message: String) : Exception(message)

// Исключение для сетевых ошибок
class NetworkException(message: String) : Exception(message)

class UserService {
    companion object {
        private const val BASE_URL = "http://192.168.21.239:8080" // Адрес сервера API
    }

    // Создание HTTP-клиента с настройками сериализации и логирования
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true }) // Игнорируем неизвестные ключи в JSON
        }
        install(Logging) {
            level = LogLevel.ALL // Включаем полное логирование запросов
        }
    }

    // Метод для добавления пользователя через POST-запрос
    suspend fun addUser(user: ExposedMusicUser): UserResponse {
        Log.i("UserService", "Начало добавления пользователя: $user")
        try {
            return client.post("$BASE_URL/users") {
                contentType(ContentType.Application.Json) // Определяем тип запроса
                setBody(user) // Передаем объект пользователя
            }.body()
        } catch (e: Exception) {
            Log.e("UserService", "Ошибка при добавлении пользователя: ${e.message}", e)
            throw UserAdditionException("Ошибка при добавлении пользователя: ${e.message}")
        }
    }

    // Вход пользователя: ищем в базе, если нет — создаем нового
    suspend fun loginUser(user: ExposedMusicUser): UserResponse {
        try {
            val existingUser = findUser(phone = user.phone, email = user.email)
            return existingUser ?: run {
                Log.i("UserService", "Пользователь не найден, создаем нового")
                addUser(user).also {
                    Log.i("UserService", "Пользователь создан: $it")
                }
            }
        } catch (e: Exception) {
            Log.e("UserService", "Ошибка при входе пользователя", e)
            throw NetworkException("Ошибка при входе пользователя: ${e.message}")
        }
    }

    // Метод для поиска пользователя по номеру телефона или email
    suspend fun findUser(phone: String? = null, email: String? = null): UserResponse? {
        try {
            val url = buildString {
                append("$BASE_URL/users/find?")
                if (!phone.isNullOrEmpty()) append("phone=${phone.encodeURLParameter()}")
                if (!email.isNullOrEmpty()) {
                    if (!phone.isNullOrEmpty()) append("&")
                    append("email=${email.encodeURLParameter()}")
                }
            }

            // Если оба параметра пустые, выбрасываем исключение
            if (phone.isNullOrEmpty() && email.isNullOrEmpty()) {
                throw IllegalArgumentException("Must provide phone or email")
            }

            return client.get(url) {
                expectSuccess = false // Позволяет обработать статус 404 вместо исключения
            }.let { response ->
                when (response.status) {
                    HttpStatusCode.OK -> response.body()
                    HttpStatusCode.NotFound -> null // Если сервер не нашел пользователя, возвращаем null
                    else -> throw NetworkException("Server error: ${response.status}")
                }
            }
        } catch (e: Exception) {
            Log.e("UserService", "Ошибка при поиске пользователя", e)
            throw NetworkException("Ошибка при поиске пользователя: ${e.message}")
        }
    }

    // Метод для получения списка всех пользователей
    suspend fun getAllUsers(): List<UserResponse> {
        try {
            return client.get("$BASE_URL/users").body()
        } catch (e: Exception) {
            Log.e("UserService", "Ошибка при получении списка пользователей", e)
            throw NetworkException("Ошибка при получении пользователей: ${e.message}")
        }
    }
}
