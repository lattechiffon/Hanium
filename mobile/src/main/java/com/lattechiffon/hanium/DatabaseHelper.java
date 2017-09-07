package com.lattechiffon.hanium;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, "Falling.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE FALLING_RECORD(no INTEGER PRIMARY KEY AUTOINCREMENT, datetime TEXT, result INTEGER, valid INTEGER);");
        db.execSQL("CREATE TABLE CONTACTS(no INTEGER PRIMARY KEY AUTOINCREMENT, userNo INTEGER, name TEXT, phone TEXT, date TEXT, valid INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void update(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public void delete(String _query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(_query);
        db.close();
    }

    public boolean checkProtector(int userNo) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM CONTACTS WHERE userNo = " + userNo + " AND valid = 1;", null);

        int resultCount = cursor.getCount();

        if (resultCount == 1) {
            return true;
        }

        return false;

    }

    public String[] selectProtectorAll() {
        SQLiteDatabase db = getReadableDatabase();
        int count = 0;

        Cursor cursor = db.rawQuery("SELECT * FROM CONTACTS WHERE valid = 1 ORDER BY no DESC;", null);

        int resultCount = cursor.getCount();

        if (resultCount == 0) {
            String[] retStr = new String[1];

            retStr[0] = "0";

            return retStr;
        }

        String[] retStr = new String[resultCount];

        while (cursor.moveToNext()) {
            retStr[count] = "" + cursor.getInt(1);

            count ++;
        }

        return retStr;
    }

    public String[][] selectFallingRecordAll() {
        SQLiteDatabase db = getReadableDatabase();
        //String retStr[][] = { new String[4] };
        int count = 0;

        Cursor cursor = db.rawQuery("SELECT * FROM FALLING_RECORD WHERE valid = 1 ORDER BY no DESC;", null);

        int resultCount = cursor.getCount();

        if (resultCount == 0) {
            String[][] retStr = new String[1][4];

            retStr[0][0] = "0";
            retStr[0][1] = "낙상 인식 기록이 없습니다.";
            retStr[0][2] = "1";
            retStr[0][3] = "0";

            return retStr;
        }

        String[][] retStr = new String[resultCount][4];

        while (cursor.moveToNext()) {
            retStr[count][0] = "" + cursor.getInt(0);
            retStr[count][1] = cursor.getString(1);
            retStr[count][2] = "" + cursor.getInt(2);
            retStr[count][3] = "" + cursor.getInt(3);

            count ++;
        }

        return retStr;
    }

    public int selectTopNo() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT no FROM FALLING_RECORD ORDER BY no DESC LIMIT 1;", null);
        cursor.moveToNext();

        return cursor.getInt(0);
    }

    public String PrintData() {
        SQLiteDatabase db = getReadableDatabase();
        String str = "";

        Cursor cursor = db.rawQuery("select * from FOOD_LIST", null);
        while(cursor.moveToNext()) {
            str += cursor.getInt(0)
                    + " : foodName "
                    + cursor.getString(1)
                    + ", price = "
                    + cursor.getInt(2)
                    + "\n";
        }

        return str;
    }
}
