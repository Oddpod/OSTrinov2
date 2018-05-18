package com.odd.ostrinov2.fragmentsLogic

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TableLayout
import com.odd.ostrinov2.MainActivity
import com.odd.ostrinov2.Ost
import com.odd.ostrinov2.R
import com.odd.ostrinov2.dialogFragments.AddOstDialog
import com.odd.ostrinov2.launchMeme
import com.odd.ostrinov2.tools.SortHandler
import java.util.*

class LibraryFragment : Fragment(), View.OnClickListener {

    private var filterText: String? = null
    internal var dialog = AddOstDialog()
    internal var playerDocked: Boolean = false
    var libListAdapter: OstsRVAdapter? = null
        private set
    private var mainActivity: MainActivity? = null
    lateinit var tlTop: TableLayout
    var shouldRefreshList: Boolean = false

    val currDispOstList: List<Ost>
        get() = libListAdapter!!.getOstList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        playerDocked = true
        val rootView = inflater.inflate(R.layout.fragment_library, container, false)
        dialog.setAddScreenListener(mainActivity!!)

        tlTop = rootView.findViewById(R.id.tlTop)

        val rvOst = rootView.findViewById<RecyclerView>(R.id.rvOstList)

        rvOst.adapter = libListAdapter
        rvOst.findViewById<View>(R.id.btnOptions)

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

        val filter = rootView.findViewById<EditText>(R.id.edtFilter)
        filterText = ""
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                filterText = s.toString()
                launchMeme(filterText!!, mainActivity!!)
                libListAdapter!!.filter(s.toString())
            }
        }
        filter.addTextChangedListener(textWatcher)
        val btnSort = rootView.findViewById<ImageButton>(R.id.btnSort)
        val btnShufflePlay = rootView.findViewById<ImageButton>(R.id.btnShufflePlay)
        val btnAdd = rootView.findViewById<ImageButton>(R.id.btnAdd)

        btnSort.setOnClickListener(this)
        btnShufflePlay.setOnClickListener(this)
        btnAdd.setOnClickListener(this)

        return rootView
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnShufflePlay -> {
                val rnd = Random()
                val currOstList = currDispOstList
                val rndPos = rnd.nextInt(currOstList.size)
                mainActivity!!.initiatePlayer(currOstList, rndPos)
                mainActivity!!.shuffleOn()
            }

            R.id.btnAdd -> {
                val TAG = "OstInfo"
                dialog.show(fragmentManager!!, TAG)
            }

            R.id.btnSort -> {
                libListAdapter!!.sort(SortHandler.SortMode.Alphabetical)
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && shouldRefreshList) {
            refreshListView()
            shouldRefreshList = false
        }
    }

    fun refreshListView() {
        val allOsts = MainActivity.getDbHandler().allOsts
        libListAdapter!!.updateList(allOsts)
    }

    fun setMainAcitivity(mainAcitivity: MainActivity) {

        this.mainActivity = mainAcitivity
        val allOsts = MainActivity.getDbHandler().allOsts
        libListAdapter = OstsRVAdapter(mainAcitivity, allOsts)
    }

    fun addOst(ost: Ost) {
        libListAdapter!!.addNewOst(ost)
    }
}
