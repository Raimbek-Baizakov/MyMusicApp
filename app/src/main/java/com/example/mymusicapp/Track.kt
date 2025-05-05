package com.example.mymusicapp

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: Int,
    val title: String,
    val author: String,
    val duration: Int,  // Продолжительность в секундах
    val imagePath: String?,
    var downloaded: Boolean, // Изменено на var
    var favorite: Boolean,
    var playlistName: String?,
    val genre: String?,
    val file_Path: String?  // Новое поле для пути к файлу

) {
    // Вычисляемое свойство для форматированного отображения времени
    val formattedDuration: String
        get() {
            val minutes = duration / 60
            val seconds = duration % 60
            return "$minutes:${seconds.toString().padStart(2, '0')}"
        }
}
