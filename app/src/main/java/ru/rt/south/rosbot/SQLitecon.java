package ru.rt.south.rosbot;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLitecon extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE TACTIVITY_LOG (" +
                    "DFOBJ INTEGER PRIMARY KEY," +
                    "DFDATE DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "DFACTIVITY TEXT," +
                    "DFDETAILS TEXT" +
                    ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS TACTIVITY_LOG";

    private static SQLitecon sInstance;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Ros.db";

    public static synchronized SQLitecon getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SQLitecon(context.getApplicationContext());
        }
        return sInstance;
    }

    public SQLitecon(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
