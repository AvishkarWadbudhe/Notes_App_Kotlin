package com.project.notes_app.Adapter

import androidx.recyclerview.widget.DiffUtil
import com.project.notes_app.Model.Note

 class DiffUtilCallback :DiffUtil.ItemCallback<Note>(){
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
    return oldItem.id==newItem.id
    }


    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
       return oldItem.id==newItem.id
    }

}