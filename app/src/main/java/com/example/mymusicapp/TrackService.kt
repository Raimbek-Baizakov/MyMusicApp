package com.example.mymusicapp

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Сервис для работы с треками через REST API.
 * Обеспечивает все основные CRUD операции с треками.
 */
class TrackService {
    companion object {
        // Базовый URL API сервера
        private const val BASE_URL = "http://192.168.21.239:8080"
        // Тег для логгирования
        private const val TAG = "TrackService"
    }

    // HTTP клиент с настройками
    private val client = HttpClient(Android) {
        // Настройка JSON сериализации
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true }) // Игнорировать неизвестные поля
        }
        // Настройка логгирования
        install(Logging) {
            level = LogLevel.ALL // Логировать все HTTP операции
        }
    }

    /**
     * Поиск треков по названию.
     * @param title Строка для поиска
     * @return Список найденных треков
     * @throws NetworkException В случае ошибки сети
     */
    suspend fun searchTracksByTitle(title: String): List<Track> {
        Log.d(TAG, "Поиск треков по названию: $title")
        try {
            return client.get("$BASE_URL/tracks/search") {
                parameter("title", title) // Параметр запроса
            }.body() // Десериализация ответа
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при поиске треков: ${e.message}", e)
            throw NetworkException("Ошибка при поиске треков: ${e.message}")
        }
    }

    /**
     * Получение всех треков.
     * @return Список всех треков
     * @throws NetworkException В случае ошибки сети
     */
    suspend fun getAllTracks(): List<Track> {
        Log.d(TAG, "Запрос всех треков")
        try {
            return client.get("$BASE_URL/tracks").body()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении треков: ${e.message}", e)
            throw NetworkException("Ошибка при получении треков: ${e.message}")
        }
    }

    /**
     * Получение трека по ID.
     * @param id ID трека
     * @return Найденный трек
     * @throws NetworkException В случае ошибки сети
     */
    suspend fun getTrackById(id: Int): Track {
        Log.d(TAG, "Запрос трека по ID: $id")
        try {
            return client.get("$BASE_URL/tracks/$id").body()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении трека: ${e.message}", e)
            throw NetworkException("Ошибка при получении трека: ${e.message}")
        }
    }

    /**
     * Добавление нового трека.
     * @param track Трек для добавления
     * @return Добавленный трек (с ID от сервера)
     * @throws NetworkException В случае ошибки сети
     */
    suspend fun addTrack(track: Track): Track {
        Log.d(TAG, "Добавление трека: ${track.title}")
        try {
            return client.post("$BASE_URL/tracks") {
                contentType(ContentType.Application.Json)
                setBody(track) // Сериализация тела запроса
            }.body()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при добавлении трека: ${e.message}", e)
            throw NetworkException("Ошибка при добавлении трека: ${e.message}")
        }
    }

    /**
     * Обновление существующего трека.
     * @param id ID трека для обновления
     * @param track Новые данные трека
     * @return true если обновление успешно
     * @throws NetworkException В случае ошибки сети
     */
    suspend fun updateTrack(id: Int, track: Track): Boolean {
        Log.d(TAG, "Обновление трека ID: $id")
        try {
            val response = client.put("$BASE_URL/tracks/$id") {
                contentType(ContentType.Application.Json)
                setBody(track) // Полное обновление объекта
            }
            return response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при обновлении трека: ${e.message}", e)
            throw NetworkException("Ошибка при обновлении трека: ${e.message}")
        }
    }

    /**
     * Удаление трека по ID.
     * @param id ID трека для удаления
     * @return true если удаление успешно
     * @throws NetworkException В случае ошибки сети
     */
    suspend fun deleteTrack(id: Int): Boolean {
        Log.d(TAG, "Удаление трека ID: $id")
        try {
            val response = client.delete("$BASE_URL/tracks/$id")
            return response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при удалении трека: ${e.message}", e)
            throw NetworkException("Ошибка при удалении трека: ${e.message}")
        }
    }
}