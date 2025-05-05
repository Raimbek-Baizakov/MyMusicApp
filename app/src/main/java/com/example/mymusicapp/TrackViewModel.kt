package com.example.mymusicapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * ViewModel для управления данными треков и состоянием плеера.
 * Связывает UI с бизнес-логикой и сервисом треков.
 */
class TrackViewModel(application: Application) : AndroidViewModel(application) {

    // Сервис для работы с треками
    private val trackService = TrackService()

    // LiveData для списка треков
    private val _tracks = MutableLiveData<List<Track>>(emptyList())
    val tracks: LiveData<List<Track>> = _tracks

    // LiveData для состояния загрузки
    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    // LiveData для ошибок
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // LiveData для текущего трека
    private val _currentTrack = MutableLiveData<Track?>(null)
    val currentTrack: LiveData<Track?> = _currentTrack

    // LiveData для состояния воспроизведения
    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    // LiveData для источника текущего плейлиста
    private val _playlistSource = MutableLiveData<String>("home")
    val playlistSource: LiveData<String> = _playlistSource

    // Кэшированные списки для разных категорий
    private val _favoriteTracks = MutableLiveData<List<Track>>(emptyList())
    private val _downloadedTracks = MutableLiveData<List<Track>>(emptyList())

    // Инициализация - загрузка треков при создании ViewModel
    init {
        loadTracks("home")
    }

    /**
     * Загрузка треков из указанного источника
     * @param source Источник треков ("home", "favorites", "downloads")
     */
    fun loadTracks(source: String = "home") {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _playlistSource.value = source

                when (source) {
                    "home" -> {
                        // Загрузка всех треков
                        val trackList = trackService.getAllTracks()
                        _tracks.value = trackList
                    }
                    "favorites" -> {
                        // Загрузка избранных треков
                        val trackList = trackService.getAllTracks().filter { it.favorite }
                        _tracks.value = trackList
                        _favoriteTracks.value = trackList
                    }
                    "downloads" -> {
                        // Загрузка загруженных треков
                        val trackList = trackService.getAllTracks().filter { it.downloaded }
                        _tracks.value = trackList
                        _downloadedTracks.value = trackList
                    }
                    else -> {
                        // По умолчанию загружаем все треки
                        val trackList = trackService.getAllTracks()
                        _tracks.value = trackList
                    }
                }
                _loading.value = false
            } catch (e: Exception) {
                _loading.value = false
                _error.value = e.message ?: "Неизвестная ошибка при загрузке треков"
            }
        }
    }

    /**
     * Переключение состояния "Избранное" для трека
     * @param track Трек для обновления
     */
    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            try {
                _error.value = null
                // Создаем копию трека с измененным состоянием
                val updatedTrack = track.copy(favorite = !track.favorite)
                val success = trackService.updateTrack(track.id, updatedTrack)

                if (success) {
                    // Локальное обновление списка
                    _tracks.value = _tracks.value?.map {
                        if (it.id == track.id) updatedTrack else it
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Неизвестная ошибка при обновлении трека"
            }
        }
    }

    /**
     * Добавление нового трека
     * @param track Трек для добавления
     */
    fun addTrack(track: Track) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val newTrack = trackService.addTrack(track)
                // Добавление в текущий список
                val currentList = _tracks.value?.toMutableList() ?: mutableListOf()
                currentList.add(newTrack)
                _tracks.value = currentList

                _loading.value = false
            } catch (e: Exception) {
                _loading.value = false
                _error.value = e.message ?: "Неизвестная ошибка при добавлении трека"
            }
        }
    }

    /**
     * Удаление трека
     * @param trackId ID трека для удаления
     */
    fun deleteTrack(trackId: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                val success = trackService.deleteTrack(trackId)

                if (success) {
                    // Удаление из локального списка
                    _tracks.value = _tracks.value?.filter { it.id != trackId }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Неизвестная ошибка при удалении трека"
            }
        }
    }

    /**
     * Получение списка избранных треков
     * @return LiveData со списком избранных треков
     */
    fun getFavoriteTracks(): LiveData<List<Track>> {
        if (_favoriteTracks.value.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val tracks = trackService.getAllTracks().filter { it.favorite }
                    _favoriteTracks.postValue(tracks)
                } catch (e: Exception) {
                    _error.postValue(e.message ?: "Ошибка при загрузке избранных треков")
                }
            }
        }
        return _favoriteTracks
    }

    /**
     * Получение списка загруженных треков
     * @return LiveData со списком загруженных треков
     */
    fun getDownloadedTracks(): LiveData<List<Track>> {
        if (_downloadedTracks.value.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val tracks = trackService.getAllTracks().filter { it.downloaded }
                    _downloadedTracks.postValue(tracks)
                } catch (e: Exception) {
                    _error.postValue(e.message ?: "Ошибка при загрузке загруженных треков")
                }
            }
        }
        return _downloadedTracks
    }

    // Методы управления плеером

    /**
     * Установка текущего трека
     * @param track Трек для воспроизведения
     */
    fun setCurrentTrack(track: Track) {
        _currentTrack.value = track
    }

    /**
     * Установка состояния воспроизведения
     * @param playing true - воспроизводится, false - на паузе
     */
    fun setIsPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    /**
     * Очистка сообщения об ошибке
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Получение следующего трека в списке
     * @return Следующий трек или null
     */
    fun getNextTrack(): Track? {
        val currentTracks = _tracks.value ?: return null
        val currentIndex = currentTracks.indexOf(_currentTrack.value)

        if (currentIndex == -1 || currentTracks.isEmpty()) return null

        val nextIndex = (currentIndex + 1) % currentTracks.size
        return currentTracks[nextIndex]
    }

    /**
     * Получение предыдущего трека в списке
     * @return Предыдущий трек или null
     */
    fun getPreviousTrack(): Track? {
        val currentTracks = _tracks.value ?: return null
        val currentIndex = currentTracks.indexOf(_currentTrack.value)

        if (currentIndex == -1 || currentTracks.isEmpty()) return null

        val prevIndex = if (currentIndex > 0) currentIndex - 1 else currentTracks.size - 1
        return currentTracks[prevIndex]
    }
}