package com.example.pruebaapi1rjo.di

import android.content.Context
import androidx.room.Room
import com.example.pruebaapi1rjo.data.local.PokemonDatabase
import com.example.pruebaapi1rjo.data.remote.PokemonApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePokemonDatabase(@ApplicationContext context: Context): PokemonDatabase {
        return Room.databaseBuilder(
            context,
            PokemonDatabase::class.java,
            "pokemon.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePokemonDao(database: PokemonDatabase) = database.dao

    @Provides
    @Singleton
    fun providePokemonApi(@ApplicationContext context: Context): PokemonApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS // Más rápido que BODY
        }
        
        // Caché de 10MB
        val cache = okhttp3.Cache(context.cacheDir, 10 * 1024 * 1024)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .cache(cache)
            .dispatcher(okhttp3.Dispatcher().apply {
                maxRequests = 50
                maxRequestsPerHost = 50 // Permite cargar los 20 detalles en paralelo real
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(PokemonApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PokemonApi::class.java)
    }
}
