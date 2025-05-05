package com.example.mymusicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Главный фрагмент приложения, отображает список треков на главном экране.
 * Реализует базовый функционал:
 * - Отображение списка треков
 * - Обработка кликов
 * - Работа с состоянием загрузки
 */
class HomeFragment : Fragment() {

    // region UI Components
    private lateinit var recyclerView: RecyclerView  // Основной список треков
    private lateinit var adapter: TrackAdapter      // Адаптер для RecyclerView
    private lateinit var progressBar: ProgressBar   // Индикатор загрузки
    private lateinit var emptyView: TextView        // Сообщение при пустом списке
    // endregion

    private lateinit var viewModel: TrackViewModel  // ViewModel для управления данными

    /**
     * Создание View для фрагмента
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Надуваем layout из fragment_home.xml
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * Инициализация UI после создания View
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация ViewModel (используем активити как owner для сохранения состояния между фрагментами)
        viewModel = ViewModelProvider(requireActivity())[TrackViewModel::class.java]

        // Привязка UI элементов
        recyclerView = view.findViewById(R.id.recycler_tracks)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyView = view.findViewById(R.id.empty_view)

        // Настройка RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())  // Вертикальный список
        adapter = TrackAdapter(
            onTrackClick = { track ->
                // Обработка клика на трек - передаем в MainActivity
                (requireActivity() as MainActivity).playTrack(track, "home")
            }
        )
        recyclerView.adapter = adapter  // Установка адаптера

        // Подписка на изменения данных
        observeViewModel()
    }

    /**
     * При возобновлении фрагмента обновляем данные
     */
    override fun onResume() {
        super.onResume()
        // Загружаем треки с указанием источника "home"
        viewModel.loadTracks("home")
    }

    /**
     * Наблюдение за изменениями в ViewModel
     */
    private fun observeViewModel() {
        // Наблюдение за списком треков
        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            if (tracks.isEmpty()) {
                // Показываем сообщение если список пуст
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                // Обновляем список если есть данные
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.updateTracks(tracks)
            }
        }

        // Наблюдение за состоянием загрузки
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Наблюдение за ошибками
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()  // Очищаем ошибку после показа
            }
        }
    }
}