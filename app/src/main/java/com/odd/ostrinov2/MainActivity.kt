package com.odd.ostrinov2

import android.app.Activity
import android.app.SearchManager
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.FragmentManager
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.odd.ostrinov2.asynctasks.YParsePlaylist
import com.odd.ostrinov2.asynctasks.YoutubeShare
import com.odd.ostrinov2.fragmentsLogic.AboutFragment
import com.odd.ostrinov2.fragmentsLogic.LibraryFragment
import com.odd.ostrinov2.fragmentsLogic.PlaylistFragment
import com.odd.ostrinov2.fragmentsLogic.SearchFragment
import com.odd.ostrinov2.services.YTplayerService
import com.odd.ostrinov2.tools.*

private const val PREFS_NAME = "Saved queue"

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var backPress: Int = 0
    lateinit var libraryFragment: LibraryFragment
        private set
    private lateinit var searchFragment: SearchFragment
    private lateinit var playlistFragment: PlaylistFragment

    private var floatingPlayer: FrameLayout? = null
    private var rlContent: RelativeLayout? = null

    private var about = false
    private var ostFromWidget = false
    private var ostFromWidgetId: Int = 0
    private var queueAdapter: QueueAdapter? = null
    private var manager: FragmentManager? = null
    private var youTubePlayerFragment: YouTubePlayerSupportFragment? = null
    private var mIsBound: Boolean = false
    private var shuffleActivated: Boolean? = false
    private var repeat: Boolean = false
    private var playing: Boolean = false
    private var lastSessionLoaded: Boolean = false
    private var yTplayerService: YTplayerService? = null
    private var btnRepeat: ImageButton? = null
    private var btnPlayPause: ImageButton? = null
    private var btnShuffle: ImageButton? = null
    var seekBar: SeekBar? = null
        private set
    private var seekbarUpdater: Runnable? = null
    private val handler = Handler()
    private var searchView: SearchView? = null
    private var fragPager: ViewPager? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            yTplayerService = (service as YTplayerService.LocalBinder).service
            if (ostFromWidget) {
                startWidgetOst(ostFromWidgetId)
                ostFromWidget = false
                shuffleOn()
                println("ost from widget")
            } else if (!lastSessionLoaded) {
                loadLastSession()
            }
            // Tell the user about this for our demo.
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mIsBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        dbHandler = DBHandler(this)
        //dbHandler.recreateDatabase();
        rlContent = findViewById(R.id.rlContent)
        fragPager = findViewById(R.id.frag_pager)

        checkAutorotate()
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        //drawer.setDrawerListener(toggle);
        toggle.syncState()

        floatingPlayer = findViewById(R.id.floatingPlayer)

        btnRepeat = findViewById(R.id.btnRepeat)
        btnPlayPause = findViewById(R.id.btnPause)
        val btnNext = findViewById<ImageButton>(R.id.btnNext)
        val btnPrevious = findViewById<ImageButton>(R.id.btnPrevious)
        btnShuffle = findViewById(R.id.btnShuffle)

        btnRepeat!!.setOnClickListener(this)
        btnPlayPause!!.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)
        btnShuffle!!.setOnClickListener(this)

        seekBar = findViewById(R.id.seekBar)
        //Make sure you update Seekbar on UI thread

        val rvQueue = findViewById<RecyclerView>(R.id.rvQueue)

        val mLayoutManager = LinearLayoutManager(this)
        rvQueue.layoutManager = mLayoutManager
        queueAdapter = QueueAdapter(this)
        rvQueue.adapter = queueAdapter
        rvQueue.itemAnimator = DefaultItemAnimator()

        libraryFragment = LibraryFragment()
        libraryFragment.setMainActivity(this)
        libraryFragment.retainInstance = true
        manager = supportFragmentManager

        searchFragment = SearchFragment()
        searchFragment.setMainActivity(this)
        searchFragment.retainInstance = true
        youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance()
        val transaction = manager!!.beginTransaction()
        transaction.add(R.id.floatingPlayer, youTubePlayerFragment).commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bnvFrag)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            item.isChecked = true
            when (item.itemId) {
                R.id.nav_barLibrary -> {
                    fragPager!!.currentItem = 0
                }
                R.id.nav_barSearch -> {
                    fragPager!!.currentItem = 1
                }
                R.id.nav_barPlaylist -> {
                    fragPager!!.currentItem = 2
                }
            }
            false
        }

        playlistFragment = PlaylistFragment()
        playlistFragment.applicationContext = applicationContext
        val frags = mutableListOf(libraryFragment, searchFragment, playlistFragment)
        val adapter = PagerAdapter(manager!!, frags)

        fragPager!!.adapter = adapter
        fragPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            internal var prevMenuItem: MenuItem? = null

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //
            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.isChecked = false
                } else {
                    bottomNavigationView.menu.getItem(0).isChecked = false
                }
                prevMenuItem = bottomNavigationView.menu.getItem(position)
                prevMenuItem!!.isChecked = true
            }

            override fun onPageScrollStateChanged(state: Int) {
                //
            }
        })

        val intent = intent
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val searchItem = menu.findItem(R.id.action_search)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }
        if (searchView != null) {
            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView!!.queryHint = "Search"

            val queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean = true

                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.i("onQueryTextSubmit", query)
                    fragPager!!.currentItem = 1
                    searchFragment.performSearch(query)
                    about = true
                    //lastQuery = query;

                    return true
                }
            }
            searchView!!.setOnQueryTextListener(queryTextListener)
        }
        return true
    }

    override fun onBackPressed() {
        Toast.makeText(this, "isInPlaylist: " + searchFragment.isInPlaylist.toString(), Toast.LENGTH_SHORT).show()
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (about) {
            super.onBackPressed()
            about = false
        } else if (playlistFragment.isViewingPlaylist && fragPager!!.currentItem == 2) {
            playlistFragment.resetAdapter()
        } else if (searchFragment.isInPlaylist && fragPager!!.currentItem == 1) {
            searchFragment.backPress()
        } else {
            backPress += 1
            Toast.makeText(this, " Press Back again to Exit ", Toast.LENGTH_SHORT).show()

            if (backPress > 1) {
                super.onBackPressed()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when (id) {
            R.id.action_settings -> {
                Toast.makeText(this, "Nothing here yet", Toast.LENGTH_SHORT).show()
            }

            R.id.action_about -> {
                val aboutFragment = AboutFragment()
                manager!!.beginTransaction()
                        .replace(R.id.frag_pager, aboutFragment)
                        .addToBackStack("about")
                        .commit()
                about = true
            }

            R.id.share -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (!youtubePlayerLaunched) {
                    Toast.makeText(this, "Nothing is playing", Toast.LENGTH_SHORT).show()
                } else {
                    val ost = yTplayerService!!.getQueueHandler().currentlyPlaying
                    val clip = ClipData.newPlainText("Ost id", ost.url)
                    clipboard.primaryClip = clip
                    Toast.makeText(this, "Link Copied to Clipboard", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.import_osts -> {
                UtilMeths.chooseFileImport(this)
            }

            R.id.export_osts -> {
                UtilMeths.chooseFileExport(this)
            }

            R.id.delete_allOsts -> {
                dbHandler.recreateDatabase(dbHandler.writableDatabase)
                libraryFragment.refreshListView()
            }

            R.id.refresh_tagsTable -> {
                /*SQLiteDatabase sqLiteDatabase = dbHandler.getWritableDatabase();
                String CREATE_TAGS_TABLE = "CREATE TABLE " + "tagsTable" + "("
                        + "tagid" + " INTEGER PRIMARY KEY,"
                        + "tag" + " TEXT " + ")";
                sqLiteDatabase.execSQL(CREATE_TAGS_TABLE);

                String CREATE_SHOW_TABLE = "CREATE TABLE " + "showTable" + "("
                        + "showid" + " INTEGER PRIMARY KEY,"
                        + "show" + " TEXT " + ")";
                sqLiteDatabase.execSQL(CREATE_SHOW_TABLE);*/
                dbHandler.reCreateTagsAndShowTables()
            }

            R.id.clear_tn_storage -> {
                UtilMeths.deleteAllThumbnails(this)
            }
            else -> return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = Runnable { onRequestCode(requestCode, resultCode, data) }
        if (requestCode == 1 || requestCode == 2) {
            checkPermission(this, callback)
        } else {
            onRequestCode(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun onRequestCode(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val currFileURI = data?.data
            IOHandler.readFromFile(currFileURI!!, this)
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            val currFileURI = data?.data
            IOHandler.writeToFile(currFileURI!!, dbHandler.allOsts, this)
        }

        if (requestCode == 3) {
            yTplayerService!!.launchFloater(floatingPlayer!!, this)
        }
    }

    private fun initPlayerService() {
        if (yTplayerService == null) {
            startService()
            doBindService()
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (!youtubePlayerLaunched) {
            Toast.makeText(this, "Play something first bruh :)", Toast.LENGTH_SHORT).show()
        } else {
            when (id) {
                R.id.btnRepeat -> {
                    repeat = if (!repeat) {
                        yTplayerService!!.setRepeating(true)
                        btnRepeat!!.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP)
                        true
                    } else {
                        yTplayerService!!.setRepeating(false)
                        btnRepeat!!.clearColorFilter()
                        false
                    }
                }

                R.id.btnPause -> {
                    yTplayerService!!.pausePlay()
                }

                R.id.btnNext -> {
                    yTplayerService!!.playerNext()
                }

                R.id.btnPrevious -> {
                    yTplayerService!!.playerPrevious()
                }

                R.id.btnShuffle -> {
                    if (shuffleActivated!!) {
                        shuffleOff()
                    } else {
                        shuffleOn()
                    }
                }
            }
        }
    }

    fun pausePlay(playing: Boolean) {
        this.playing = playing
        if (playing) {
            btnPlayPause!!.setImageResource(R.drawable.ic_pause_black_24dp)
        } else {
            btnPlayPause!!.setImageResource(R.drawable.ic_play_arrow_black_24dp)
        }
    }

    fun shuffleOn() {
        yTplayerService!!.getQueueHandler().shuffleOn()
        shuffleActivated = true
        btnShuffle!!.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP)
    }

    private fun shuffleOff() {
        shuffleActivated = false
        yTplayerService!!.getQueueHandler().shuffleOff()
        btnShuffle!!.clearColorFilter()
    }

    fun youtubePlayerStopped() {
        youtubePlayerLaunched = false
        handler.removeCallbacks(seekbarUpdater)
    }

    fun youtubePlayerLaunched() {
        youtubePlayerLaunched = true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Constants.REQUEST_READWRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionCallback?.run()
                } else {
                    launchReadWriteExternalNotGrantedDialog(this)
                }
            }
            Constants.REQUEST_SYSTEM_OVERLAY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionCallback?.run()
                } else {
                    Toast.makeText(this, "I'm sorry, can't play anything without this request :C",
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkAutorotate()
    }

    private fun checkAutorotate() {
        val autoRotate = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
        requestedOrientation = if (autoRotate) {
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        } else {
            ActivityInfo.SCREEN_ORIENTATION_USER
        }
    }

    private fun startService() {
        val serviceIntent = Intent(this@MainActivity, YTplayerService::class.java)
        serviceIntent.action = Constants.STARTFOREGROUND_ACTION
        startService(serviceIntent)
    }

    fun initiatePlayer(ostList: List<Ost>, startId: Int) = if (!youtubePlayerLaunched) {
        initiateSeekbarTimer()
        rlContent!!.removeView(floatingPlayer)
        yTplayerService!!.launchFloater(floatingPlayer!!, this)
        yTplayerService!!.startQueue(ostList, startId, shuffleActivated!!,
                libraryFragment.libListAdapter!!, queueAdapter!!, youTubePlayerFragment!!)
    } else {
        yTplayerService!!.initiateQueue(ostList, startId, shuffleActivated!!)
    }

    private fun doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(Intent(this,
                YTplayerService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        mIsBound = true
    }

    fun doUnbindService() {
        if (mIsBound) {

            // Detach our existing connection.
            unbindService(mConnection)
            mIsBound = false
        }
    }

    public override fun onStop() {
        super.onStop()
        if (youtubePlayerLaunched) {
            yTplayerService!!.refresh()
            saveSession()
        }
        handler.removeCallbacks(seekbarUpdater)
        doUnbindService()
    }

    public override fun onDestroy() {
        if (youtubePlayerLaunched) {
            val closeIntent = Intent(this, YTplayerService::class.java)
            closeIntent.action = Constants.STOPFOREGROUND_ACTION
            startService(closeIntent)
            destroyingActivity = true
        }
        super.onDestroy()
    }

    public override fun onStart() {
        super.onStart()
        checkAutorotate()
        doBindService()
        if (!youtubePlayerLaunched && !addingOstLink) {
            initPlayerService()
        } else {
            handler.postDelayed(seekbarUpdater, 1000)
        }
    }

    private fun addOstLink(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val extras = intent.extras
            val link = extras!!.getString(Intent.EXTRA_TEXT)
            if (link!!.contains("playlist")) {

                val pListName = link.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                val pListId = link.split("list=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                val yPP = YParsePlaylist(pListId, pListName, this)
                yPP.execute()
            } else {
                val yShare = YoutubeShare(link)
                yShare.setContext(this)
                yShare.execute()
            }
            val result = Intent("com.example.RESULT_ACTION", Uri.parse("content://result_uri"))
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
        if (ostFromWidget) {
            startWidgetOst(ostFromWidgetId)
            ostFromWidget = false
            shuffleOn()
        }
        super.onNewIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val intAction = intent.action ?: return
        when (intAction) {
            Constants.NOT_OPEN_ACTIVITY_ACTION -> {
            }// Does nothing so far
            Constants.START_OST -> {
                ostFromWidget = true
                ostFromWidgetId = intent.getIntExtra(getString(R.string.label_ost_of_the_day),
                        -1)
            }
            Intent.ACTION_SEND -> {
                addOstLink(intent)
                addingOstLink = true
            }
            Constants.INITPLAYER -> {
                val osts = intent.getParcelableArrayListExtra<Ost>("osts_extra")
                val startPos = intent.getIntExtra("startIndex", 0)
                initiatePlayer(osts, startPos)
            }
            else -> {
            }
        }
    }

    private fun startWidgetOst(startId: Int) {
        if (startId != -1) {
            if (dbHandler.allOsts.isEmpty()) {
                Toast.makeText(this, "Uh oh, It seems your library is empty :C",
                        Toast.LENGTH_SHORT).show()
            } else {
                initiatePlayer(dbHandler.allOsts, startId)
            }
        }
    }

    private fun initiateSeekbarTimer() {
        val interval = 1000 // 1 Second
        seekbarUpdater = Runnable {
            if (youtubePlayerLaunched && playing) {
                seekBar!!.progress = yTplayerService!!.getPlayer().currentTimeMillis
            }
            handler.postDelayed(seekbarUpdater, interval.toLong())
        }

        handler.postAtTime(seekbarUpdater, System.currentTimeMillis() + interval)
        handler.postDelayed(seekbarUpdater, interval.toLong())
    }

    fun setSeekBarProgress(progress: Int) {
        seekBar!!.progress = progress
    }

    private fun loadLastSession() {
        val lastSessionPrefs = getSharedPreferences(PREFS_NAME, 0)
        val queueString = lastSessionPrefs.getString("lastSession", "")
        val timestamp = lastSessionPrefs.getInt("timeStamp", 0)
        val lastCurr = lastSessionPrefs.getInt("lastCurrPlaying", 0)
        val videoDuration = lastSessionPrefs.getInt("videoDuration", 0)
        if (queueString != "") {
            Log.d("lastQueue", queueString)
            val lastQueueList = UtilMeths.buildOstListFromQueue(queueString!!, dbHandler)
            if (!lastQueueList.isEmpty() && lastCurr < lastQueueList.size) {
                initiatePlayer(lastQueueList, lastCurr)
                yTplayerService!!.getPlayerHandler().loadLastSession(true, timestamp, videoDuration)
            }
        }
        lastSessionLoaded = true
    }

    fun saveSession() {
        val settings = getSharedPreferences(PREFS_NAME, 0)
        val editor = settings.edit()
        val stringBuilder = StringBuilder()
        for (ost in yTplayerService!!.getQueueHandler().ostList) {
            if (ost.id == 0) { //appends the id of the ost since it has been added from search
                stringBuilder.append(ost.videoId).append(",")
            } else {
                stringBuilder.append(ost.id).append(",")
            }
        }
        val idString = stringBuilder.toString()
        editor.putString("lastSession", idString)
        val youTubePlayer = yTplayerService!!.getPlayer()
        val lastCurrentlyPlaying = yTplayerService!!.getQueueHandler().getCurrPlayingIndex()
        editor.putInt("lastCurrPlaying", lastCurrentlyPlaying)
        editor.putInt("timeStamp", youTubePlayer.currentTimeMillis)
        editor.putInt("videoDuration", youTubePlayer.durationMillis)

        // Commit the edits!
        val success = editor.commit()
        val successString = success.toString()
        Log.i("Wrote session", successString)
    }

    companion object {

        var youtubePlayerLaunched = false
        lateinit var dbHandler: DBHandler
            private set
        private var addingOstLink: Boolean = false

        private var permissionCallback: Runnable? = null
        var destroyingActivity = false

        fun setPermissionCallback(permissionCallback: Runnable) {
            MainActivity.permissionCallback = permissionCallback
        }
    }


}
