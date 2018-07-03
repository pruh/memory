package space.naboo.memory.models

import android.content.Context
import space.naboo.memory.R

interface BoardSize {
    val columns: Int
    val rows: Int

    fun getTotal(): Int {
        return columns * rows
    }
}

class ResourceBoardSize(context: Context) : BoardSize {

    override val columns: Int = context.resources.getInteger(R.integer.columns)
    override val rows: Int = context.resources.getInteger(R.integer.rows)

}
