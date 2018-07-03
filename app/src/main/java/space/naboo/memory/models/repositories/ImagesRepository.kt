package space.naboo.memory.models.repositories

import io.reactivex.Single

interface ImagesRepository {
    /**
     * Returns list of URLs.
     */
    fun search(count: Int): Single<List<String>>
}