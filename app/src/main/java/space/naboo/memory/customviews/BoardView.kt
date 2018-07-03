package space.naboo.memory.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import space.naboo.memory.R
import space.naboo.memory.extensions.use
import space.naboo.memory.image.ImageSpec
import space.naboo.memory.models.Board

class BoardView : LinearLayout {

    private lateinit var cardViews: MutableList<CardView>

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER

        context.theme.obtainStyledAttributes(attrs, R.styleable.BoardView, 0, 0).use { a ->
            val columnsNumber = a.getInteger(R.styleable.BoardView_columnsNumber, 4)
            val rowsNumber = a.getInteger(R.styleable.BoardView_rowsNumber, 4)

            setBoardSize(columnsNumber, rowsNumber)
        }
    }

    fun setBoardSize(columnsNumber: Int, rowsNumber: Int) {
        cardViews = mutableListOf()
        removeAllViews()

        (0 until rowsNumber).forEach {
            val (rowLayout, rowCardViews) = createRow(context, columnsNumber)

            cardViews.addAll(rowCardViews)

            addView(rowLayout)
        }

        invalidate()
        requestLayout()
    }

    private fun getHorizontalMargin(): Int {
        return (resources.getDimension(R.dimen.horizontal_board_margin) * resources.displayMetrics.density).toInt()
    }

    private fun createRow(context: Context, columnsNumber: Int): Pair<LinearLayout, List<CardView>> {
        val rowLayout = LinearLayout(context)
        rowLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            weight = 1f
        }
        rowLayout.orientation = LinearLayout.HORIZONTAL
        rowLayout.gravity = Gravity.CENTER

        val cardViews = mutableListOf<CardView>()

        (0 until columnsNumber).forEach {
            val cardView = CardView(context)
            cardView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                weight = 1f
                if (it != columnsNumber - 1) {
                    setMargins(0, 0, getHorizontalMargin(), 0)
                }
            }

            cardViews.add(cardView)
            rowLayout.addView(cardView)
        }

        return Pair(rowLayout, cardViews)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val rowHeight = h / childCount
        (0 until childCount).forEach {
            val rowLayout = getChildAt(it) as LinearLayout
            rowLayout.layoutParams.height = rowHeight
        }
    }

    fun setBoardData(board: Board) {
        if (board.cards.size != cardViews.size) {
            // fail fast in case of an error
            throw IllegalArgumentException("number of cards on the board do not match spots on the board")
        }

        (0 until board.cards.size).forEach { i ->
            cardViews[i].setData(if (board.isFlipped(i)) board.cards[i].front else board.back)
        }
    }

    fun setCardClickListener(listener: CardClickListener?) {
        cardViews.forEachIndexed { index, cardView ->
            cardView.setCardClickListener(index, listener)
        }
    }

    fun flip(position: Int, imageSpec: ImageSpec, endAction: (() -> Unit)?) {
        cardViews[position].flip(image = imageSpec, endAction = endAction)
    }

}

interface CardClickListener {
    fun onCardClicked(pos: Int)
}
