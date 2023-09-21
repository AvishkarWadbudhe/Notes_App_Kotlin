package com.project.notes_app.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.project.notes_app.Repository.NoteRepository

@Suppress("UNCHECKED_CAST")
class NoteViewModelFactory(private val repository: NoteRepository) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        // Create a new instance of NoteViewModel with the repository
        return NoteViewModel(repository) as T
    }
}
