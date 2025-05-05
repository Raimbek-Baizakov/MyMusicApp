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
 * Фрагмент для отображения списка избранных треков.
 * Аналогичен DownloadsMusicListFragment, но работает с избранными треками.
 */
class FavoritesMusicListFragment : Fragment() {

    // UI элементы
    private lateinit var recyclerView: RecyclerView  // Список треков
    private lateinit var adapter: TrackAdapter      // Адаптер для списка
    private lateinit var viewModel: TrackViewModel  // ViewModel для данных
    private lateinit var progressBar: ProgressBar   // Индикатор загрузки
    private lateinit var emptyView: TextView        // Заглушка при пустом списке

    /**
     * Создает view для фрагмента.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Надуваем layout из fragment_favorites_music_list.xml
        return inflater.inflate(R.layout.fragment_favorites_music_list, container, false)
    }

    /**
     * Инициализация UI после создания view.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация ViewModel (используем активити как owner для сохранения состояния)
        viewModel = ViewModelProvider(requireActivity())[TrackViewModel::class.java]

        // Привязка UI элементов
        recyclerView = view.findViewById(R.id.recycler_tracks)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyView = view.findViewById(R.id.empty_view)

        // Настройка RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())  // Линейный layout
        adapter = TrackAdapter(
            onFavoriteClick = { track ->
                // Обработчик клика на иконку избранного
                viewModel.toggleFavorite(track)  // Переключаем состояние в ViewModel
            },
            onTrackClick = { track ->
                // Обработчик клика на трек
                playTrack(track)  // Воспроизводим трек
            }
        )
        recyclerView.adapter = adapter  // Устанавливаем адаптер

        // Подписываемся на изменения данных
        observeViewModel()
    }

    /**
     * Наблюдаем за LiveData в ViewModel.
     */
    private fun observeViewModel() {
        // Список избранных треков
        viewModel.getFavoriteTracks().observe(viewLifecycleOwner) { tracks ->
            if (tracks.isEmpty()) {
                // Показываем заглушку если список пуст
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                // Скрываем заглушку и обновляем список
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.updateTracks(tracks)
            }
        }

        // Состояние загрузки
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Ошибки
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Воспроизведение трека.
     * @param track - трек для воспроизведения
     */
    private fun playTrack(track: Track) {
        // Уведомление для отладки
        Toast.makeText(requireContext(), "Playing ${track.title}", Toast.LENGTH_SHORT).show()

        // Вызов метода в MainActivity
        // "favorites" указывает источник трека для аналитики/истории
        (requireActivity() as MainActivity).playTrack(track, "favorites")
    }
}