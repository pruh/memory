package space.naboo.memory.mocks

import space.naboo.memory.serializer.Storage

open class MapStorage : Storage {
    private val map = mutableMapOf<String, Any>()

    override fun containsKey(key: String): Boolean {
        return map.containsKey(key)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getIntList(key: String): List<Int> {
        return map[key] as List<Int>
    }

    override fun storeIntList(key: String, list: List<Int>) {
        map[key] = list
    }

    @Suppress("UNCHECKED_CAST")
    override fun getStringList(key: String): List<String> {
        return map[key] as List<String>
    }

    override fun storeStringList(key: String, list: List<String>) {
        map[key] = list
    }

    @Suppress("UNCHECKED_CAST")
    override fun getIntToIntMap(key: String): Map<Int, Int> {
        return map[key] as Map<Int, Int>
    }

    override fun storeIntToIntMap(key: String, map: Map<Int, Int>) {
        this@MapStorage.map[key] = map
    }

    override fun getInt(key: String): Int {
        return map[key] as Int
    }

    override fun storeInt(key: String, value: Int) {
        map[key] = value
    }

    override fun getBoolean(key: String): Boolean {
        return map[key] as Boolean
    }

    override fun storeBoolean(key: String, value: Boolean) {
        map[key] = value
    }

}
