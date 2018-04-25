import QueryStrings.Fields.FKEY_OST_ID
import QueryStrings.Fields.FKEY_OST_SHOW_ID
import QueryStrings.Fields.FKEY_PLAYLIST_ID
import QueryStrings.Fields.KEY_ENTRY
import QueryStrings.Fields.KEY_OST_ID
import QueryStrings.Fields.KEY_OST_TAG
import QueryStrings.Fields.KEY_OST_TITLE
import QueryStrings.Fields.KEY_OST_URL
import QueryStrings.Fields.KEY_SHOW
import QueryStrings.Fields.KEY_SHOW_ID
import QueryStrings.Fields.OST_TABLE
import QueryStrings.Fields.PLAYLIST_OST_TABLE
import QueryStrings.Fields.SHOW_TABLE

object QueryStrings {
    //TODO Make Actual Not Garbage Query
    const val selectAllOstsInPlaylist = "SELECT $KEY_OST_ID,$KEY_OST_TITLE," +
            "$KEY_SHOW,$KEY_OST_TAG, $KEY_OST_URL " +
            "FROM " +
            "$PLAYLIST_OST_TABLE " +
            "INNER JOIN $SHOW_TABLE ON $KEY_SHOW_ID = $FKEY_OST_SHOW_ID " +
            "INNER JOIN $OST_TABLE ON $KEY_OST_ID = $FKEY_OST_ID " +
            "WHERE $PLAYLIST_OST_TABLE.$FKEY_PLAYLIST_ID = ?" +
            "ORDER BY $KEY_ENTRY"


    const val selectOstQuery = "SELECT $KEY_OST_ID,$KEY_OST_TITLE,$KEY_SHOW,$KEY_OST_TAG,$KEY_OST_URL " +
            "FROM " +
            "$OST_TABLE " +
            "INNER JOIN $SHOW_TABLE ON $KEY_SHOW_ID = $FKEY_OST_SHOW_ID " +
            "WHERE $KEY_OST_ID IS ?"

    const val selectAllOsts = "SELECT $KEY_OST_ID,$KEY_OST_TITLE,$KEY_SHOW,$KEY_OST_TAG,$KEY_OST_URL " +
            "FROM " +
            "$OST_TABLE " +
            "INNER JOIN $SHOW_TABLE ON $KEY_SHOW_ID = $FKEY_OST_SHOW_ID"

    internal object Fields {

        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "ostdb"
        const val OST_TABLE = "ostTable"
        const val SHOW_TABLE = "showTable"
        const val TAGS_TABLE = "tagsTable"
        const val PLAYLIST_TABLE = "playListTable"
        const val PLAYLIST_OST_TABLE = "playListOstTable"
        //private const val OST_SHOW_TABLE = "ostShowTable"
        const val OST_TAGS_TABLE = "ostTagsTable"

        const val KEY_OST_ID = "ostid"
        const val KEY_OST_TITLE = "title"
        const val KEY_SHOW = "show"
        const val KEY_OST_URL = "url"
        const val KEY_OST_TAG = "tag"

        const val KEY_PLAYLIST_ID = "playlistId"
        const val KEY_PLAYLIST_NAME = "playlistName"
        const val KEY_TAG_ID = "tagId"
        const val KEY_SHOW_ID = "showId"

        const val FKEY_PLAYLIST_ID = "fkey_playlist_id"
        const val FKEY_OST_ID = "fkey_ost_id"
        const val FKEY_TAG_ID = "tag_id"
        const val FKEY_SHOW_ID = "show_id"
        const val FKEY_OST_SHOW_ID = "ostShowId"
        const val FKEY_OST_TAG_ID = "ostTagId"
        const val KEY_ENTRY = "rowId"
    }
}