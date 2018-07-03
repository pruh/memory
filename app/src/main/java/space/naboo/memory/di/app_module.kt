package space.naboo.memory.di

import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext
import space.naboo.memory.R
import space.naboo.memory.di.Params.GAME_VIEW
import space.naboo.memory.game.GameContract
import space.naboo.memory.game.GamePresenterImpl
import space.naboo.memory.image.ResourceImageSpec
import space.naboo.memory.logger.AndroidLogger
import space.naboo.memory.logger.Logger
import space.naboo.memory.models.BoardSize
import space.naboo.memory.models.ResourceBoardSize
import space.naboo.memory.models.images.ImageLoader
import space.naboo.memory.models.images.ImageLoaderImpl
import space.naboo.memory.models.interactors.GameInteractor
import space.naboo.memory.models.interactors.GameInteractorImpl
import space.naboo.memory.models.repositories.ImagesRepository
import space.naboo.memory.models.repositories.ImagesRepositoryImpl
import space.naboo.memory.serializer.GameStateSerializer
import space.naboo.memory.serializer.StateSerializer
import space.naboo.memory.serializer.Storage
import space.naboo.memory.states.GameState

val gameModule: Module = applicationContext {
    val backImage = ResourceImageSpec(R.drawable.ic_card_back)

    factory { params -> GamePresenterImpl(params[GAME_VIEW],
            backImage, get(), get(), get(), get()) as GameContract.Presenter }

    factory { GameInteractorImpl(get(), get()) as GameInteractor }
    factory { ImageLoaderImpl(get()) as ImageLoader }

    factory { GameStateSerializer(get(), backImage) as StateSerializer<GameState, Storage> }

    factory { ResourceBoardSize(get()) as BoardSize }

    factory { ImagesRepositoryImpl(get()) as ImagesRepository }

    bean { AndroidLogger() as Logger }
}

object Params {
    const val GAME_VIEW = "GAME_VIEW"
}
