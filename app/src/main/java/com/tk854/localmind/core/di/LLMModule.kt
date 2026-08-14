package com.tk854.localmind.core.di

import com.tk854.localmind.llm.nativelib.LlamaCppBridge
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LLMModule {

    @Provides
    @Singleton
    fun provideLlamaCppBridge(): LlamaCppBridge {
        return LlamaCppBridge()
    }
}
