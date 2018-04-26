package com.odd.ostrinov2.fragmentsLogic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;

import com.odd.ostrinov2.MainActivity;
import com.odd.ostrinov2.MemesKt;
import com.odd.ostrinov2.Ost;
import com.odd.ostrinov2.R;
import com.odd.ostrinov2.dialogFragments.AddOstDialog;
import com.odd.ostrinov2.tools.SortHandler;

import java.util.List;
import java.util.Random;

public class LibraryFragment extends Fragment implements
        View.OnClickListener {

    private String filterText;
    AddOstDialog dialog;
    boolean playerDocked;
    private PlaylistRVAdapter libListAdapter;
    private MainActivity mainActivity;
    public TableLayout tlTop;
    public boolean shouldRefreshList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        playerDocked = true;
        final View rootView = inflater.inflate(R.layout.fragment_library, container, false);
        dialog = new AddOstDialog();
        dialog.setAddScreenListener(mainActivity);

        tlTop = rootView.findViewById(R.id.tlTop);

        RecyclerView rvOst = rootView.findViewById(R.id.rvOstList);

        rvOst.setAdapter(libListAdapter);
        rvOst.findViewById(R.id.btnOptions);

        /*if(allOsts.isEmpty()){
            final TextView tv = new TextView(getContext());
            ViewGroup.LayoutParams tvParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            tv.setLayoutParams(tvParams);
            tv.setText(R.string.label_empty_list_import);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        DBHandler db = MainActivity.getDbHandler();
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(
                                    new InputStreamReader(getContext().getAssets().open("Osts02_08_2017.txt")));

                            // do reading, usually loop until end of file reading
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String[] lineArray = line.split("; ");
                                if (lineArray.length < 4) {
                                    return;
                                }
                                Ost ost = new Ost(lineArray[0], lineArray[1], lineArray[2],
                                        lineArray[3]);
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
                        rvOst.removeHeaderView(tv);
                        refreshListView();
                }
            });
            rvOst.addHeaderView(tv);
        }*/

        EditText filter = rootView.findViewById(R.id.edtFilter);
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
                libListAdapter.filter(s.toString());
            }
        };
        filter.addTextChangedListener(textWatcher);
        ImageButton btnSort = rootView.findViewById(R.id.btnSort);
        ImageButton btnShufflePlay = rootView.findViewById(R.id.btnShufflePlay);
        ImageButton btnAdd = rootView.findViewById(R.id.btnAdd);

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
                List<Ost> currOstList = getCurrDispOstList();
                int rndPos = rnd.nextInt(currOstList.size());
                mainActivity.initiatePlayer(currOstList, rndPos);
                mainActivity.shuffleOn();
                break;
            }

            case R.id.btnAdd:{
                String TAG = "OstInfo";
                dialog.show(getFragmentManager(), TAG);
                break;
            }

            case R.id.btnSort:{
                libListAdapter.sort(SortHandler.SortMode.Alphabetical);
                break;
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        System.out.println("Isvisibletouser");
        if (isVisibleToUser && shouldRefreshList) {
            System.out.println("refreshingisvisibile");
            refreshListView();
            shouldRefreshList = false;
        }
    }

    public List<Ost> getCurrDispOstList(){
        return libListAdapter.getOstList();
    }

    public void refreshListView(){
        List<Ost> allOsts = MainActivity.getDbHandler().getAllOsts();
        libListAdapter.updateList(allOsts);
    }

    public void setMainAcitivity(MainActivity mainAcitivity){

        this.mainActivity = mainAcitivity;
        List<Ost> allOsts = MainActivity.getDbHandler().getAllOsts();
        libListAdapter = new PlaylistRVAdapter(mainAcitivity, allOsts);
    }

    public void addOst(Ost ost) {
        libListAdapter.addNewOst(ost);
    }

    public PlaylistRVAdapter getLibListAdapter() {
        return libListAdapter;
    }
}
