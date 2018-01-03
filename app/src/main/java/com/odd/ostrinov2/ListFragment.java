package com.odd.ostrinov2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.odd.ostrinov2.dialogFragments.AddScreen;
import com.odd.ostrinov2.listeners.QueueListener;
import com.odd.ostrinov2.tools.DBHandler;
import com.odd.ostrinov2.tools.UtilMeths;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ListFragment extends Fragment implements
        View.OnClickListener, QueueListener {

    boolean editedOst;
    private int ostReplaceId, ostReplacePos;
    private List<Ost> allOsts, currOstList;
    private DBHandler dbHandler;
    private String TAG = "OstInfo";
    private String filterText;
    private CustomAdapter customAdapter;
    private ListView lvOst;
    boolean shuffleActivated, playerDocked;
    AddScreen dialog;
    private Ost unAddedOst;
    private MainActivity mainActivity;
    TableLayout tlTop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        shuffleActivated = false;
        playerDocked = true;
        final View rootView = inflater.inflate(R.layout.activity_listscreen, container, false);
        dbHandler = new DBHandler(getActivity());
        unAddedOst = null;
        dialog = new AddScreen();
        dialog.setAddScreenListener(mainActivity);

        tlTop = (TableLayout) rootView.findViewById(R.id.tlTop);

        lvOst = (ListView) rootView.findViewById(R.id.lvOstList);
        lvOst.findViewById(R.id.btnOptions);
        lvOst.setDivider(null);

        allOsts = dbHandler.getAllOsts();

        customAdapter = new CustomAdapter(getContext(), allOsts, this);
        lvOst.setAdapter(customAdapter);

        lvOst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currOstList = getCurrDispOstList();
                mainActivity.initiatePlayer(currOstList, position);
            }
        });

        lvOst.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Ost ost = customAdapter.getItem(position);
                dialog.show(getFragmentManager(), TAG);
                ostReplaceId = ost.getId();
                ostReplacePos = position;
                editedOst = true;
                dialog.setEditing(ost, true);
                return true;
            }
        });

        if(allOsts.isEmpty()){
            final TextView tv = new TextView(getContext());
            ViewGroup.LayoutParams tvParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            tv.setLayoutParams(tvParams);
            tv.setText(R.string.label_empty_list_import);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        DBHandler db = new DBHandler(getContext());
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(
                                    new InputStreamReader(getContext().getAssets().open("Osts02_08_2017.txt")));

                            // do reading, usually loop until end of file reading
                            String line;
                            while ((line = reader.readLine()) != null) {
                                Ost ost = new Ost();
                                String[] lineArray = line.split("; ");
                                if (lineArray.length < 4) {
                                    return;
                                }
                                ost.setTitle(lineArray[0]);
                                ost.setShow(lineArray[1]);
                                ost.setTags(lineArray[2]);
                                ost.setUrl(lineArray[3]);
                                boolean alreadyInDB = db.checkiIfOstInDB(ost);
                                if (!alreadyInDB) {
                                    db.addNewOst(ost);
                                    UtilMeths.INSTANCE.downloadThumbnail(lineArray[3], getContext());
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        lvOst.removeHeaderView(tv);
                        refreshListView();
                }
            });
            lvOst.addHeaderView(tv);
        }

        EditText filter = (EditText) rootView.findViewById(R.id.edtFilter);
        filterText = "";
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterText = s.toString();
                MemesKt.launchMeme(filterText, mainActivity);
                customAdapter.filter(s.toString());
            }
        };
        filter.addTextChangedListener(textWatcher);
        ImageButton btnSort = (ImageButton) rootView.findViewById(R.id.btnSort);
        ImageButton btnShufflePlay = (ImageButton) rootView.findViewById(R.id.btnShufflePlay);
        ImageButton btnAdd = (ImageButton)  rootView.findViewById(R.id.btnAdd);

        btnSort.setOnClickListener(this);
        btnShufflePlay.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnShufflePlay:{
                Random rnd = new Random();
                currOstList = getCurrDispOstList();
                int rndPos = rnd.nextInt(currOstList.size());
                mainActivity.initiatePlayer(currOstList, rndPos);
                customAdapter.updateCurrentlyPlaying(currOstList.get(rndPos).getId());
                mainActivity.shuffleOn();
                shuffleActivated = true;
                break;
            }

            case R.id.btnAdd:{
                addOst();
                break;
            }

            case R.id.btnSort:{
                customAdapter.sort(1);
                break;
            }
        }
    }

    public int getOstReplaceId(){
        return ostReplaceId;
    }

    public int getOstReplacePos(){
        return  ostReplacePos;
    }

    public boolean isEditedOst(){
        return editedOst;
    }

    public List<Ost> getCurrDispOstList(){
        return customAdapter.getOstList();
    }

    @Override
    public void addToQueue(int addId) {
        Ost ost = getCurrDispOstList().get(addId);
        mainActivity.addToQueue(ost);
    }

    @Override
    public void removeFromQueue(String url) {

    }

    public void refreshListView(){
        editedOst = false;
        allOsts = dbHandler.getAllOsts();
        customAdapter.updateList(allOsts);
    }

    public AddScreen getDialog(){
        return dialog;
    }

    public void setUnAddedOst(Ost ost){
        unAddedOst = ost;
    }

    public void addOst(){
        if(unAddedOst != null){
            dialog.setEditing(unAddedOst, false);
        }else {
            dialog.setEditing(new Ost("", "", "", ""), false);
        }
        dialog.show(getFragmentManager(), TAG);
        editedOst = false;
    }


    public void setMainAcitivity(MainActivity mainAcitivity){
        this.mainActivity = mainAcitivity;
    }

    public CustomAdapter getCustomAdapter() {
        return customAdapter;
    }
}
