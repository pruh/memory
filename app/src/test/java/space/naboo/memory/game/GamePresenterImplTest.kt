package space.naboo.memory.game

import io.reactivex.Single
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext
import org.koin.standalone.StandAloneContext.closeKoin
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Captor
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import space.naboo.memory.extensions.any
import space.naboo.memory.extensions.capture
import space.naboo.memory.logger.Logger
import space.naboo.memory.mocks.ConsoleLogger
import space.naboo.memory.mocks.MapStorage
import space.naboo.memory.mocks.MockBoardSize
import space.naboo.memory.mocks.MockImageSpec
import space.naboo.memory.models.Board
import space.naboo.memory.models.BoardSize
import space.naboo.memory.models.interactors.GameInteractor
import space.naboo.memory.models.interactors.LoadedImage
import space.naboo.memory.rules.RxImmediateSchedulerRule
import space.naboo.memory.serializer.GameStateSerializer
import space.naboo.memory.serializer.StateSerializer
import space.naboo.memory.serializer.Storage
import space.naboo.memory.states.GameState

class GamePresenterImplTest : KoinTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    @Rule
    @JvmField
    val mockitoRule = MockitoJUnit.rule()!!

    @Captor
    private lateinit var boardCaptor: ArgumentCaptor<Board>
    @Captor
    private lateinit var movesCaptor: ArgumentCaptor<Int>

    private val presenter by inject<GameContract.Presenter>()
    private val view by inject<GameContract.View>()
    private val interactor by inject<GameInteractor>()
    private val boardSize by inject<BoardSize>()

    @Before
    fun before() {
        startKoin(listOf(gamePresenterTestModule))
    }

    @After
    fun after() {
        closeKoin()
    }

    @Test
    fun testRestartGame() {
        val boardSize = boardSize.columns * boardSize.rows
        val loadedImages: List<LoadedImage> = createLoadedImages(boardSize / 2)

        `when`(interactor.getImages(eq(boardSize / 2), anyInt(), anyInt()))
                .thenReturn(Single.just(loadedImages))

        presenter.restartGame(500, 500)

        verify(view).displayBoard(any())
        verify(view).displayMoves(0)
    }

    @Test
    fun testBoardCorrect() {
        val boardSize = boardSize.columns * boardSize.rows
        val loadedImages: List<LoadedImage> = createLoadedImages(boardSize / 2)

        `when`(interactor.getImages(eq(boardSize / 2), anyInt(), anyInt()))
                .thenReturn(Single.just(loadedImages))

        presenter.restartGame(500, 500)

        verify(view).displayBoard(capture(boardCaptor))

        val board = boardCaptor.value
        assertEquals("board size is incorrect", boardSize, board.cards.size)

        assertTrue("some cards are flipped", board.flipped.isEmpty() || board.candidate1Pos != -1 || board.candidate2Pos != -1)

        // test all cards have matches
        val cardsMap = board.cards.associateByTo(mutableMapOf(), { it.uniqueId }, { it })
        board.cards.forEach {
            // check images and ids are correct
            val pairId = if (it.uniqueId % 2 == 0) it.uniqueId + 1 else it.uniqueId - 1

            assertTrue("no pair found for ${it.uniqueId}", cardsMap.containsKey(it.uniqueId))
            assertTrue("no pair found for $pairId", cardsMap.containsKey(pairId))

            val card1 = cardsMap[it.uniqueId]
            val card2 = cardsMap[pairId]

            assertSame("images are not the same", card1?.front, card2?.front)
        }
    }

    @Test
    fun testBoardShuffled() {
        val boardSize = boardSize.columns * boardSize.rows
        val loadedImages: List<LoadedImage> = createLoadedImages(boardSize / 2)

        `when`(interactor.getImages(eq(boardSize / 2), anyInt(), anyInt()))
                .thenReturn(Single.just(loadedImages))

        presenter.restartGame(500, 500)

        verify(view).displayBoard(capture(boardCaptor))

        val board = boardCaptor.value

        var shuffled = false
        board.cards.forEachIndexed { i, card ->
            if (card.uniqueId != i) {
                shuffled = true
                return@forEachIndexed
            }
        }

        assertTrue("board is not shuffled", shuffled)
    }

    @Test
    fun testCardFlipped() {
        var lastEndAction: (() -> Unit)? = null
        `when`(view.flip(anyInt(), any(), any())).thenAnswer {
            @Suppress("UNCHECKED_CAST")
            lastEndAction = it.arguments[2] as? (() -> Unit)
            null
        }

        val boardSize = boardSize.columns * boardSize.rows
        val loadedImages: List<LoadedImage> = createLoadedImages(boardSize / 2)

        `when`(interactor.getImages(eq(boardSize / 2), anyInt(), anyInt()))
                .thenReturn(Single.just(loadedImages))

        presenter.restartGame(500, 500)

        verify(view).displayBoard(capture(boardCaptor))

        val board = boardCaptor.value

        val card1 = Pair(board.cards[0], 0)
        // pos1 and pos2 do not match
        val card2 = board.cards
                .mapIndexed { index, card -> Pair(card, index) }
                .first {
                    it.first.uniqueId != board.cards[card1.second].uniqueId
                            && it.first.uniqueId / 2 != board.cards[card1.second].uniqueId / 2
                }
        // pos1 and pos3 match
        val card3 = board.cards
                .mapIndexed { index, card -> Pair(card, index) }
                .first {
                    it.first.uniqueId != board.cards[card1.second].uniqueId
                            && it.first.uniqueId / 2 == board.cards[card1.second].uniqueId / 2
                }

        presenter.onCardClicked(card1.second)
        verify(view, times(1)).flip(eq(card1.second), any(), any())
        assertTrue("card at pos ${card1.second} is not flipped ${card1.first}", board.isFlipped(card1.second))

        assertTrue(board.flipped.isEmpty())
        assertTrue(board.candidate1Pos == card1.second)
        assertTrue(board.candidate2Pos == -1)

        // click the same card
        presenter.onCardClicked(card1.second)
        verify(view, times(1)).flip(eq(card1.second), any(), any())
        assertTrue("card at pos ${card1.second} is not flipped ${card1.first}", board.isFlipped(card1.second))

        assertTrue(board.flipped.isEmpty())
        assertTrue(board.candidate1Pos == card1.second)
        assertTrue(board.candidate2Pos == -1)

        // click not matching card
        presenter.onCardClicked(card2.second)
        verify(view, times(1)).flip(eq(card2.second), any(), any())
        assertTrue("card at pos ${card2.second} is not flipped ${card2.first}", board.isFlipped(card2.second))

        lastEndAction?.invoke()

        assertTrue(board.flipped.isEmpty())
        assertTrue(board.candidate1Pos == -1)
        assertTrue(board.candidate2Pos == -1)

        presenter.onCardClicked(card1.second)
        verify(view, times(3)).flip(eq(card1.second), any(), any())
        assertTrue("card at pos ${card1.second} is not flipped ${card1.first}", board.isFlipped(card1.second))

        assertTrue(board.flipped.isEmpty())
        assertTrue(board.candidate1Pos == card1.second)
        assertTrue(board.candidate2Pos == -1)

        // click matching card
        presenter.onCardClicked(card3.second)
        verify(view, times(1)).flip(eq(card3.second), any(), any())
        assertTrue("card at pos ${card3.second} is not flipped ${card3.first}", board.isFlipped(card3.second))

        lastEndAction?.invoke()

        assertTrue(board.flipped.size == 2)
        assertTrue(board.flipped.contains(card1.second))
        assertTrue(board.flipped.contains(card3.second))
        assertTrue(board.candidate1Pos == -1)
        assertTrue(board.candidate2Pos == -1)
    }

    @Test
    fun testSaveRestore() {
        val boardSize = boardSize.columns * boardSize.rows
        val loadedImages: List<LoadedImage> = createLoadedImages(boardSize / 2)

        `when`(interactor.getImages(eq(boardSize / 2), anyInt(), anyInt()))
                .thenReturn(Single.just(loadedImages))
        `when`(interactor.getImages(any<List<String>>(), anyInt(), anyInt()))
                .thenReturn(Single.just(loadedImages))

        val storage = spy(MapStorage())

        // save/restore before restart
        presenter.saveState(storage)
        verify(storage, never()).storeIntList(anyString(), any())

        presenter.restoreGame(storage, 500, 500)
        verify(storage, times(1)).containsKey(anyString())
        verify(storage, never()).getIntList(anyString())

        verify(view, times(1)).displayBoard(capture(boardCaptor))
        val board1 = boardCaptor.value

        // save/restore after restart
        presenter.restartGame(500, 500)
        presenter.saveState(storage)
        verify(storage, atLeastOnce()).storeIntList(anyString(), any())

        presenter.restoreGame(storage, 500, 500)
        verify(storage, atLeastOnce()).containsKey(anyString())
        verify(storage, atLeastOnce()).getIntList(anyString())

        // 3 times = 1 restart + 2 restore
        verify(view, times(3)).displayBoard(capture(boardCaptor))
        val board2 = boardCaptor.value

        assertNotSame(board1, board2)
    }

    @Test
    fun testMovesUpdates() {
        var lastEndAction: (() -> Unit)? = null
        `when`(view.flip(anyInt(), any(), any())).thenAnswer {
            @Suppress("UNCHECKED_CAST")
            lastEndAction = it.arguments[2] as? (() -> Unit)
            null
        }

        val boardSize = boardSize.columns * boardSize.rows
        val loadedImages: List<LoadedImage> = createLoadedImages(boardSize / 2)

        `when`(interactor.getImages(eq(boardSize / 2), anyInt(), anyInt()))
                .thenReturn(Single.just(loadedImages))

        presenter.restartGame(500, 500)

        verify(view).displayBoard(capture(boardCaptor))

        val board = boardCaptor.value

        val card1 = Pair(board.cards[0], 0)
        // pos1 and pos2 do not match
        val card2 = board.cards
                .mapIndexed { index, card -> Pair(card, index) }
                .first {
                    it.first.uniqueId != board.cards[card1.second].uniqueId
                            && it.first.uniqueId / 2 != board.cards[card1.second].uniqueId / 2
                }
        // pos1 and pos3 match
        val card3 = board.cards
                .mapIndexed { index, card -> Pair(card, index) }
                .first {
                    it.first.uniqueId != board.cards[card1.second].uniqueId
                            && it.first.uniqueId / 2 == board.cards[card1.second].uniqueId / 2
                }

        presenter.onCardClicked(card1.second)
        verify(view, times(1)).displayMoves(capture(movesCaptor))
        assertEquals("moves are not correct", 0, movesCaptor.value)

        // click the same card
        presenter.onCardClicked(card1.second)
        verify(view, times(1)).displayMoves(capture(movesCaptor))
        assertEquals("moves are not correct", 0, movesCaptor.value)

        // click not matching card
        presenter.onCardClicked(card2.second)
        verify(view, times(2)).displayMoves(capture(movesCaptor))
        assertEquals("moves are not correct", 1, movesCaptor.value)

        lastEndAction?.invoke()

        presenter.onCardClicked(card1.second)
        verify(view, times(2)).displayMoves(capture(movesCaptor))
        assertEquals("moves are not correct", 1, movesCaptor.value)

        // click matching card
        presenter.onCardClicked(card3.second)
        verify(view, times(3)).displayMoves(capture(movesCaptor))
        assertEquals("moves are not correct", 2, movesCaptor.value)
    }

    private fun createLoadedImages(count: Int): List<LoadedImage> {
        return (0 until count)
                .map { LoadedImage(it.toString(), MockImageSpec(it.toString())) }
    }
}

val gamePresenterTestModule: Module = applicationContext {
    val backImage = MockImageSpec("back")

    factory { GamePresenterImpl(get(), backImage, get(), get(), get(), get()) as GameContract.Presenter }

    factory { GameStateSerializer(get(), backImage) as StateSerializer<GameState, Storage> }

    bean { mock(GameInteractor::class.java) as GameInteractor }

    factory { MockBoardSize(5, 4) as BoardSize }

    bean { mock(GameContract.View::class.java) as GameContract.View }

    bean { ConsoleLogger() as Logger }
}
