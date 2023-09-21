package com.project.notes_app.Repository

import com.project.notes_app.Database.NoteDatabase
import com.project.notes_app.Model.Note

class NoteRepository(private val db: NoteDatabase) {

    fun getNote()= db.getNoteDao().getAllNote();

    fun searchNote(query: String)=db.getNoteDao().searchNote(query)

    suspend fun addNote(note : Note)= db.getNoteDao().addNote(note)


    suspend fun updateNote(note : Note)= db.getNoteDao().updateNote(note)

    suspend fun deleteNote(note : Note)= db.getNoteDao().deleteNote(note)


}