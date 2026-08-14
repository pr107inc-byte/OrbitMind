package com.tk854.localmind.llm

import com.tk854.localmind.domain.model.ModelCatalogItem
import com.tk854.localmind.domain.model.ModelRunTarget
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRunTargetResolver @Inject constructor() {

    fun resolve(@Suppress("UNUSED_PARAMETER") model: ModelCatalogItem): ModelRunTarget = ModelRunTarget.LOCAL
}
