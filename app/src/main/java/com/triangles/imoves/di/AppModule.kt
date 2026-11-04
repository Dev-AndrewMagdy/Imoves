package com.triangles.imoves.di

import com.triangles.data.AppBuildConfig
import com.triangles.imoves.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppBuildConfig(): AppBuildConfig = object : AppBuildConfig {
        override val isDebug: Boolean = BuildConfig.DEBUG
        override val baseApiUrl: String = BuildConfig.API_URL
    }


}
