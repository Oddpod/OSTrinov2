package com.odd.ostrinov2.dialogFragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.*
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R
import com.odd.ostrinov2.tools.UtilMeths
import com.odd.ostrinov2.tools.YDataHandler
import kotlinx.coroutines.experimental.async

class AddOstDialog : DialogFragment() {

    private var lastOst = Ost("", "", "", "")

    private lateinit var edTitle: EditText
    private lateinit var edUrl: EditText
    internal lateinit var actvShow: AutoCompleteTextView
    internal lateinit var mactvTags: MultiAutoCompleteTextView

    private lateinit var addDialogListener: AddDialogListener

    interface AddDialogListener {

        fun onAddButtonClick(ostToAdd: Ost, dialog: DialogFragment)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        lastOst.title = edTitle.text.toString()
        lastOst.show = actvShow.text.toString()
        lastOst.tags = mactvTags.text.toString()
        lastOst.url = edUrl.text.toString()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dbHandler = MainActivity.dbHandler
        val showList = dbHandler.allShows
        val tagList = dbHandler.allTags

        val builder = AlertDialog.Builder(activity!!)

        val inflater = activity!!.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_addscreen, null)

        builder.setView(dialogView)
                .setPositiveButton("Add") { _, _ -> addOst() }

        val tagsAdapter = ArrayAdapter(activity!!,
                android.R.layout.simple_spinner_dropdown_item, tagList)

        val showAdapter = ArrayAdapter(activity!!,
                android.R.layout.simple_spinner_dropdown_item, showList)

        edTitle = dialogView.findViewById(R.id.edtTitle)
        actvShow = dialogView.findViewById(R.id.actvShow)
        mactvTags = dialogView.findViewById(R.id.mactvTags)
        edUrl = dialogView.findViewById(R.id.edtUrl)

        edTitle.setText(lastOst.title)
        actvShow.setText(lastOst.show)
        mactvTags.setText(lastOst.tags)
        edUrl.setText(lastOst.url)
        edUrl.setOnFocusChangeListener { _, b ->
            if (!b && edTitle.text.isEmpty()) {
                async {
                    val suggestedTitle = YDataHandler.getVideoTitle(UtilMeths.urlToId(
                            edUrl.text.toString()))
                    edTitle.setText(suggestedTitle)
                }
            }
        }

        actvShow.setAdapter(showAdapter)
        mactvTags.setAdapter(tagsAdapter)
        mactvTags.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())

        return builder.create()
    }

    fun setAddScreenListener(addDialogListener: AddDialogListener) {
        this.addDialogListener = addDialogListener
    }

    private fun addOst() {
        lastOst.title = edTitle.text.toString()
        lastOst.show = actvShow.text.toString()
        lastOst.tags = mactvTags.text.toString()
        val url = edUrl.text.toString()
        if (!url.contains("youtu") || url.endsWith("="))
            Toast.makeText(this.context, "You have to enter a valid link",
                    Toast.LENGTH_SHORT).show()
        else {
            lastOst.url = edUrl.text.toString()
            addDialogListener.onAddButtonClick(lastOst, this)
            lastOst = Ost("", "", "", "")
        }
    }
}
