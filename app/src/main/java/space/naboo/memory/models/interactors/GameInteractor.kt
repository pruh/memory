package space.naboo.memory.models.interactors

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import space.naboo.memory.image.ImageSpec
import space.naboo.memory.models.images.ImageLoader
import space.naboo.memory.models.repositories.ImagesRepository
import java.util.concurrent.Executors

interface GameInteractor {
    fun getImages(count: Int, width: Int, height: Int): Single<List<LoadedImage>>
    fun getImages(urls: List<String>, width: Int, height: Int): Single<List<LoadedImage>>
}

class GameInteractorImpl(private val imageLoader: ImageLoader,
        private val repository: ImagesRepository) : GameInteractor {

    override fun getImages(count: Int, width: Int, height: Int): Single<List<LoadedImage>> {
        return repository.search(count)
                .flatMap { loadBitmaps(it, width, height) }
    }

    override fun getImages(urls: List<String>, width: Int, height: Int): Single<List<LoadedImage>> {
        return loadBitmaps(urls, width, height)
    }

    /**
     * Loads image and caches it. Further requests to the same URL will get image from cache.
     */
    private fun loadBitmaps(photoUrls: List<String>, width: Int, height: Int): Single<List<LoadedImage>> {
        // load with 3 parallel thread
        val executor = Executors.newFixedThreadPool(3)
        val scheduler = Schedulers.from(executor)

        return Observable.fromCallable { photoUrls }
                .flatMapIterable { it }
                .flatMap {
                    Observable.fromCallable { it }
                            .map { LoadedImage(it, imageLoader.loadImage(it, width, height)) }
                            .subscribeOn(scheduler)
                }
                .toList()
    }
}

data class LoadedImage(val url: String, val imageSpec: ImageSpec)
