package space.naboo.memory

import android.app.Application
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.android.startKoin
import space.naboo.memory.di.gameModule
import space.naboo.memory.extensions.fastLazy
import space.naboo.memory.logger.AndroidLogger
import space.naboo.memory.logger.Logger

class MyApp : Application() {

    private val tag = MyApp::class.java.simpleName
    private val logger: Logger by fastLazy { AndroidLogger() }

    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(gameModule))

        RxJavaPlugins.setErrorHandler {
            logger.loge(tag, "Caught rx java exception", it)
        }
    }

}
