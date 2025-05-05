package com.example.mymusicapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

/**
 * Активность для поиска музыкальных треков.
 * Позволяет:
 * - Вводить поисковый запрос
 * - Отображать результаты поиска
 * - Выбирать треки для воспроизведения
 * - Добавлять треки в плейлисты
 */
class SearchMusicListActivity : AppCompatActivity() {

    // UI элементы
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchProgressBar: ProgressBar

    // Адаптер и сервис
    private lateinit var trackAdapter: TrackAdapter
    private val trackService = TrackService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_music_list)

        initViews()
        setupRecyclerView()
        setupListeners()
    }

    /**
     * Инициализация UI элементов
     */
    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        statusTextView = findViewById(R.id.statusTextView)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        searchProgressBar = findViewById(R.id.searchProgressBar)
    }

    /**
     * Настройка RecyclerView для отображения результатов поиска
     */
    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter(
            tracks = emptyList(),
            onTrackClick = { track -> onTrackSelected(track) },
            onAddClick = { track -> onAddToPlaylistClicked(track) }
        )

        searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchMusicListActivity)
            adapter = trackAdapter
            setHasFixedSize(true) // Оптимизация для фиксированного размера элементов
        }
    }

    /**
     * Настройка обработчиков событий
     */
    private fun setupListeners() {
        searchButton.setOnClickListener {
            val searchQuery = searchEditText.text.toString().trim()
            when {
                searchQuery.isEmpty() -> {
                    showToast("Введите название трека для поиска")
                    searchEditText.requestFocus()
                }
                searchQuery.length < 3 -> {
                    showToast("Введите минимум 3 символа")
                }
                else -> {
                    performSearch(searchQuery)
                }
            }
        }

        // Обработка нажатия Enter в поле поиска
        searchEditText.setOnEditorActionListener { _, _, _ ->
            searchButton.performClick()
            true
        }
    }

    /**
     * Выполнение поиска треков
     * @param query Поисковый запрос
     */
    private fun performSearch(query: String) {
        // Показать состояние загрузки
        updateUIState(
            isLoading = true,
            message = "Поиск...",
            showResults = false
        )

        lifecycleScope.launch {
            try {
                val tracks = trackService.searchTracksByTitle(query)

                runOnUiThread {
                    if (tracks.isEmpty()) {
                        updateUIState(
                            isLoading = false,
                            message = "Треки не найдены",
                            showResults = false
                        )
                    } else {
                        updateUIState(
                            isLoading = false,
                            message = "Найдено треков: ${tracks.size}",
                            showResults = true
                        )
                        trackAdapter.updateTracks(tracks)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    updateUIState(
                        isLoading = false,
                        message = "Ошибка поиска: ${e.localizedMessage}",
                        showResults = false
                    )
                    showToast("Ошибка: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Обновление состояния UI
     */
    private fun updateUIState(isLoading: Boolean, message: String, showResults: Boolean) {
        searchProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        statusTextView.text = message
        searchResultsRecyclerView.visibility = if (showResults) View.VISIBLE else View.GONE
    }

    /**
     * Обработка выбора трека
     */
    private fun onTrackSelected(track: Track) {
        // Можно сразу запускать воспроизведение:
        // (requireActivity() as MainActivity).playTrack(track, "search")

        showToast("Выбран трек: ${track.title}")
    }

    /**
     * Обработка добавления трека в плейлист
     */
    private fun onAddToPlaylistClicked(track: Track) {
        // TODO: Реализовать выбор плейлиста через диалог
        showToast("Добавление трека в плейлист: ${track.title}")
    }

    /**
     * Показать Toast сообщение
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}