import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mymusicapp.R
import com.example.mymusicapp.Song

class SongAdapter(
    private val songList: MutableList<Song>,
    private val onFavoriteClick: (Int) -> Unit,
    private val onDownloadClick: (Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    // ViewHolder класс
    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.song_title)
        val authorText: TextView = view.findViewById(R.id.song_author)
        val durationText: TextView = view.findViewById(R.id.song_duration)
        val imageView: ImageView = view.findViewById(R.id.song_image)
        val playlistText: TextView = view.findViewById(R.id.song_playlist)
        val iconFavorite: ImageView = view.findViewById(R.id.icon_favorite)
        val iconDownloaded: ImageView = view.findViewById(R.id.icon_downloaded)
    }

    // Создание ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view)
    }

    // Привязка данных
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songList[position]

        // Заполнение данных
        holder.titleText.text = song.title
        holder.authorText.text = song.author
        holder.durationText.text = song.duration
        holder.playlistText.text = song.playlistName ?: "Playlist: None"

        // Загрузка изображения
        val resourceId = holder.itemView.context.resources.getIdentifier(
            song.imagePath.substringBeforeLast("."),
            "drawable",
            holder.itemView.context.packageName
        )
        Glide.with(holder.itemView.context)
            .load(if (resourceId != 0) resourceId else R.drawable.ic_launcher_background)
            .into(holder.imageView)

        // Обновление иконок
        updateIcons(holder, song)

        // Обработчики кликов
        holder.iconFavorite.setOnClickListener {
            songList[position] = song.copy(favorite = !song.favorite) // меняем флаг
            notifyItemChanged(position) // уведомляем адаптер
            onFavoriteClick(position) // уведомляем о событии
        }

        holder.iconDownloaded.setOnClickListener {
            songList[position] = song.copy(downloaded = !song.downloaded) // меняем флаг
            notifyItemChanged(position) // уведомляем адаптер
            onDownloadClick(position) // уведомляем о событии
        }
    }

    // Обновление иконок
    private fun updateIcons(holder: SongViewHolder, song: Song) {
        Log.d("SongAdapter", "Updating icons for: ${song.title} | Favorite: ${song.favorite}, Downloaded: ${song.downloaded}")
        holder.iconFavorite.setImageResource(
            if (song.favorite) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_border
        )
        holder.iconDownloaded.setImageResource(
            if (song.downloaded) R.drawable.ic_download_done
            else R.drawable.ic_download
        )
    }

    // Возвращает количество элементов
    override fun getItemCount(): Int = songList.size
}
