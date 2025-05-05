package com.example.mymusicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Фрагмент для отображения и управления плейлистами.
 * Показывает список плейлистов в виде сетки 2 колонки.
 */
class PlaylistsFragment : Fragment() {

    // Временный список плейлистов (в реальном приложении следует использовать ViewModel)
    private val playlists = mutableListOf(
        "My playlist 1",
        "My playlist 2",
        "My playlist 3",
        "My playlist 4",
        "My playlist 5",
        "My playlist 6"
    )

    private lateinit var adapter: PlaylistsAdapter  // Адаптер для списка плейлистов

    /**
     * Создание View для фрагмента
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Надуваем layout из fragment_playlists.xml
        return inflater.inflate(R.layout.fragment_playlists, container, false)
    }

    /**
     * Инициализация UI после создания View
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPlaylists)

        // Установка GridLayoutManager с 2 колонками
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Инициализация адаптера с обработчиками кликов
        adapter = PlaylistsAdapter(
            playlists = playlists,
            onPlaylistClick = { playlistName ->
                // Обработка клика по плейлисту
                Toast.makeText(
                    requireContext(),
                    "Opening: $playlistName",
                    Toast.LENGTH_SHORT
                ).show()

                // TODO: Реализовать переход к содержимому плейлиста
                // (можно открыть новый фрагмент с треками плейлиста)
            },
            onAddButtonClick = {
                // Обработка клика по кнопке добавления
                val newPlaylistNumber = playlists.size + 1
                val newPlaylistName = "My playlist $newPlaylistNumber"

                // Добавляем новый плейлист
                playlists.add(newPlaylistName)
                adapter.notifyItemInserted(playlists.size - 1)

                Toast.makeText(
                    requireContext(),
                    "Added: $newPlaylistName",
                    Toast.LENGTH_SHORT
                ).show()

                // TODO: Реализовать сохранение в базу данных/ViewModel
            }
        )

        recyclerView.adapter = adapter  // Установка адаптера
    }
}