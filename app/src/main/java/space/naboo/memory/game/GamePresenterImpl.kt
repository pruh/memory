package space.naboo.memory.game

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import space.naboo.memory.image.ImageSpec
import space.naboo.memory.logger.Logger
import space.naboo.memory.models.Board
import space.naboo.memory.models.BoardSize
import space.naboo.memory.models.Card
import space.naboo.memory.models.interactors.GameInteractor
import space.naboo.memory.models.interactors.LoadedImage
import space.naboo.memory.serializer.StateSerializer
import space.naboo.memory.serializer.Storage
import space.naboo.memory.states.GameState
import java.util.concurrent.TimeUnit

class GamePresenterImpl(
        private val view: GameContract.View,
        private val cardsBack: ImageSpec,
        private val interactor: GameInteractor,
        private val logger: Logger,
        private val stateSerializer: StateSerializer<GameState, Storage>,
        private val boardSize: BoardSize
) : GameContract.Presenter {

    private lateinit var state: GameState

    private val tag = GamePresenterImpl::class.java.simpleName

    private var cardLoadDisposable: Disposable? = null
    private var flipBackDisposable: Disposable? = null

    override fun restartGame(viewWidth: Int, viewHeight: Int) {
        if (cardLoadDisposable?.isDisposed == false) {
            logger.logd(tag, "game is already restarting")
            return
        }

        logger.logd(tag, "restarting game")
        val imagesToRequest = boardSize.getTotal() / 2
        val imageWidth = viewWidth / boardSize.columns
        val imageHeight = viewHeight / boardSize.rows
        cardLoadDisposable = interactor.getImages(imagesToRequest, imageWidth, imageHeight)
                .map { createdShuffledBoard(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.showLoading() }
                .subscribe({
                    logger.logd(tag, "board created: $it")

                    state = GameState(it)
                    view.displayBoard(it)
                    view.displayMoves(state.moves)
                }, {
                    logger.logw(tag, "exception while requesting images", it)
                    view.displayError(it)
                })
    }

    private fun createdShuffledBoard(loadedImages: List<LoadedImage>): Board {
        return (0 until loadedImages.size * 2)
                .map { Pair(Card(it, loadedImages[it / 2].imageSpec), loadedImages[it / 2].url) }
                .shuffled()
                .let {
                    val cards = it.map { it.first }
                    val urls = it.map { it.second }

                    Board(cards, cardsBack, urls)
                }
    }

    override fun onCardClicked(position: Int) {
        logger.logd(tag, "card clicked ${state.board.cards[position]}")

        val board = state.board
        if (board.flipped.contains(position)) {
            logger.logd(tag, "clicked already flipped card")
            return
        }
        if (board.candidate1Pos == position) {
            logger.logd(tag, "clicked candidate card")
            return
        }
        if (board.candidate2Pos != -1) {
            logger.logd(tag, "waiting for cards to flip back")
            return
        }

        if (board.candidate1Pos != -1) {
            board.candidate2Pos = position

            view.displayMoves(++state.moves)

            view.flip(board.candidate2Pos, board.cards[board.candidate2Pos].front) {
                if (board.isMatch(board.candidate1Pos, board.candidate2Pos)) {
                    logger.logd(tag, "clicked cards match")
                    board.flipped.add(board.candidate1Pos)
                    board.flipped.add(board.candidate2Pos)

                    board.candidate1Pos = -1
                    board.candidate2Pos = -1
                } else {
                    logger.logd(tag, "clicked cards do not match")

                    setDelayedFlipBack(board)
                }
            }
        } else {
            logger.logd(tag, "new candidate ${board.cards[position]}")
            view.flip(position, board.cards[position].front)
            board.candidate1Pos = position
        }
    }

    override fun saveState(storage: Storage) {
        if (::state.isInitialized) {
            stateSerializer.serialize(state, storage)
        }
    }

    override fun restoreGame(storage: Storage, viewWidth: Int, viewHeight: Int) {
        val imagesToRequest = boardSize.getTotal() / 2
        val imageWidth = viewWidth / boardSize.columns
        val imageHeight = viewHeight / boardSize.rows

        stateSerializer.deserialize(storage, imageWidth, imageHeight)
                .switchIfEmpty(interactor.getImages(imagesToRequest, imageWidth, imageHeight)
                        .map { createdShuffledBoard(it) }
                        .map { GameState(it) })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.showLoading() }
                .subscribe({
                    logger.logd(tag, "board created: ${it.board}")

                    state = it

                    if (it.board.candidate2Pos != -1) {
                        setDelayedFlipBack(it.board)
                    }

                    view.displayBoard(it.board)
                    view.displayMoves(it.moves)
                }, {
                    logger.loge(tag, "error on restoring game", it)
                    view.displayError(it)
                })
    }

    private fun setDelayedFlipBack(board: Board) {
        flipBackDisposable = Observable.timer(500, TimeUnit.MILLISECONDS)
                .map {
                    view.flip(board.candidate1Pos, board.back)
                    view.flip(board.candidate2Pos, board.back)

                    board.candidate1Pos = -1
                    board.candidate2Pos = -1
                }
                .subscribe()
    }

    override fun destroy() {
        cardLoadDisposable?.dispose()
        flipBackDisposable?.dispose()
    }

}
