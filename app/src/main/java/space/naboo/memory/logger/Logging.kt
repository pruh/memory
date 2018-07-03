package space.naboo.memory.logger

import android.util.Log

interface Logger {
    fun logv(tag: String, message: String, t: Throwable? = null)
    fun logd(tag: String, message: String, t: Throwable? = null)
    fun logi(tag: String, message: String, t: Throwable? = null)
    fun logw(tag: String, message: String, t: Throwable? = null)
    fun loge(tag: String, message: String, t: Throwable? = null)
}

class AndroidLogger : Logger {
    override fun logv(tag: String, message: String, t: Throwable?) {
        t?.let {
            Log.v(tag, message, it)
        } ?: run {
            Log.v(tag, message)
        }
    }

    override fun logd(tag: String, message: String, t: Throwable?) {
        t?.let {
            Log.d(tag, message, it)
        } ?: run {
            Log.d(tag, message)
        }
    }

    override fun logi(tag: String, message: String, t: Throwable?) {
        t?.let {
            Log.i(tag, message, it)
        } ?: run {
            Log.i(tag, message)
        }
    }

    override fun logw(tag: String, message: String, t: Throwable?) {
        t?.let {
            Log.w(tag, message, it)
        } ?: run {
            Log.w(tag, message)
        }
    }

    override fun loge(tag: String, message: String, t: Throwable?) {
        t?.let {
            Log.e(tag, message, it)
        } ?: run {
            Log.e(tag, message)
        }
    }

}
