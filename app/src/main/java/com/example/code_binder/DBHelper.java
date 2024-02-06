package com.example.code_binder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "codebinder.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS dataCodes ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "data TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS dataCodes");
        onCreate(db);
    }

    public void clearTable(SQLiteDatabase db) {
        db.execSQL("DELETE FROM dataCodes;");
        db.execSQL("UPDATE SQLITE_SEQUENCE SET seq = 0 WHERE name = 'dataCodes';");
    }
}
