package space.naboo.memory.image

import android.support.annotation.DrawableRes
import android.widget.ImageView

interface ImageSetter {

    companion object {
        fun newInstance(imageView: ImageView, imageSpec: ImageSpec): ImageSetter {
            return when (imageSpec) {
                is BitmapImageSpec -> BitmapImageSetter(imageView, imageSpec)
                is ResourceImageSpec -> ResourceImageSetter(imageView, imageSpec)
                else -> throw IllegalArgumentException("Unknown image specification class ${imageSpec::class}")
            }
        }
    }

    fun setImage()
}

private class BitmapImageSetter(private val imageView: ImageView,
        private val imageSpec: BitmapImageSpec) : ImageSetter {
    override fun setImage() {
        imageView.setImageBitmap(imageSpec.bitmap)
    }
}

private class ResourceImageSetter(private val imageView: ImageView,
        @DrawableRes private val imageSpec: ResourceImageSpec) : ImageSetter {
    override fun setImage() {
        imageView.setImageResource(imageSpec.resourceId)
    }
}
