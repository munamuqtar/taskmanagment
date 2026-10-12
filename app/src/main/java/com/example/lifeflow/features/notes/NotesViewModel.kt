package com.example.lifeflow.features.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeflow.core.common.Result
import com.example.lifeflow.data.repository.NoteRepositoryImpl
import com.example.lifeflow.domain.model.Note
import com.example.lifeflow.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotesUiState(
    val isLoading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class NotesViewModel(
    private val noteRepository: NoteRepository = NoteRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            noteRepository.getNotes().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            notes = result.data,
                            filteredNotes = result.data
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    Result.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun searchNotes(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredNotes = if (query.isBlank()) {
                _uiState.value.notes
            } else {
                _uiState.value.notes.filter {
                    it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
                }
            }
        )
    }

    fun createNote(title: String, content: String, color: String, onSuccess: () -> Unit) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            val note = Note(title = title, content = content, color = color)
            when (noteRepository.createNote(note)) {
                is Result.Success -> {
                    onSuccess()
                    loadNotes()
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun updateNote(note: Note, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (noteRepository.updateNote(note)) {
                is Result.Success -> {
                    onSuccess()
                    loadNotes()
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            when (noteRepository.togglePin(note.id, !note.pinned)) {
                is Result.Success -> loadNotes()
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }

    fun deleteNote(noteId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (noteRepository.deleteNote(noteId)) {
                is Result.Success -> {
                    onSuccess()
                    loadNotes()
                }
                is Result.Error -> {}
                Result.Loading -> {}
            }
        }
    }
}
