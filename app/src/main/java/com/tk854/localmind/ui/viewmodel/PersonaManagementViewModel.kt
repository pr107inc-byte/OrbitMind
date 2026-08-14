package com.tk854.localmind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tk854.localmind.domain.model.Persona
import com.tk854.localmind.data.repository.PersonaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonaManagementViewModel @Inject constructor(
    private val personaRepository: PersonaRepository
) : ViewModel() {

    // Only custom (user-saved) personas â€” built-in ones are in Persona.FEATURED_PERSONAS
    val personas = personaRepository.getAllPersonas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun savePersona(persona: Persona) {
        viewModelScope.launch {
            personaRepository.savePersona(persona)
        }
    }

    fun deletePersona(persona: Persona) {
        viewModelScope.launch {
            personaRepository.deletePersona(persona)
        }
    }
}
