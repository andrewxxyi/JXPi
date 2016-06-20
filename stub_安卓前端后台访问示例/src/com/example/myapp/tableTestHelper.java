package com.example.myapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by andrew on 16-5-31.
 */
public class tableTestHelper extends SQLiteOpenHelper {
    //调用父类构造器
    public tableTestHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                           int version) {
        super(context, name, factory, version);
    }

    /**
     * 当数据库首次创建时执行该方法，一般将创建表等初始化操作放在该方法中执行.
     * 重写onCreate方法，调用execSQL方法创建表
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists tableTest("
                + "id integer primary key,"
                + "name varchar,"
                + "level integer)");

    }

    //当打开数据库时传入的版本号与当前的版本号不同时会调用该方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insert(int id, String name, int level) {
//获取数据库对象
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("level", level);

        db.insert("tableTest", null, values);
        db.close();
    }

    public void update(int id, String name, int level) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("level", level);
        db.update("tableTest", values, "id = " + id, null);

        //关闭SQLiteDatabase对象
        db.close();
    }

    public void delete(int id) {
        //获取数据库对象
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereValue = {Integer.toString(id)};
        db.delete("tableTest", "id = ?", whereValue);
        db.close();
    }

    public Cursor select(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String where = "id = ?";
        String[] whereValue = {Integer.toString(id)};
        Cursor cursor = db.query("tableTest", new String[]{"id", "name", "level"}, where, whereValue, null, null, null);
        return cursor;
    }

    public Cursor select(String colName, String value) {
        SQLiteDatabase db = this.getReadableDatabase();
        String where = colName + " = ?";
        String[] whereValue = {value};
        Cursor cursor = db.query("tableTest", new String[]{"id", "name", "level"}, where, whereValue, null, null, null);
        return cursor;
    }
}
