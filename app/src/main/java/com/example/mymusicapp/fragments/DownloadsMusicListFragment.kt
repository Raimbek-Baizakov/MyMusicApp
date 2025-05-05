package com.example.mymusicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Toast

/**
 * Фрагмент для отображения списка загруженных треков.
 * Наследуется от Fragment и использует ViewModel для управления данными.
 */
class DownloadsMusicListFragment : Fragment() {

    // UI элементы
    private lateinit var recyclerView: RecyclerView  // RecyclerView для списка треков
    private lateinit var adapter: TrackAdapter      // Адаптер для RecyclerView
    private lateinit var viewModel: TrackViewModel  // ViewModel для управления данными
    private lateinit var progressBar: ProgressBar   // Индикатор загрузки
    private lateinit var emptyView: TextView        // Текст при пустом списке

    /**
     * Создает view для фрагмента.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Надуваем layout для этого фрагмента
        return inflater.inflate(R.layout.fragment_downloads_music_list, container, false)
    }

    /**
     * Вызывается после создания view, здесь инициализируем UI компоненты.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация ViewModel - используем активити как owner,
        // чтобы сохранять данные при повороте экрана
        viewModel = ViewModelProvider(requireActivity())[TrackViewModel::class.java]

        // Инициализация UI компонентов
        recyclerView = view.findViewById(R.id.recycler_tracks)  // Список треков
        progressBar = view.findViewById(R.id.progress_bar)     // Индикатор загрузки
        emptyView = view.findViewById(R.id.empty_view)         // Текст "список пуст"

        // Настройка RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())  // Вертикальный список
        adapter = TrackAdapter(
            onFavoriteClick = { track ->
                // Обработчик клика на "избранное"
                viewModel.toggleFavorite(track)
            },
            onTrackClick = { track ->
                // Обработчик клика на трек - воспроизведение
                playTrack(track)
            }
        )
        recyclerView.adapter = adapter  // Устанавливаем адаптер

        // Подписываемся на изменения данных в ViewModel
        observeViewModel()
    }

    /**
     * Наблюдаем за изменениями данных в ViewModel.
     */
    private fun observeViewModel() {
        // Наблюдение за списком загруженных треков
        viewModel.getDownloadedTracks().observe(viewLifecycleOwner) { tracks ->
            if (tracks.isEmpty()) {
                // Если треков нет - показываем "список пуст"
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                // Если есть треки - скрываем "список пуст" и обновляем адаптер
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.updateTracks(tracks)
            }
        }

        // Наблюдение за состоянием загрузки
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Показываем/скрываем индикатор загрузки
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Наблюдение за ошибками
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                // Показываем Toast с сообщением об ошибке
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Воспроизводит выбранный трек.
     * @param track - трек для воспроизведения
     */
    private fun playTrack(track: Track) {
        // Показываем уведомление о воспроизведении (для отладки)
        Toast.makeText(requireContext(), "Playing ${track.title}", Toast.LENGTH_SHORT).show()

        // Вызываем метод воспроизведения из MainActivity
        // "downloads" - указывает источник треков (для истории/аналитики)
        (requireActivity() as MainActivity).playTrack(track, "downloads")
    }
}