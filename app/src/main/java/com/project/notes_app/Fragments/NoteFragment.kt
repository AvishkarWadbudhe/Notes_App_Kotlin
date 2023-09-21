package com.project.notes_app.Fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.project.notes_app.Activites.MainActivity
import com.project.notes_app.Adapter.NotesAdapter
import com.project.notes_app.R
import com.project.notes_app.Utils.SwipeToDeleteCallback
import com.project.notes_app.Utils.hideKeyboard
import com.project.notes_app.ViewModel.NoteViewModel
import com.project.notes_app.databinding.FragmentNoteBinding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NoteFragment : Fragment(R.layout.fragment_note) {

    private lateinit var noteBinding: FragmentNoteBinding
    private val noteActivityViewModel: NoteViewModel by activityViewModels()
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration  =350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration =350
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteBinding  =FragmentNoteBinding.bind(view)
        val activity = activity as MainActivity
        val navController = Navigation.findNavController(view)
        requireView().hideKeyboard()
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            //activity.window.statusBarColor = Color.WHITE
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9E9D9D")

        }

        noteBinding.addNote.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment())

        }
        noteBinding.innerFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrUpdateFragment())

        }

        recycleViewDisplay()
        swipeToDelete(noteBinding.recycleView)
        //implement here
        noteBinding.search.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
               noteBinding.noDataImage.isVisible =false
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
             if(s.toString().isNotEmpty())
             {
                 val text =s.toString()
                 val  query = "%$text%"
                 if(query.isNotEmpty())
                 {
                     noteActivityViewModel.searchNote(query).observe(viewLifecycleOwner){
                         notesAdapter.submitList(it)

                     }
                 }
                 else{
                     observerDataChanges()
                 }

             }
                else{
                    observerDataChanges()

             }

            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        noteBinding.search.setOnEditorActionListener{v,actionId,_->
        if(actionId==EditorInfo.IME_ACTION_SEARCH)
        {
            v.clearFocus()
            requireView().hideKeyboard()

        }
            return@setOnEditorActionListener true
        }

        noteBinding.recycleView.setOnScrollChangeListener{_,scrollX,scrollY,_,oldScrollY ->
            when{
                scrollY>oldScrollY->{
                    noteBinding.chatFabtext.isVisible =false
                }
                scrollX==scrollY->
                {
                    noteBinding.chatFabtext.isVisible=true
                }
                else->{
                    noteBinding.chatFabtext.isVisible=true
                }
            }

        }

    }

    private fun swipeToDelete(recycleView: RecyclerView) {
        val swipeToDeleteCallback =object :SwipeToDeleteCallback()
        {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
             val position=viewHolder.absoluteAdapterPosition
                val note =notesAdapter.currentList[position]
                var actionBtnTapped=false
                noteActivityViewModel.deleteNote(note)
                noteBinding.search.apply {
                    hideKeyboard()
                    clearFocus()
                }
                if(noteBinding.search.text.toString().isEmpty())
                {
                    observerDataChanges()
                }
                val snackbar = Snackbar.make(
                    requireView(),"Note Deleted",Snackbar.LENGTH_LONG
                ).addCallback(object :BaseTransientBottomBar.BaseCallback<Snackbar>(){
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {
                        transientBottomBar?.setAction("UNDO"){
                            noteActivityViewModel.saveNote(note)
                            actionBtnTapped=true
                            noteBinding.noDataImage.isVisible=false
                        }
                        super.onShown(transientBottomBar)
                    }
                }).apply {
                    animationMode =Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.add_note)
                }
                snackbar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),R.color.yellowOrange
                    )
                )
                snackbar.show()
            }

        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recycleView)



    }

    private fun observerDataChanges() {
       noteActivityViewModel.getAllNotes().observe(viewLifecycleOwner){
           list ->
           noteBinding.noDataImage.isVisible=list.isEmpty()
           notesAdapter.submitList(list)
       }
    }

    private fun recycleViewDisplay() {
       when(resources.configuration.orientation)
       {
           Configuration.ORIENTATION_PORTRAIT->setUpRecycleView(2)
           Configuration.ORIENTATION_LANDSCAPE->setUpRecycleView(3)

       }
    }

    private fun setUpRecycleView(spanCount: Int) {

    noteBinding.recycleView.apply {
        layoutManager=StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL)
        setHasFixedSize(true)
        notesAdapter = NotesAdapter()
        notesAdapter.stateRestorationPolicy=
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        adapter =notesAdapter
        postponeEnterTransition(300L, TimeUnit.MILLISECONDS)
        viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }
    }
        observerDataChanges()


    }
}