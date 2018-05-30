package com.odd.ostrinov2.dialogFragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R

class EditOstDialog : DialogFragment() {

    private var lastOst = Ost("", "", "", "")

    private lateinit var edTitle: EditText
    private lateinit var edUrl: EditText
    internal lateinit var actvShow: AutoCompleteTextView
    internal lateinit var mactvTags: MultiAutoCompleteTextView

    lateinit var editOstDialogListener: EditOstDialogListener

    init {
        lastOst.id = -1
    }

    interface EditOstDialogListener {

        fun onSaveButtonClick(editedOst: Ost, dialog: EditOstDialog)

        fun onDeleteButtonClick(deletedOst: Ost, dialog: EditOstDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dbHandler = MainActivity.dbHandler

        val builder = AlertDialog.Builder(activity!!)

        val inflater = activity!!.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_addscreen, null)

        builder.setView(dialogView)
                .setPositiveButton("Save") { _, _ -> saveOst() }

        builder.setNegativeButton("Delete") { _, _ -> deleteOst() }

        val tagsAdapter = ArrayAdapter(activity!!,
                android.R.layout.simple_spinner_dropdown_item, dbHandler.allTags)

        val showAdapter = ArrayAdapter(activity!!,
                android.R.layout.simple_spinner_dropdown_item, dbHandler.allShows)

        edTitle = dialogView.findViewById(R.id.edtTitle)
        actvShow = dialogView.findViewById(R.id.actvShow)
        mactvTags = dialogView.findViewById(R.id.mactvTags)
        edUrl = dialogView.findViewById(R.id.edtUrl)

        edTitle.setText(lastOst.title)
        actvShow.setText(lastOst.show)
        mactvTags.setText(lastOst.tags)
        edUrl.setText(lastOst.url)

        actvShow.setAdapter(showAdapter)
        mactvTags.setAdapter(tagsAdapter)
        mactvTags.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())


        return builder.create()
    }

    fun setText(ost: Ost) {
        // sets editText to the chosen osts values
        if (lastOst.id == ost.id) {
            return
        }
        lastOst = ost
    }

    fun setEditOstListener(editOstDialogListener: EditOstDialogListener) {
        this.editOstDialogListener = editOstDialogListener
    }

    private fun saveOst() {
        lastOst.title = edTitle.text.toString()
        lastOst.show = actvShow.text.toString()
        lastOst.tags = mactvTags.text.toString()
        lastOst.url = edUrl.text.toString()
        println("Saving")
        editOstDialogListener.onSaveButtonClick(lastOst, this)
    }

    private fun deleteOst() {
        editOstDialogListener.onDeleteButtonClick(lastOst, this)
    }


}
