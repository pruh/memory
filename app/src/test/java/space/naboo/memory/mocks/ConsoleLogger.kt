package space.naboo.memory.mocks

import space.naboo.memory.logger.Logger

class ConsoleLogger : Logger {
    override fun logv(tag: String, message: String, t: Throwable?) {
        val error = t?.let { " $it" } ?: ""
        println("V/$tag $message$error")
    }

    override fun logd(tag: String, message: String, t: Throwable?) {
        val error = t?.let { " $it" } ?: ""
        println("D/$tag $message$error")
    }

    override fun logi(tag: String, message: String, t: Throwable?) {
        val error = t?.let { " $it" } ?: ""
        println("I/$tag $message$error")
    }

    override fun logw(tag: String, message: String, t: Throwable?) {
        val error = t?.let { " $it" } ?: ""
        println("W/$tag $message$error")
    }

    override fun loge(tag: String, message: String, t: Throwable?) {
        val error = t?.let { " $it" } ?: ""
        println("E/$tag $message$error")
    }

}
