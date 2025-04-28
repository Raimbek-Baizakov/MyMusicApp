package com.example.mymusicapp

data class Song(
    val id: Int,
    val title: String,
    val author: String,
    val duration: String,
    val imagePath: String,
    val downloaded: Boolean = false,  // По умолчанию False
    val favorite: Boolean = false,    // По умолчанию False
    val playlistName: String? = null  // По умолчанию null
)
