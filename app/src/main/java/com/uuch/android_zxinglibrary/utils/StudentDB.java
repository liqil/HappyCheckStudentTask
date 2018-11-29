package com.uuch.android_zxinglibrary.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by liqil on 2018/10/30.
 */

public class StudentDB extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "Orders";
    private static final int DB_VERSION = 1;
    public String dbName;

    public StudentDB(Context context, String name) {
        super(context, name, null, DB_VERSION);
        dbName = name;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i("liqil", "create Database------------->"+TABLE_NAME);
        String sql = "create table if not exists " + TABLE_NAME + " (StudentId integer primary key, StudentName varchar(50), Sex varchar(10))";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.i("liqil", "update Database------------->");
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }
}
