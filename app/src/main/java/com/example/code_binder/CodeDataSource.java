package com.example.code_binder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class CodeDataSource {
    private SQLiteDatabase database;
    private DBHelper dbHelper;

    public CodeDataSource(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void clear() {
        dbHelper.clearTable(database);
    }

    public void addData(String data) {
        ContentValues values = new ContentValues();
        values.put("data", data);
        database.insert("dataCodes", null, values);
    }

    public ArrayList<String> getAllData() {
        ArrayList<String> list = new ArrayList<>();
        Cursor cursor = database.query("dataCodes", null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            list.add(cursor.getString(1));
        }
        cursor.close();
        return list;
    }
}
