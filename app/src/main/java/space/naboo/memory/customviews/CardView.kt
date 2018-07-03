package space.naboo.memory.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import space.naboo.memory.image.BitmapImageSpec
import space.naboo.memory.image.ImageSetter
import space.naboo.memory.image.ImageSpec
import space.naboo.memory.image.ResourceImageSpec

class CardView : ImageView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun setData(imageSpec: ImageSpec) {
        when(imageSpec) {
            is BitmapImageSpec -> setImageBitmap(imageSpec.bitmap)
            is ResourceImageSpec -> setImageResource(imageSpec.resourceId)
            else -> throw IllegalArgumentException("ImageSpec of type ${imageSpec::class} is not supported")
        }
    }

    fun flip(image: ImageSpec, animationDuration: Long = 200L, endAction: (() -> Unit)?) {
        animate()
                .scaleX(0f)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(animationDuration)
                .withEndAction {
                    ImageSetter.newInstance(this, image).setImage()
                    animate()
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .setDuration(animationDuration)
                            .scaleX(1f)
                            .withEndAction {
                                endAction?.invoke()
                            }
                }
    }

    fun setCardClickListener(position: Int, listener: CardClickListener?) {
        listener?.let {
            setOnClickListener {
                listener.onCardClicked(position)
            }
        } ?: run {
            setOnClickListener(null)
        }
    }

}
