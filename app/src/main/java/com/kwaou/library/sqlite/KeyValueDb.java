package com.kwaou.library.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Manish on 2/13/2017.
 */

public class KeyValueDb extends SQLiteOpenHelper {
    private static final String TAG = "KeyValueDB";
    private static KeyValueDb sInstance;
    Context context;

    private static final String DATABASE_NAME = "app";
    private static final String DATABASE_TABLE = "cache";
    private static final int DATABASE_VERSION = 1;

    private static final String KEY = "KEY";
    private static final String VALUE = "VALUE";
    private static final String PERSIST = "PERSIST";
    private static final String KEY_CREATED_AT = "KEY_CREATED_AT";


    private static final String CREATE_TABLE = "CREATE TABLE "
            + DATABASE_TABLE + "(" + KEY + " TEXT PRIMARY KEY," + VALUE
            + " TEXT," + PERSIST + " INTEGER," + KEY_CREATED_AT
            + " DATETIME" + ")";

    private static synchronized KeyValueDb getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new KeyValueDb(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     *
     * @param context Any context object.
     */
    public KeyValueDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG, "onCreate");
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }

    /**
     * Setter method. Sets a (key, value) pair in sqlite3 db.
     *
     * @param context Any context object.
     * @param key     The URL or some other unique id for data can be used
     * @param value   String data to be saved
     * @param persist Whether to delete this (key, value, time, persist) tuple, when cleaning cache in
     *                clearCacheByLimit() method. 1 Means persist, 0 Means remove.
     * @return rowid of the insertion row
     */
    public static synchronized long set(Context context, String key, String value, Integer persist) {
        key = DatabaseUtils.sqlEscapeString(key);
        Log.v(TAG, "setting cache: " + key);
        KeyValueDb dbHelper = getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long row = 0;
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(KEY, key);
            values.put(VALUE, value);
            values.put(PERSIST, persist);
            values.put(KEY_CREATED_AT, "time('now')");
            row = db.replace(DATABASE_TABLE, null, values);
            Log.v(TAG, "save cache size: " + String.valueOf(value.length()));
            db.close();
        }
        return row;
    }


    public static synchronized long update(Context context, String key, String value, Integer persist) {
        key = DatabaseUtils.sqlEscapeString(key);
        Log.v(TAG, "setting cache: " + key);
        KeyValueDb dbHelper = getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long row = 0;
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(KEY, key);
            values.put(VALUE, value);
            values.put(PERSIST, persist);
            values.put(KEY_CREATED_AT, "time('now')");

            row = db.update(DATABASE_TABLE, values,KEY + "=?",new String[]{key});
            Log.v(TAG, "save cache size: " + String.valueOf(value.length()));
            db.close();
        }
        return row;
    }


    /**
     * @param context      Any context object.
     * @param key          The URL or some other unique id for data can be used
     * @param defaultValue value to be returned in case something goes wrong or no data is found
     * @return value stored in DB if present, defaultValue otherwise.
     */
    public static synchronized String get(Context context, String key, String defaultValue) {
        key = DatabaseUtils.sqlEscapeString(key);
        Log.v(TAG, "getting cache: " + key);
        KeyValueDb dbHelper = getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String value = defaultValue;
        if (db != null) {
            Cursor c = db.query(DATABASE_TABLE, new String[]{VALUE}, KEY + "=?", new String[]{key}, null, null, null);
            if (c != null) {
                if (c.moveToNext()) {
                    value = c.getString(c.getColumnIndex(VALUE));
                }
                Log.v(TAG, "get cache size:" + String.valueOf(value.length()));
                c.close();
            }
            db.close();
        }
        return value;
    }

    /**
     * Clear the cache like a FIFO queue defined by the limit parameter.
     * Each function call made to this will remove count(*)-limit first rows from the DB
     * Only the data with (Persist, 0) will be removed
     *
     * @param context Any context object.
     * @param limit   amount of data to be retained in FIFO, rest would be removed like a queue
     * @return number of rows affected on success
     */
    public static synchronized long clearCacheByLimit(Context context, long limit) {
        KeyValueDb dbHelper = getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long numRows = 0;
        if (db != null) {
            Cursor c = db.query(DATABASE_TABLE, null, null, null, null, null, null);
            if (c != null) {
                long count = c.getCount();
                Log.v(TAG, "cached rows" + String.valueOf(count));
                if (count > limit) {
                    String ALTER_TBL = "DELETE FROM " + DATABASE_TABLE +
                            " WHERE " + KEY + " IN (SELECT " + KEY + " FROM " + DATABASE_TABLE + " WHERE " + PERSIST + " = 0" + " ORDER BY " + KEY_CREATED_AT + " ASC LIMIT " + String.valueOf(count - limit) + ");";
                    db.execSQL(ALTER_TBL);
                }
                c = db.query(DATABASE_TABLE, null, null, null, null, null, null);
                numRows = count - c.getCount();
                c.close();
            }
            db.close();
        }
        return numRows;
    }
}
