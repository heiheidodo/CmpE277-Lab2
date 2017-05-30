package com.swipetry.huiyu.swipeviewtry;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Created by huiyu on 3/16/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    public DatabaseHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }
    public DatabaseHelper(Context context, String name) {
        this(context, name, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        System.out.println("create a database");
        sqLiteDatabase.execSQL("create table user(key_id INTEGER PRIMARY KEY AUTOINCREMENT, property_type varchar(20), street varchar(40), city varchar(40), state varchar(20), zipcode varchar(10), property_price float, down_payment float, apr float, terms Integer, payment float)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        System.out.println("upgrade a database");
    }
}
