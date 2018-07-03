package space.naboo.memory.models.repositories

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import space.naboo.memory.BuildConfig
import space.naboo.memory.extensions.filter
import space.naboo.memory.extensions.map
import space.naboo.memory.logger.Logger
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.Executors

class ImagesRepositoryImpl(private val logger: Logger) : ImagesRepository {

    private val tag = ImagesRepositoryImpl::class.java.simpleName

    private val baseUrl = "https://api.flickr.com/services/rest/?api_key=${BuildConfig.FLICKR_API_KEY}&format=json&nojsoncallback=1"

    override fun search(count: Int): Single<List<String>> {
        // page is randomly generated to have random photos on refresh
        val page = Random().nextInt(100)
        return Single
                .fromCallable {
                    val url = "$baseUrl&method=flickr.photos.search&tags=kitten&page=$page&per_page=$count"
                    val resp = request(url)
                    JSONObject(resp)
                }
                .map { json ->
                    json.getJSONObject("photos").getJSONArray("photo")
                            .map { it.getString("id") }
                }
                .flatMap { getImageUrls(it) }
    }

    private fun getImageUrls(photoIds: List<String>): Single<List<String>> {
        // load with 3 parallel thread
        val executor = Executors.newFixedThreadPool(3)
        val scheduler = Schedulers.from(executor)

        return Observable.fromCallable { photoIds }
                .flatMapIterable { it }
                .flatMap {
                    Observable.fromCallable { it }
                            .map {
                                val url = "$baseUrl&method=flickr.photos.getSizes&photo_id=$it"
                                val resp = request(url)
                                JSONObject(resp)
                            }
                            .map {
                                it.getJSONObject("sizes").getJSONArray("size")
                                        .filter { it.getString("label") == "Medium" }
                                        .first()
                                        .getString("source")
                            }
                            .subscribeOn(scheduler)
                }
                .toList()
    }

    private fun request(url: String): String {
        URL(url).openConnection()?.let { it as HttpURLConnection
            it.requestMethod = "GET"
            it.readTimeout = 10000
            it.connectTimeout = 15000
            it.setRequestProperty("Content-Type", "application/json")

            return it.inputStream.bufferedReader().readText().also {
                logger.logd(tag, "Requesting $url\nResponse $it")
            }
        } ?: throw IOException("Can not get photos")
    }
}
