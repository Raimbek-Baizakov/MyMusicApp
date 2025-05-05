package com.example.mymusicapp

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Сервис для воспроизведения аудиотреков.
 * Обеспечивает базовые функции медиаплеера и уведомляет о событиях через callback-интерфейс.
 */
class TrackPlayerService {
    // MediaPlayer для воспроизведения треков
    private var mediaPlayer: MediaPlayer? = null

    // LiveData для отслеживания состояния воспроизведения
    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    // LiveData для отслеживания текущей позиции трека
    private val _currentPosition = MutableLiveData<Int>(0)
    val currentPosition: LiveData<Int> = _currentPosition

    // LiveData для отслеживания текущего трека
    private val _currentTrack = MutableLiveData<Track?>(null)
    val currentTrack: LiveData<Track?> = _currentTrack

    /**
     * Интерфейс для обработки событий плеера
     */
    interface OnPlayerEventListener {
        fun onPrepared() // Вызывается когда трек готов к воспроизведению
        fun onCompletion() // Вызывается когда трек закончился
        fun onError(message: String) // Вызывается при ошибке воспроизведения
    }

    /**
     * Воспроизведение трека
     * @param context Контекст приложения
     * @param track Трек для воспроизведения
     * @param listener Обработчик событий плеера
     */
    fun playTrack(context: Context, track: Track, listener: OnPlayerEventListener) {
        // Проверка наличия пути к файлу
        val filePath = track.file_Path ?: run {
            listener.onError("File path is invalid!")
            return
        }

        try {
            // Освобождаем предыдущий MediaPlayer
            release()
            // Устанавливаем текущий трек
            _currentTrack.value = track

            // Инициализация и настройка MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath) // Установка источника данных
                prepareAsync() // Асинхронная подготовка к воспроизведению

                // Обработчик готовности к воспроизведению
                setOnPreparedListener {
                    start() // Начинаем воспроизведение
                    _isPlaying.postValue(true) // Обновляем состояние
                    listener.onPrepared() // Уведомляем слушателя
                }

                // Обработчик завершения воспроизведения
                setOnCompletionListener {
                    _isPlaying.postValue(false) // Обновляем состояние
                    listener.onCompletion() // Уведомляем слушателя
                }

                // Обработчик ошибок
                setOnErrorListener { _, _, _ ->
                    _isPlaying.postValue(false) // Обновляем состояние
                    listener.onError("Error playing track") // Уведомляем слушателя
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("TrackPlayerService", "Error playing track", e)
            listener.onError("Error playing track: ${e.message}")
        }
    }

    /**
     * Переключение между воспроизведением и паузой
     */
    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause() // Ставим на паузу
                _isPlaying.postValue(false) // Обновляем состояние
            } else {
                it.start() // Продолжаем воспроизведение
                _isPlaying.postValue(true) // Обновляем состояние
            }
        }
    }

    /**
     * Перемотка трека на указанную позицию
     * @param position Позиция в миллисекундах
     */
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    /**
     * Получение текущей позиции воспроизведения
     * @return Текущая позиция в миллисекундах
     */
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    /**
     * Получение длительности текущего трека
     * @return Длительность в миллисекундах
     */
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    /**
     * Проверка, воспроизводится ли трек в данный момент
     * @return true если воспроизведение активно
     */
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    /**
     * Освобождение ресурсов MediaPlayer
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.postValue(false) // Обновляем состояние
    }
}