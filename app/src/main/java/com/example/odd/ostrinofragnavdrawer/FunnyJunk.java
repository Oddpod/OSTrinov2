package com.example.odd.ostrinofragnavdrawer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class FunnyJunk extends DialogFragment{

        AlertDialog.Builder builder;
        LayoutInflater inflater;
        View dialogView;
        ImageView jojo;

    public FunnyJunk(){}

        interface YareYareListener {
        }

        YareYareListener yareyareListener;

        @Override
        public void onAttach(Activity activity){
            super.onAttach(activity);

            try{
                yareyareListener = (FunnyJunk.YareYareListener) activity;
            } catch(ClassCastException e){
                throw new ClassCastException(activity.toString()
                        + " must implement yareyareListener");
            }

        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            builder = new AlertDialog.Builder(getActivity());

            inflater = getActivity().getLayoutInflater();
            dialogView = inflater.inflate(R.layout.activity_yareyare, null);

            builder.setView(dialogView);
            jojo = (ImageView) dialogView.findViewById(R.id.jojo);
            return builder.create();
        }

        public View getDialogView(){
            return dialogView;
        }
    }
