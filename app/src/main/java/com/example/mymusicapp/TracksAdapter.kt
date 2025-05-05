package com.example.mymusicapp

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymusicapp.utils.ImageLoader

/**
 * Адаптер для отображения списка треков в RecyclerView.
 * Обрабатывает клики по элементам и обновление состояния треков.
 */
class TrackAdapter(
    private var tracks: List<Track> = emptyList(), // Список треков для отображения
    private val onFavoriteClick: (Track) -> Unit = {}, // Обработчик клика на "избранное"
    private val onDownloadClick: (Track) -> Unit = {}, // Обработчик клика на загрузку
    private val onAddClick: (Track) -> Unit = {}, // Обработчик клика на добавление в плейлист
    private val onTrackClick: (Track) -> Unit = {}, // Обработчик клика по треку
    private val availablePlaylists: List<String> = emptyList() // Доступные плейлисты
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    // Сервис для работы с треками
    private val trackService = TrackService()

    /**
     * ViewHolder для отображения отдельного трека
     */
    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Элементы UI
        val songImage: ImageView = view.findViewById(R.id.song_image) // Обложка трека
        val songTitle: TextView = view.findViewById(R.id.song_title) // Название трека
        val songAuthor: TextView = view.findViewById(R.id.song_author) // Исполнитель
        val songDuration: TextView = view.findViewById(R.id.song_duration) // Длительность
        val songPlaylist: TextView = view.findViewById(R.id.song_playlist) // Название плейлиста
        val iconFavorite: ImageButton = view.findViewById(R.id.icon_favorite) // Кнопка избранного
        val iconDownloaded: ImageButton = view.findViewById(R.id.icon_downloaded) // Кнопка загрузки
        val iconAdd: ImageButton = view.findViewById(R.id.icon_add) // Кнопка добавления
    }

    /**
     * Создание нового ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)
        return TrackViewHolder(view)
    }

    /**
     * Привязка данных к ViewHolder
     */
    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        val context = holder.itemView.context

        // Установка основных данных трека
        holder.songTitle.text = track.title
        holder.songAuthor.text = track.author
        holder.songDuration.text = track.formattedDuration

        // Обработчик клика на кнопку добавления в плейлист
        holder.iconAdd.setOnClickListener {
            showPlaylistSelectionDialog(holder.itemView.context, track)
            onAddClick(track) // Вызов внешнего обработчика
        }

        // Отображение названия плейлиста (если есть)
        track.playlistName?.let {
            holder.songPlaylist.visibility = View.VISIBLE
            holder.songPlaylist.text = "Playlist: $it"
        } ?: run {
            holder.songPlaylist.visibility = View.GONE
        }

        // Загрузка обложки трека
        loadTrackImage(holder.songImage, track.imagePath, context)

        // Установка иконки избранного в зависимости от состояния
        holder.iconFavorite.setImageResource(
            if (track.favorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )

        // Установка иконки загрузки в зависимости от состояния
        holder.iconDownloaded.setImageResource(
            if (track.downloaded) R.drawable.ic_delete else R.drawable.ic_download
        )

        // Обработчик клика на избранное
        holder.iconFavorite.setOnClickListener {
            // Изменение состояния
            track.favorite = !track.favorite
            notifyItemChanged(position) // Обновление отображения

            // Асинхронное обновление на сервере
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    updateTrackInDatabase(track)
                } catch (e: Exception) {
                    Log.e("TrackAdapter", "Ошибка обновления трека: ${e.message}", e)
                }
            }

            onFavoriteClick(track) // Вызов внешнего обработчика
        }

        // Обработчик клика на загрузку
        holder.iconDownloaded.setOnClickListener {
            track.downloaded = !track.downloaded
            notifyItemChanged(position)
            updateTrackInDatabase(track)
            onDownloadClick(track)
        }

        // Обработчик клика по всему элементу трека
        holder.itemView.setOnClickListener { onTrackClick(track) }
    }

    /**
     * Показывает диалог выбора плейлиста
     */
    private fun showPlaylistSelectionDialog(context: Context, track: Track) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.playlist_selection_dialog)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.playlistsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Адаптер для списка плейлистов
        val adapter = PlaylistSelectionAdapter(availablePlaylists) { selectedPlaylist ->
            track.playlistName = selectedPlaylist
            notifyItemChanged(tracks.indexOf(track))

            // Асинхронное обновление на сервере
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    trackService.updateTrack(track.id, track)
                } catch (e: Exception) {
                    Log.e("TrackAdapter", "Ошибка при обновлении плейлиста трека", e)
                }
            }

            dialog.dismiss()
        }

        recyclerView.adapter = adapter
        dialog.show()
    }

    /**
     * Обновление трека в базе данных
     */
    private fun updateTrackInDatabase(track: Track) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                trackService.updateTrack(track.id, track)
            } catch (e: Exception) {
                Log.e("TrackAdapter", "Ошибка обновления трека: ${e.message}", e)
            }
        }
    }

    /**
     * Загрузка изображения трека с помощью ImageLoader
     */
    private fun loadTrackImage(imageView: ImageView, imagePath: String?, context: Context) {
        ImageLoader.loadTrackImage(imageView, imagePath, context)
    }

    /**
     * Количество элементов в списке
     */
    override fun getItemCount() = tracks.size

    /**
     * Обновление списка треков
     */
    fun updateTracks(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}