package com.Anaptixis.AmusingMuseum;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by panos on 18/10/2015.
 */
public class OtherMuseumsSQLiteDB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Museums.db";
    private static final String TABLE_NAME_MUSEUMS = "Museums";

    public static final String COLUMN_MUSEUM_ID = "m_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_MUSEUM_NAME = "museum_name";
    public static final String COLUMN_NUMBER_OF_ROOMS = "number_of_rooms";
    public static final String COLUMN_LOGO = "logo";
    public static final String COLUMN_FLOOR_PLAN = "floor_plan";

    public static final String SQL_DELETE_ENTRIES_FROM_MUSEUMS =
            "DROP TABLE IF EXISTS " + OtherMuseumsSQLiteDB.TABLE_NAME_MUSEUMS;

    public OtherMuseumsSQLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.w("Warn", "DB Constructor!");

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /**  String CREATE_PRODUCTS_TABLE = "CREATE TABLE IF NOT EXISTS " +
         TABLE_NAME_MUSEUMS + "("
         + COLUMN_ID + " INTEGER PRIMARY KEY,"
         + COLUMN_PRODUCTNAME + " TEXT,"
         + COLUMN_QUANTITY + " INTEGER" + ")";
         db.execSQL(CREATE_PRODUCTS_TABLE);
         Log.w("Warn","DB Created!");
         **/
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES_FROM_MUSEUMS);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addData()
    {
        /**      ContentValues values = new ContentValues();
         values.put(COLUMN_PRODUCTNAME, "myname");//queue
         values.put(COLUMN_QUANTITY, "myquantity");

         SQLiteDatabase db = this.getWritableDatabase();

         db.insert(TABLE_NAME_MUSEUMS, null, values);
         db.close();
         Log.w("Warn", "Data Added");
         **/
    }

    public void getData()
    {
        String query = "Select * FROM " + TABLE_NAME_MUSEUMS;

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);   //Printing data using cursor

        while (cursor.moveToNext())
        {
            Log.w("Warn", cursor.getString(0));
            Log.w("Warn", cursor.getString(1));
            Log.w("Warn", cursor.getString(2));
        }
        Log.w("Warn","All data shown");
        cursor.close();
        db.close();
    }
}