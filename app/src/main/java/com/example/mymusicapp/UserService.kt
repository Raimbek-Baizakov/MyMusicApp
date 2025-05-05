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

@Serializable
data class ExposedMusicUser(val phone: String?, val email: String?) {
    init {
        require(phone != null || email != null) { "Должен быть указан phone или email" }
    }
}

@Serializable
data class UserResponse(
    val id: Int,
    val phone: String?,
    val email: String?,
    val role: String
)

class UserAdditionException(message: String) : Exception(message)
class NetworkException(message: String) : Exception(message)

class UserService {
    companion object {
        private const val BASE_URL = "http://192.168.127.140:8080"
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    suspend fun addUser(user: ExposedMusicUser): UserResponse {
        Log.i("UserService", "Начало добавления пользователя: $user")
        try {
            return client.post("$BASE_URL/users") {
                contentType(ContentType.Application.Json)
                setBody(user)
            }.body()
        } catch (e: Exception) {
            Log.e("UserService", "Ошибка при добавлении пользователя: ${e.message}", e)
            throw UserAdditionException("Ошибка при добавлении пользователя: ${e.message}")
        }
    }

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

            if (phone.isNullOrEmpty() && email.isNullOrEmpty()) {
                throw IllegalArgumentException("Must provide phone or email")
            }

            return client.get(url) {
                expectSuccess = false
            }.let { response ->
                when (response.status) {
                    HttpStatusCode.OK -> response.body()
                    HttpStatusCode.NotFound -> null
                    else -> throw NetworkException("Server error: ${response.status}")
                }
            }
        } catch (e: Exception) {
            Log.e("UserService", "Ошибка при поиске пользователя", e)
            throw NetworkException("Ошибка при поиске пользователя: ${e.message}")
        }
    }

    suspend fun getAllUsers(): List<UserResponse> {
        try {
            return client.get("$BASE_URL/users").body()
        } catch (e: Exception) {
            Log.e("UserService", "Ошибка при получении списка пользователей", e)
            throw NetworkException("Ошибка при получении пользователей: ${e.message}")
        }
    }
}