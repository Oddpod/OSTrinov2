package com.example.odd.ostrinofragnavdrawer;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Odd on 12.02.2017.
 */

public class AddScreen extends DialogFragment{

    AlertDialog.Builder builder;
    LayoutInflater inflater;
    View dialogView;
    String title, show, tags, url;

    EditText edTitle, edShow, edTags, edUrl;

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

        builder = new AlertDialog.Builder(getActivity());

        inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.activity_addscreen, null);

        builder.setView(dialogView)

                .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addScreenListener.onSaveButtonClick(AddScreen.this);

                    }
        });

        edTitle = (EditText) dialogView.findViewById(R.id.edtTitle);
        edTitle.setText(title);
        edShow = (EditText) dialogView.findViewById(R.id.edtShow);
        edShow.setText(show);
        edTags = (EditText) dialogView.findViewById(R.id.edtTags);
        edTags.setText(tags);
        edUrl = (EditText) dialogView.findViewById(R.id.edtUrl);
        edUrl.setText(url);

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
}
