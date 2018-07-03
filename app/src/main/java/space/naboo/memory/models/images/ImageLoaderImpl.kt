package space.naboo.memory.models.images

import android.content.Context
import com.bumptech.glide.Glide
import space.naboo.memory.image.BitmapImageSpec

interface ImageLoader {
    fun loadImage(url: String, width: Int, height: Int): BitmapImageSpec
}

class ImageLoaderImpl(private val context: Context) : ImageLoader {
    override fun loadImage(url: String, width: Int, height: Int): BitmapImageSpec {
        return BitmapImageSpec(Glide.with(context)
                .asBitmap()
                .load(url)
                .submit(width, height)
                .get())
    }
}
