package com.example.notesapproom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var addNoteEt: EditText
    lateinit var addBu : Button
    lateinit var recView: RecyclerView
    private lateinit var rvAdapter: RecyclerViewAdapter
    private lateinit var notes: List<Note>
    var userInput = ""
    private val noteDao by lazy { NoteDatabase.getDatabase(this).noteDao() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notes = listOf()

        addNoteEt = findViewById(R.id.addNoteEt)
        addBu = findViewById(R.id.addBu)
        recView = findViewById(R.id.recView)
        rvAdapter = RecyclerViewAdapter(notes, this)
        recView.adapter = rvAdapter
        recView.layoutManager = LinearLayoutManager(this)
        getNotes()
        addBu.setOnClickListener {

            CoroutineScope(IO).launch {
                var note = addNoteEt.text.toString()
                noteDao.addNote(Note(0, note))
                getNotes()
            }

            Toast.makeText(this, "Added successfully", Toast.LENGTH_LONG).show()

        }

    }

    private fun getNotes(){
        CoroutineScope(IO).launch {
            val data = async {
                noteDao.getNotes()
            }.await()
            if(data.isNotEmpty()){
                Log.d("dataIs","dataIs$data")
                notes = data
                withContext(Main){
                    rvAdapter.update(notes)
                }

            }else{
                Log.e("MainActivity", "Unable to get data", )
            }
        }
    }
    private fun updateNote(noteID: Int, noteText: String){
        CoroutineScope(IO).launch {
            noteDao.updateNote(Note(noteID,noteText))
            getNotes()
        }
    }

    fun deleteNote(noteID: Int){
        CoroutineScope(IO).launch {
            noteDao.deleteNote(Note(noteID,""))
        }
    }

    fun dilog(updateOrDelete :String,selectedNote:String,pk:Int) {

        if (updateOrDelete.equals("update")){
            val builder = AlertDialog.Builder(this)
            //  set title for alert dialog
            builder.setTitle("Update Note")

            var  input = EditText(this)
            input.setText(selectedNote)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)


            //performing positive action
            builder.setPositiveButton("update") { dialogInterface, which ->

                userInput = input.text.toString()
                updateNote(pk,userInput)


            }
            builder.setNegativeButton("CANCEL"){dialogInterface, which ->}
            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            // alertDialog.setCancelable(false)
            alertDialog.show()
        }

        else {
            val builder = AlertDialog.Builder(this)
            //  set title for alert dialog
            builder.setTitle("Are you sure to delete note  ")
            var  input = EditText(this)
            input.setText(selectedNote)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)
            //performing positive action
            builder.setPositiveButton("Delete") { dialogInterface, which ->
                userInput = input.text.toString()
                deleteNote(pk)

            }
            builder.setNegativeButton("CANCEL"){dialogInterface, which ->}
            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(true)
            alertDialog.show()
        }
    }
}