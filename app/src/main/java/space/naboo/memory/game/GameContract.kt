package space.naboo.memory.game

import space.naboo.memory.image.ImageSpec
import space.naboo.memory.models.Board
import space.naboo.memory.models.images.ImageLoaderImpl
import space.naboo.memory.serializer.Storage

interface GameContract {

    interface View {
        fun displayBoard(board: Board)
        fun displayError(error: Throwable)
        fun getCardBackDrawable() : Int
        fun showLoading()
        fun getImageLoader(): ImageLoaderImpl
        fun flip(position: Int, imageSpec: ImageSpec, endAction: (() -> Unit)? = null)
        fun displayMoves(moves: Int)
    }

    interface Presenter {
        fun restartGame(viewWidth: Int, viewHeight: Int)
        fun onCardClicked(position: Int)
        fun restoreGame(storage: Storage, viewWidth: Int, viewHeight: Int)
        fun saveState(storage: Storage)
        fun destroy()
    }

}
