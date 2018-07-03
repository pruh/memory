package space.naboo.memory.extensions

import android.content.res.TypedArray
import org.json.JSONArray
import org.json.JSONObject

fun <T> fastLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

inline fun TypedArray.use(block: (TypedArray) -> Unit) {
    try {
        block(this)
    } finally {
        recycle()
    }
}

inline fun <R> JSONArray.map(transform: (JSONObject) -> R): List<R> = mapTo(ArrayList(), transform)

inline fun <R> JSONArray.mapTo(destination: ArrayList<R>, transform: (JSONObject) -> R): List<R> {
    forEach { destination.add(transform(it)) }
    return destination
}

inline fun JSONArray.forEach(action: (JSONObject) -> Unit) {
    (0 until length()).forEach { action(getJSONObject(it)) }
}

inline fun JSONArray.filter(predicate: (JSONObject) -> Boolean): List<JSONObject> =
        filterTo(ArrayList(), predicate)

inline fun JSONArray.filterTo(destination: ArrayList<JSONObject>, predicate: (JSONObject) -> Boolean): List<JSONObject> {
    forEach { if (predicate(it)) destination.add(it) }
    return destination
}