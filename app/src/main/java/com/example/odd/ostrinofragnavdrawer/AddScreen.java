package com.example.odd.ostrinofragnavdrawer;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.method.MultiTapKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;

import java.util.List;

public class AddScreen extends DialogFragment{

    AlertDialog.Builder builder;
    LayoutInflater inflater;
    View dialogView;
    String title, show, tags, url, buttonText;

    EditText edTitle, edUrl;
    AutoCompleteTextView actvShow;
    MultiAutoCompleteTextView mactvTags;

    private List<String> showList, tagList;


    interface AddScreenListener {

        void onSaveButtonClick(DialogFragment dialog);
    }

    AddScreenListener addScreenListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        try{
            addScreenListener = (AddScreenListener) activity;
        } catch(ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement AddScreenListener");
        }

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        DBHandler dbHandler = new DBHandler(getActivity());
        showList = dbHandler.getAllShows();
        tagList = dbHandler.getAllTags();
        System.out.println("ShowList: " + showList.toString());
        System.out.println("tagList: " + tagList.toString());

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

        ArrayAdapter<String> tagsAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, tagList);

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

    public View getDialogView(){
        return dialogView;
    }

    public void setText(Ost ost){
        // sets editText to the chosen osts values
        title = ost.getTitle();
        show = ost.getShow();
        tags = ost.getTags();
        url = ost.getUrl();
    }

    public void setButtonText(String text){
        buttonText = text;
    }
}
