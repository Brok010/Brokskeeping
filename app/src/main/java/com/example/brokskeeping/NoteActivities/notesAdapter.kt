package com.example.brokskeeping.NoteActivities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.brokskeeping.DataClasses.HiveNotes
import com.example.brokskeeping.DbFunctionality.DatabaseHelper
import com.example.brokskeeping.DbFunctionality.NotesFunctionality
import com.example.brokskeeping.Functionality.Utils
import com.example.brokskeeping.R
import java.text.SimpleDateFormat
import java.util.Locale

class NotesAdapter(private val notesList: MutableList<HiveNotes>,
                   private val hiveId: Int,
                   private val db: DatabaseHelper,
                   private val notesBrowserActivity: NotesBrowserActivity
) : RecyclerView.Adapter<NotesAdapter.HiveNotesViewHolder>() {

    fun updateData(newNotesList: List<HiveNotes>) {
        notesList.clear()
        notesList.addAll(newNotesList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiveNotesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_note_browser, parent, false)
        return HiveNotesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HiveNotesViewHolder, position: Int) {
        val currentHiveNotes = notesList[position]
        holder.bind(currentHiveNotes)

        holder.itemView.setOnClickListener {
            notesBrowserActivity.startNoteActivity(currentHiveNotes.id)
        }

        holder.itemView.setOnLongClickListener {
            showContextMenu(holder.itemView, currentHiveNotes)
            true
        }
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    inner class HiveNotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNotesText: TextView = itemView.findViewById(R.id.tv_note_text)
        private val tvNotesDate: TextView = itemView.findViewById(R.id.tv_note_date)

        fun bind(note: HiveNotes) {
            tvNotesText.text = note.noteText
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(note.noteDate)
            tvNotesDate.text = formattedDate
        }
    }
    private fun showContextMenu(view: View, note: HiveNotes) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu_long_click_browser, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_long_click_delete -> {
                    Utils.showConfirmationDialog(
                        view.context,
                        view.context.getString(R.string.are_you_sure_you_want_to_delete_this_note)
                    ) { confirmed ->
                        if (confirmed) {
                            // User confirmed the deletion
                            NotesFunctionality.deleteNote(db, note.id, hiveId)
                            val (notes, result) = NotesFunctionality.getAllNotes(db, hiveId, null, null, true)
                            if (result == 0) {
                                Log.e("NotesAdapter", "Couldn't retrieve notes")
                            }
                            updateData(notes)
                        }
                    }
                    true
                }
                R.id.menu_long_click_adjust -> {
                    // Handle adjust action
                    notesBrowserActivity.startAdjustNotesActivity(note.id)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
