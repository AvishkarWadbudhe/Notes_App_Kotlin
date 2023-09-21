package com.project.notes_app.Fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialContainerTransform
import com.project.notes_app.Activites.MainActivity
import com.project.notes_app.Model.Note
import com.project.notes_app.R
import com.project.notes_app.Utils.hideKeyboard
import com.project.notes_app.ViewModel.NoteViewModel
import com.project.notes_app.databinding.BottomSheetLayoutBinding
import com.project.notes_app.databinding.FragmentSaveOrUpdateBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class SaveOrUpdateFragment : Fragment(R.layout.fragment_save_or_update) {


    private lateinit var navController: NavController
    private lateinit var contentBinding: FragmentSaveOrUpdateBinding
    private lateinit var result:String
    private var note: Note?=null
    private var color=-1
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat.getInstance().format(Date())
    private val job = CoroutineScope(Dispatchers.Main)
    private val args:SaveOrUpdateFragmentArgs by navArgs()


    override fun onCreate(savedInstanceState:Bundle?)
    {
        super.onCreate(savedInstanceState)
        val animation= MaterialContainerTransform().apply {
            drawingViewId=R.id.fragment
            scrimColor = Color.TRANSPARENT
            duration=300L
        }
        sharedElementEnterTransition=animation
        sharedElementReturnTransition=animation

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding  =FragmentSaveOrUpdateBinding.bind(view)

        navController = Navigation.findNavController(view)
        val activity = activity as MainActivity

        ViewCompat.setTransitionName(
            contentBinding.noteContentFragmentParent,"recycleView_${args.note?.id}"
        )

        contentBinding.backBtn.setOnClickListener{
            requireView().hideKeyboard()
            navController.popBackStack()

        }

        contentBinding.saveNote.setOnClickListener {
            saveNote()
        }

            try {
                contentBinding.etNoteContent.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus)
                    {
                        contentBinding.bottomBar.visibility = View.VISIBLE
                        contentBinding.etNoteContent.setStylesBar(contentBinding.styleBar)
                    }else contentBinding.bottomBar.visibility = View.GONE

                }
            }catch (e:Throwable){
                Log.d("Tag",e.stackTrace.toString())
            }

            contentBinding.fabColorPick.setOnClickListener{
                val bottomSheetDialog = BottomSheetDialog(
                    requireContext(),
                    R.style.BottomSheetDialogTheme
                )
                val bottomSheetView:View = layoutInflater.inflate(
                    R.layout.bottom_sheet_layout,
                    null
                )
                with(bottomSheetDialog)
                {
                    setContentView(bottomSheetView)
                    show()
                }
                val bottomSheetBinding = BottomSheetLayoutBinding.bind(bottomSheetView)
                bottomSheetBinding.apply {
                    colorPicker.apply {
                        setSelectedColor(color)
                        setOnColorSelectedListener {
                                value->
                            color=value
                            contentBinding.apply {
                                noteContentFragmentParent.setBackgroundColor(color)
                                toolbarFragmentNoteContent.setBackgroundColor(color)
                                bottomBar.setBackgroundColor(color)
                                activity.window.statusBarColor=color
                            }
                            bottomSheetBinding.bottomSheetParent.setCardBackgroundColor(color)

                        }
                    }
                    bottomSheetParent.setCardBackgroundColor(color)
                }
                bottomSheetView.post{
                    bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

                }
            }
        //open existing note
        setUpNote()
    }

    private fun setUpNote() {
      val note =args.note
        val title =contentBinding.etTitle
        val content = contentBinding.etNoteContent
        val lastEdited = contentBinding.lastEdited

        if(note ==null){
            contentBinding.lastEdited.text = getString(R.string.edited_on,SimpleDateFormat.getDateInstance().format(Date()))
        }
        if(note!=null)
        {
            title.setText(note.title)
            content.renderMD(note.content)
            lastEdited.text=getString(R.string.edited_on,note.date)
            color=note.color
            contentBinding.apply {
                job.launch {
                    delay(10)
                    noteContentFragmentParent.setBackgroundColor(color)
                }
                toolbarFragmentNoteContent.setBackgroundColor(color)
                bottomBar.setBackgroundColor(color)
            }
            activity?.window?.statusBarColor=note.color
        }
    }


    private fun saveNote() {
        if(contentBinding.etNoteContent.text.toString().isEmpty()||contentBinding.etTitle.text.toString().isEmpty())
        {
            Toast.makeText(activity,"Empty", Toast.LENGTH_SHORT).show()
        }
        else{
            note =args.note
            when(note){
                null->{
                    noteViewModel.saveNote(
                        Note(
                            0,
                            contentBinding.etTitle.text.toString(),
                            contentBinding.etNoteContent.getMD(),
                            currentDate,
                            color
                        )

                    )
                    result = "Note Saved"
                    setFragmentResult(
                        "key",
                        bundleOf("bundleKey" to result)
                    )
                    navController.navigate(SaveOrUpdateFragmentDirections.actionSaveOrUpdateFragmentToNoteFragment())
                }
                else ->{
                    //update notes
                    updateNote()
                    navController.popBackStack()

                }
            }
        }
    }

    private fun updateNote() {
     if(note!=null)
     {
         noteViewModel.updateNote(
             Note(
                 note!!.id,
                 contentBinding.etTitle.text.toString(),
                 contentBinding.etNoteContent.getMD(),
                 currentDate,
                 color,

             )
         )
     }
    }

}