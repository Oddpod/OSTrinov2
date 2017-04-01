package com.example.odd.ostrinofragnavdrawer;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment implements AddScreen.AddScreenListener, FunnyJunk.YareYareListener, View.OnClickListener{

    private int ostReplaceId;
    private List<Ost> allOsts, currDispOstList;
    private List<CheckBox> checkBoxes;
    private TableLayout tableLayout;
    private float rowTextSize = 11;
    private DBHandler dbHandler;
    private EditText filter;
    private String TAG = "OstInfo";
    private String TAG2 = "Jojo";
    private String filterText;
    private TextWatcher textWatcher;
    private TableRow tR;
    private FrameLayout flOntop, flLandscape, flNether;
    public YoutubeFragment youtubeFragment = null;
    Button btnDelHeader, btnPlayAll, btnplaySelected, btnStopPlayer;
    boolean youtubeFragLaunched;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_listscreen, container, false);
        dbHandler = new DBHandler(getActivity());

        filter = (EditText) rootView.findViewById(R.id.edtFilter);
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterText = filter.getText().toString();
                filterText = filterText.toLowerCase();
                if (filterText.equals("muda muda muda")) {
                    FunnyJunk dialog = new FunnyJunk();
                    dialog.show(getActivity().getFragmentManager(), TAG2);
                }
                cleanTable(tableLayout);
                for (Ost ost : allOsts) {
                    addRow(ost);
                }

            }
        };
        filter.addTextChangedListener(textWatcher);
        btnPlayAll = (Button) rootView.findViewById(R.id.btnPlayAll);
        btnplaySelected = (Button) rootView.findViewById(R.id.btnPlaySelected);
        btnStopPlayer = (Button) rootView.findViewById(R.id.btnStopPlayer);
        tableLayout = (TableLayout) rootView.findViewById(R.id.tlOstTable);
        flOntop = (FrameLayout) rootView.findViewById(R.id.flOntop);
        flLandscape = (FrameLayout) rootView.findViewById(R.id.flLandscape);
        flNether = (FrameLayout) rootView.findViewById(R.id.flNether);

        btnPlayAll.setOnClickListener(this);
        btnplaySelected.setOnClickListener(this);
        btnStopPlayer.setOnClickListener(this);

        currDispOstList = new ArrayList<>(); //Contains all ost in the shown list even when filtered
        createList();

        return rootView;
    }

    public void createList() {
        checkBoxes = new ArrayList<>();

        btnDelHeader = (Button) rootView.findViewById(R.id.btnDelHeader);

        btnDelHeader.setOnClickListener(this);

        allOsts = dbHandler.getAllOsts();

        for (Ost ost : allOsts) {
            addRow(ost);
        }
    }

    public void addRow(final Ost ost) {
        //final int id = ost.getId();
        String ostInfoString = ost.getTitle() + " " + ost.getShow() + " " + ost.getTags();
        ostInfoString = ostInfoString.toLowerCase();
        final String title = ost.getTitle();
        String show = ost.getShow();
        String tags = ost.getTags();
        tR = new TableRow(getActivity());
        tR.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        final String url = ost.getUrl();

        TextView label_title = new TextView(getActivity());
        label_title.setText(title);
        label_title.setTextColor(Color.BLACK);
        label_title.setTextSize(rowTextSize);
        label_title.setPadding(5, 5, 5, 5);
        tR.addView(label_title);

        TextView label_show = new TextView(getActivity());
        label_show.setText(show);
        label_show.setTextColor(Color.BLACK);
        label_show.setTextSize(rowTextSize);
        label_show.setPadding(5, 5, 5, 5);
        tR.addView(label_show);

        TextView label_tags = new TextView(getActivity());
        label_tags.setText(tags);
        label_tags.setTextColor(Color.BLACK);
        label_tags.setTextSize(rowTextSize);
        label_tags.setPadding(5, 5, 5, 5);
        tR.addView(label_tags);

        //Launches url when you click the title
        label_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(url);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                initYoutubeFrag();
                youtubeFragment.setVideoId(url);
                updateYoutubeFrag();
            }

        });

        CheckBox checkBox = new CheckBox(getActivity());
        checkBox.setPadding(5, 5, 0, 5);
        checkBox.setTextSize(rowTextSize);
        checkBox.setChecked(false);
        checkBoxes.add(checkBox);

        tR.addView(checkBox);
        //Long press to edit Ost
        tR.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                System.out.println(ost.toString());
                AddScreen dialog = new AddScreen();
                dialog.show(getFragmentManager(), TAG);
                ostReplaceId = ost.getId();
                dialog.setText(ost);

                //Toast.makeText(getApplicationContext(), " Editing Ost ", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        if (filterText != null && !ostInfoString.contains(filterText)) {
            //System.out.println(filterText + ostInfoString);
            currDispOstList.remove(ost);
            tR.removeAllViews();
        }
        else{
            currDispOstList.add(ost);
        }
        tableLayout.addView(tR, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onSaveButtonClick(DialogFragment dialog) {
        EditText entTitle = (EditText) dialog.getDialog().findViewById(R.id.edtTitle);
        String title = entTitle.getText().toString();
        EditText entShow = (EditText) dialog.getDialog().findViewById(R.id.edtShow);
        String show = entShow.getText().toString();
        EditText entTags = (EditText) dialog.getDialog().findViewById(R.id.edtTags);
        String tags = entTags.getText().toString();
        EditText entUrl = (EditText) dialog.getDialog().findViewById(R.id.edtUrl);
        String url = entUrl.getText().toString();
        Ost ost = new Ost(title, show, tags, url);
        ost.setId(ostReplaceId);

        dbHandler.updateOst(ost);
        cleanTable(tableLayout);
        allOsts = dbHandler.getAllOsts();
        for (Ost ost2 : allOsts) {
            addRow(ost2);
        }
        Toast.makeText(getActivity(), "updated ost: " + ost.getTitle(), Toast.LENGTH_LONG).show();
    }

    private void cleanTable(TableLayout table) {
        checkBoxes.clear();

        int childCount = table.getChildCount();

        // Remove all rows except the first one
        if (childCount > 1) {
            table.removeViews(1, childCount - 1);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlayAll: {
                List<String> urlList = new ArrayList<>();
                for (Ost ost : currDispOstList) {
                    urlList.add(ost.getUrl());
                }
                //System.out.println("urlList: " + urlList);
                initYoutubeFrag();
                youtubeFragment.setVideoIds(urlList);
                youtubeFragment.playAll(true);
                updateYoutubeFrag();
                break;
            }
            case R.id.btnPlaySelected:{
                int i = 0;
                List<String> playList = new ArrayList<>();
                for (CheckBox box : checkBoxes){
                    //System.out.println(box.isChecked());
                    if(box.isChecked()){
                        //System.out.println("i: " + i);
                        String url = allOsts.get(i).getUrl();
                        playList.add(url);
                        box.setChecked(false);
                    }
                    i++;
                }
                if(playList.size()> 0){
                    initYoutubeFrag();
                    youtubeFragment.setVideoIds(playList);
                    youtubeFragment.playAll(true);
                    updateYoutubeFrag();
                }
                break;

            }
            case R.id.btnDelHeader: {
                int i = 0;
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.isChecked()) {
                        int id = allOsts.get(i).getId();
                        dbHandler.deleteOst(id);
                    }

                    i++;
                }
                tR.removeAllViews();
                cleanTable(tableLayout);
                allOsts = dbHandler.getAllOsts();
                checkBoxes.clear(); //clear list of Checkboxes
                for (Ost ost : allOsts) {
                    addRow(ost);
                }
                break;
            }
            case R.id.btnStopPlayer: {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction().remove(youtubeFragment).commit();
                youtubeFragLaunched = false;
                btnStopPlayer.setVisibility(View.GONE);
                break;
            }
        }
    }

    public void initYoutubeFrag(){
        if(youtubeFragment == null){
            youtubeFragment = new YoutubeFragment();
        }
    }

    public void launchYoutubeFrag(){
        youtubeFragLaunched = true;
        btnStopPlayer.setVisibility(View.VISIBLE);
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction()
                .add(R.id.flOntop, youtubeFragment)
                .commit();
        int orientation = getActivity().getResources().getConfiguration().orientation;
        if( orientation == Configuration.ORIENTATION_LANDSCAPE){
            flNether.removeView(flOntop);
            flLandscape.addView(flOntop);
        }
    }

    public void updateYoutubeFrag(){
        if(youtubeFragLaunched){
            youtubeFragment.initPlayer();
        }
        else{
            launchYoutubeFrag();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(youtubeFragment != null){
                flNether.removeView(flOntop);
                flLandscape.addView(flOntop);
                Toast.makeText(getActivity(), "landscape", Toast.LENGTH_SHORT).show();
            }


        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            if(youtubeFragment != null){
                flLandscape.removeView(flOntop);
                flNether.addView(flOntop);
                Toast.makeText(getActivity(), "portrait", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startOst(String url){
        initYoutubeFrag();
        youtubeFragment.setVideoId(url);
        updateYoutubeFrag();
    }
}
