package com.example.mymusicapp.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mymusicapp.R

/**
 * Утилитарный объект для загрузки и отображения изображений треков.
 * Использует библиотеку Glide для эффективной и безопасной загрузки изображений.
 *
 * Особенности:
 * - Автоматическая обработка null-значений
 * - Загрузка из ресурсов приложения (drawable)
 * - Запасное изображение при ошибках
 * - Плейсхолдер во время загрузки
 */
object ImageLoader {

    /**
     * Загружает изображение трека в ImageView.
     *
     * @param imageView Целевой ImageView для отображения изображения
     * @param imagePath Путь к изображению (имя ресурса без расширения)
     * @param context Контекст для доступа к ресурсам
     */
    fun loadTrackImage(imageView: ImageView, imagePath: String?, context: Context) {
        // Проверяем и обрабатываем путь к изображению
        val resourceName = imagePath?.substringBeforeLast(".") ?: run {
            // Если путь null или пустой - загружаем изображение по умолчанию
            loadDefaultImage(imageView, context)
            return
        }

        // Получаем ID ресурса по имени
        val resourceId = context.resources.getIdentifier(
            resourceName,
            "drawable",
            context.packageName
        )

        // Загружаем изображение с помощью Glide
        Glide.with(context)
            .load(if (resourceId != 0) resourceId else R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background) // Показываем во время загрузки
            .error(R.drawable.ic_launcher_background)       // Показываем при ошибке
            .into(imageView)
    }

    /**
     * Загружает изображение по умолчанию.
     */
    private fun loadDefaultImage(imageView: ImageView, context: Context) {
        Glide.with(context)
            .load(R.drawable.ic_launcher_background)
            .into(imageView)
    }

    /**
     * Оптимизированная версия с использованием расширения функций.
     */
    fun ImageView.loadTrackImageOptimized(imagePath: String?) {
        ImageLoader.loadTrackImage(this, imagePath, context)
    }
}