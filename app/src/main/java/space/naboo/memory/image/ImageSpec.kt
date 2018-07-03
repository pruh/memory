package space.naboo.memory.image

import android.graphics.Bitmap
import android.support.annotation.DrawableRes

interface ImageSpec

data class BitmapImageSpec(val bitmap: Bitmap) : ImageSpec

data class ResourceImageSpec(@DrawableRes val resourceId: Int) : ImageSpec