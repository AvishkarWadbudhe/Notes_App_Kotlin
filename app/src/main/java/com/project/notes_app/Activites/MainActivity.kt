package com.project.notes_app.Activites

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.project.notes_app.Database.NoteDatabase
import com.project.notes_app.R
import com.project.notes_app.Repository.NoteRepository
import com.project.notes_app.ViewModel.NoteViewModel
import com.project.notes_app.ViewModel.NoteViewModelFactory
import com.project.notes_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var noteViewModel: NoteViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)

        try {
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteViewModelFactory  = NoteViewModelFactory(noteRepository)
            noteViewModel = ViewModelProvider(
              this,
                noteViewModelFactory)[NoteViewModel::class.java]

        }catch (e:Exception){
            //  NoteViewModel(repository) as T
        }
    }
}