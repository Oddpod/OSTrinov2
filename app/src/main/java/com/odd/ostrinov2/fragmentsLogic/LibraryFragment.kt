package com.odd.ostrinov2.fragmentsLogic

import android.os.Bundle
import android.support.v4.app.DialogFragment
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
import android.widget.Toast
import com.odd.ostrinov2.*
import com.odd.ostrinov2.dialogFragments.AddOstDialog
import com.odd.ostrinov2.tools.SortHandler
import java.util.*


class LibraryFragment : Fragment(), View.OnClickListener, AddOstDialog.AddDialogListener {

    private var dialog = AddOstDialog()
    var libListAdapter: OstsRVAdapter? = null
        private set
    private var mainActivity: MainActivity? = null
    lateinit var tlTop: TableLayout

    private val currDispOstList: List<Ost>
        get() = libListAdapter!!.getOstList()
    private var isScrollingUp = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_library, container, false)
        dialog.setAddScreenListener(this)

        tlTop = rootView.findViewById(R.id.tlTop)

        val rvOst = rootView.findViewById<RecyclerView>(R.id.rvOstList)

        val fastScroller = rootView.findViewById(R.id.fast_scroller) as FastScroller
        fastScroller.setRecyclerView(rvOst)

        rvOst.adapter = libListAdapter

        rvOst.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isScrollingUp = dy < 0
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_SETTLING && isScrollingUp) {
                    tlTop.visibility = View.VISIBLE
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING && !isScrollingUp) {
                    tlTop.visibility = View.GONE
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

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
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val filterText = s.toString()
                if (mainActivity != null)
                    launchMeme(filterText, mainActivity!!)
                libListAdapter?.filter(s.toString())
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
                if (!currOstList.isEmpty()) {
                    val rndPos = rnd.nextInt(currOstList.size)
                    mainActivity!!.initiatePlayer(currOstList, rndPos)
                    mainActivity!!.shuffleOn()
                }
            }

            R.id.btnAdd -> {
                dialog.show(fragmentManager!!, "OstInfo")
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
        val allOsts = MainActivity.dbHandler.allOsts
        libListAdapter!!.updateList(allOsts)
    }

    fun setMainActivity(mainActivity: MainActivity) {

        this.mainActivity = mainActivity
        val allOsts = MainActivity.dbHandler.allOsts
        libListAdapter = OstsRVAdapter(mainActivity, allOsts)
    }

    private fun addOst(ost: Ost) {
        libListAdapter!!.addNewOst(ost)
    }

    override fun onAddButtonClick(ostToAdd: Ost, dialog: DialogFragment) {
        addNewOst(ostToAdd)
    }

    private fun addNewOst(ostToAdd: Ost) {
        val dbHandler = MainActivity.dbHandler
        val alreadyAdded = dbHandler.checkiIfOstInDB(ostToAdd)
        if (!alreadyAdded) {
            dbHandler.addNewOst(ostToAdd)
            Toast.makeText(context, ostToAdd.title + " added",
                    Toast.LENGTH_SHORT).show()
            addOst(ostToAdd)
        } else {
            Toast.makeText(context, ostToAdd.title + " From " + ostToAdd.show
                    + " has already been added", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        var shouldRefreshList: Boolean = false
    }
}
