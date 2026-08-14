package com.tk854.localmind.domain.usecase

import com.tk854.localmind.domain.model.Model
import com.tk854.localmind.data.repository.ModelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDownloadedModelsUseCase @Inject constructor(
    private val modelRepository: ModelRepository
) {
    operator fun invoke(): Flow<List<Model>> {
        return modelRepository.getAllModels()
    }
}
