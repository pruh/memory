package space.naboo.memory.models

import space.naboo.memory.image.ImageSpec

data class Board(
        val cards: List<Card>,
        val back: ImageSpec,
        val cardsUrls: List<String>,
        /** Positions of flipped card */
        val flipped: MutableSet<Int> = mutableSetOf(),
        /** Position of first candidate, -1 if no set. */
        var candidate1Pos: Int = -1,
        /** Position of second candidate, -1 if no set. */
        var candidate2Pos: Int = -1
) {

    fun isMatch(pos1: Int, pos2: Int): Boolean {
        return cards[pos1].uniqueId != cards[pos2].uniqueId
                && cards[pos1].uniqueId / 2 == cards[pos2].uniqueId / 2
    }

    fun isFlipped(position: Int): Boolean {
        return flipped.contains(position)
                || candidate1Pos == position
                || candidate2Pos == position
    }
}
