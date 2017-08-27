package com.odd.ostrinov2;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;

import java.util.Arrays;
import java.util.List;

public class AddScreen extends DialogFragment {

    AlertDialog.Builder builder;
    LayoutInflater inflater;
    View dialogView;
    String title, show, tags, url, buttonText;

    EditText edTitle, edUrl;
    AutoCompleteTextView actvShow;
    MultiAutoCompleteTextView mactvTags;

    private boolean showDeleteButton = false;


    interface AddScreenListener {

        void onSaveButtonClick(DialogFragment dialog);

        void onDeleteButtonClick(DialogFragment dialog);
    }

    AddScreenListener addScreenListener;

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener && !showDeleteButton) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DBHandler dbHandler = new DBHandler(getActivity());
        List<String> showList = dbHandler.getAllShows();
        List<String> tagList = dbHandler.getAllTags();
        String[] tagsArray = dbHandler.getAllTagsArray();
        System.out.println(Arrays.asList(tagsArray));

        builder = new AlertDialog.Builder(getActivity());

        inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.activity_addscreen, null);

        builder.setView(dialogView)

                .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addScreenListener.onSaveButtonClick(AddScreen.this);

                    }
                });
        if (showDeleteButton) {
            builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addScreenListener.onDeleteButtonClick(AddScreen.this);

                }
            });
        }
        ArrayAdapter<String> tagsArrayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, tagsArray);
        System.out.println(tagsArrayAdapter.toString());

        ArrayAdapter<String> tagsAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, tagList);

        ArrayAdapter<String> showAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, showList);

        edTitle = (EditText) dialogView.findViewById(R.id.edtTitle);
        actvShow = (AutoCompleteTextView) dialogView.findViewById(R.id.actvShow);
        mactvTags = (MultiAutoCompleteTextView) dialogView.findViewById(R.id.mactvTags);
        edUrl = (EditText) dialogView.findViewById(R.id.edtUrl);

        edTitle.setText(title);
        actvShow.setText(show);
        mactvTags.setText(tags);
        edUrl.setText(url);

        actvShow.setAdapter(showAdapter);
        mactvTags.setAdapter(tagsAdapter);
        mactvTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());


        return builder.create();
    }

    public void setText(Ost ost) {
        // sets editText to the chosen osts values
        title = ost.getTitle();
        show = ost.getShow();
        tags = ost.getTags();
        url = ost.getUrl();
    }

    public void setButtonText(String text) {
        buttonText = text;
    }

    public String[] getFieldData() {
        String[] fields = new String[4];
        fields[0] = edTitle.getText().toString();
        fields[1] = actvShow.getText().toString();
        fields[2] = mactvTags.getText().toString();
        fields[3] = edUrl.getText().toString();
        return fields;
    }

    public void showDeleteButton(boolean show) {
        showDeleteButton = show;
    }

    public void setAddScreenListener(AddScreenListener addScreenListener) {
        this.addScreenListener = addScreenListener;
    }
}
