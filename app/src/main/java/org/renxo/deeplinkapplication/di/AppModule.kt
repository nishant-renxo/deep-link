package org.renxo.deeplinkapplication.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.renxo.deeplinkapplication.networking.ApiHelper
import org.renxo.deeplinkapplication.networking.ApiRepository
import org.renxo.deeplinkapplication.utils.ContactInfo
//import java.util.logging.Logger
import javax.inject.Singleton
import io.ktor.client.plugins.logging.Logger



@Module
@InstallIn(ViewModelComponent::class)
object AppModuleForViewModels {
    @ViewModelScoped
    @Provides
    fun provideContactInfo(@ApplicationContext context: Context): ContactInfo {
        return ContactInfo(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideHttpClientEngine(): HttpClientEngine {
        return OkHttp.create()
    }


    @Singleton
    @Provides
    fun provideHttpClient(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        when {
                            message.contains("REQUEST") -> Log.i("🔵 HTTP_REQUEST", message)
                            message.contains("RESPONSE") -> Log.i("🟢 HTTP_RESPONSE", message)
                            message.contains("BODY") -> Log.d("📦 HTTP_BODY", message)
                            message.contains("Authorization") -> Log.w("🔑 AUTH_TOKEN", message)
                            else -> Log.d("HTTP_LOG", message)
                        }
                    }
                }
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(json = Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }

        }
    }

    @Singleton
    @Provides
    fun provideApiRepository(client: HttpClient): ApiRepository {
        return ApiRepository(ApiHelper(client))
    }


}