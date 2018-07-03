package space.naboo.memory.serializer

import android.os.Bundle
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import space.naboo.memory.image.ImageSpec
import space.naboo.memory.models.Board
import space.naboo.memory.models.Card
import space.naboo.memory.models.interactors.GameInteractor
import space.naboo.memory.states.GameState

interface StateSerializer<T1, T2: Storage> {
    fun serialize(state: T1, storage: T2)
    fun deserialize(storage: T2, imageWidth: Int, imageHeight: Int): Maybe<T1>
}

class GameStateSerializer(private val interactor: GameInteractor,
        private val cardsBack: ImageSpec) : StateSerializer<GameState, Storage> {

    companion object {
        private const val CARDS_KEY = "CARDS_KEY"
        private const val URLS_KEY = "URLS_KEY"
        private const val FLIPPED_KEY = "FLIPPED_KEY"
        private const val CANDIDATE1_KEY = "CANDIDATE1_KEY"
        private const val CANDIDATE2_KEY = "CANDIDATE2_KEY"
        private const val MOVES_KEY = "MOVES_KEY"
    }

    override fun serialize(state: GameState, storage: Storage) {
        storage.storeIntList(CARDS_KEY, state.board.cards.map { it.uniqueId })
        storage.storeStringList(URLS_KEY, state.board.cardsUrls)
        storage.storeIntList(FLIPPED_KEY, state.board.flipped.toList())
        storage.storeInt(CANDIDATE1_KEY, state.board.candidate1Pos)
        storage.storeInt(CANDIDATE2_KEY, state.board.candidate2Pos)
        storage.storeInt(MOVES_KEY, state.moves)
    }

    override fun deserialize(storage: Storage, imageWidth: Int, imageHeight: Int): Maybe<GameState> {
        return if (storage.containsKey(CARDS_KEY)) {
            deserializeExisting(storage, imageWidth, imageHeight)
        } else {
            Maybe.empty()
        }
    }

    private fun deserializeExisting(storage: Storage, imageWidth: Int, imageHeight: Int): Maybe<GameState> {
        val ids = storage.getIntList(CARDS_KEY)
        val urls = storage.getStringList(URLS_KEY)
        val flipped = storage.getIntList(FLIPPED_KEY).toMutableSet()
        val candidate1Pos = storage.getInt(CANDIDATE1_KEY)
        val candidate2Pos = storage.getInt(CANDIDATE2_KEY)
        val moves = storage.getInt(MOVES_KEY)

        return interactor.getImages(urls, imageWidth, imageHeight)
                .toMaybe()
                .subscribeOn(Schedulers.io())
                .map { loadedImages ->
                    // loaded images will be in random order, so we need to remember url to id
                    val map = loadedImages.associateBy({ it.url }, { it })
                    (0 until ids.size)
                            .map {
                                val url = urls[it]
                                val loadedImage = map[url]
                                        ?: throw IllegalStateException("cannot find loaded image for url: $url")

                                Pair(Card(ids[it], loadedImage.imageSpec), loadedImage.url)
                            }
                }
                .map {
                    val board = Board(
                            cards = it.map { it.first },
                            back = cardsBack,
                            cardsUrls = it.map { it.second },
                            flipped = flipped,
                            candidate1Pos = candidate1Pos,
                            candidate2Pos = candidate2Pos
                    )
                    GameState(
                            board = board,
                            moves = moves
                    )
                }
                .subscribeOn(AndroidSchedulers.mainThread())
    }
}

interface Storage {
    fun containsKey(key: String): Boolean

    fun getIntList(key: String): List<Int>
    fun storeIntList(key: String, list: List<Int>)
    fun getStringList(key: String): List<String>
    fun storeStringList(key: String, list: List<String>)
    fun getIntToIntMap(key: String): Map<Int, Int>
    fun storeIntToIntMap(key: String, map: Map<Int, Int>)
    fun getInt(key: String): Int
    fun storeInt(key: String, value: Int)
    fun getBoolean(key: String): Boolean
    fun storeBoolean(key: String, value: Boolean)

}

class BundleStorage(private val bundle: Bundle) : Storage {
    override fun containsKey(key: String): Boolean {
        return bundle.containsKey(key)
    }

    override fun getIntList(key: String): List<Int> {
        return bundle.getIntegerArrayList(key)
    }

    override fun storeIntList(key: String, list: List<Int>) {
        if (list is ArrayList) {
            bundle.putIntegerArrayList(key, list)
        } else {
            bundle.putIntegerArrayList(key, ArrayList(list))
        }
    }

    override fun getStringList(key: String): List<String> {
        return bundle.getStringArrayList(key)
    }

    override fun storeStringList(key: String, list: List<String>) {
        if (list is ArrayList) {
            bundle.putStringArrayList(key, list)
        } else {
            bundle.putStringArrayList(key, ArrayList(list))
        }
    }

    override fun getIntToIntMap(key: String): Map<Int, Int> {
        return getIntList(key).zipWithNext().toMap()
    }

    override fun storeIntToIntMap(key: String, map: Map<Int, Int>) {
        storeIntList(key, map.flatMap { listOf(it.key, it.value) })
    }

    override fun getInt(key: String): Int {
        return bundle.getInt(key)
    }

    override fun storeInt(key: String, value: Int) {
        bundle.putInt(key, value)
    }

    override fun getBoolean(key: String): Boolean {
        return bundle.getBoolean(key)
    }

    override fun storeBoolean(key: String, value: Boolean) {
        bundle.putBoolean(key, value)
    }

}
