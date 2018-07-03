package space.naboo.memory.mocks

import space.naboo.memory.logger.Logger

class VoidLogger : Logger {

    override fun logv(tag: String, message: String, t: Throwable?) {}
    override fun logd(tag: String, message: String, t: Throwable?) {}
    override fun logi(tag: String, message: String, t: Throwable?) {}
    override fun logw(tag: String, message: String, t: Throwable?) {}
    override fun loge(tag: String, message: String, t: Throwable?) {}

}
