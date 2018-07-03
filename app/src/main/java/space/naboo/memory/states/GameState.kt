package space.naboo.memory.states

import space.naboo.memory.models.Board

data class GameState(
        val board: Board,
        var moves: Int = 0)
