package space.naboo.memory.game

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import android.widget.TextView
import org.koin.android.ext.android.inject
import space.naboo.memory.R
import space.naboo.memory.customviews.BoardView
import space.naboo.memory.customviews.CardClickListener
import space.naboo.memory.di.Params.GAME_VIEW
import space.naboo.memory.extensions.fastLazy
import space.naboo.memory.image.ImageSpec
import space.naboo.memory.models.Board
import space.naboo.memory.models.images.ImageLoaderImpl
import space.naboo.memory.serializer.BundleStorage

class GameActivity : AppCompatActivity(), GameContract.View {

    private val presenter: GameContract.Presenter by inject { mapOf(GAME_VIEW to this) }

    private val boardView by fastLazy { findViewById<BoardView>(R.id.board_view) }
    private val movesView by fastLazy { findViewById<TextView>(R.id.moves) }
    private val progressView by fastLazy { findViewById<ProgressBar>(R.id.progress) }

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (savedInstanceState == null) {
            restartGame()
        } else {
            restoreGame(savedInstanceState)
        }

        boardView.setCardClickListener(object : CardClickListener {
            override fun onCardClicked(pos: Int) {
                presenter.onCardClicked(pos)
            }
        })
    }

    private fun restoreGame(savedInstanceState: Bundle) {
        if (boardView.measuredWidth == 0 || boardView.measuredHeight == 0) {
            boardView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (boardView.measuredWidth == 0 || boardView.measuredHeight == 0) {
                        return
                    }

                    boardView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    presenter.restoreGame(BundleStorage(savedInstanceState),
                            boardView.measuredWidth, boardView.measuredHeight)
                }

            })
        } else {
            presenter.restoreGame(BundleStorage(savedInstanceState),
                    boardView.measuredWidth, boardView.measuredHeight)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        presenter.saveState(BundleStorage(outState))
    }

    private fun restartGame() {
        if (boardView.measuredWidth == 0 || boardView.measuredHeight == 0) {
            boardView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (boardView.measuredWidth == 0 || boardView.measuredHeight == 0) {
                        return
                    }

                    boardView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    presenter.restartGame(boardView.measuredWidth, boardView.measuredHeight)
                }

            })
        } else {
            presenter.restartGame(boardView.measuredWidth, boardView.measuredHeight)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.restart -> {
            restartGame()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun displayBoard(board: Board) {
        boardView.setBoardData(board)

        boardView.visibility = View.VISIBLE
        movesView.visibility = View.VISIBLE
        progressView.visibility = View.INVISIBLE
    }


    override fun displayError(error: Throwable) {
        progressView.visibility = View.INVISIBLE

        dialog = AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(error.localizedMessage)
                .setPositiveButton(R.string.ok, null)
                .show()
    }

    override fun getCardBackDrawable(): Int {
        return R.drawable.ic_card_back
    }

    override fun showLoading() {
        progressView.visibility = View.VISIBLE
        boardView.visibility = View.INVISIBLE
        movesView.visibility = View.INVISIBLE
    }

    override fun flip(position: Int, imageSpec: ImageSpec, endAction: (() -> Unit)?) {
        boardView.flip(position, imageSpec, endAction)
    }

    override fun displayMoves(moves: Int) {
        movesView.text = getString(R.string.moves_made, moves)
    }

    override fun getImageLoader(): ImageLoaderImpl = ImageLoaderImpl(this)

    override fun onDestroy() {
        presenter.destroy()
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }

        super.onDestroy()
    }
}
