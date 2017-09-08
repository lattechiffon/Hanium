package com.lattechiffon.hanium;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite 데이터베이스의 쿼리 처리를 담당하는 클래스입니다.
 * @version : 1.0
 * @author  : Yongguk Go (lattechiffon@gmail.com)
 */
class DatabaseHelper extends SQLiteOpenHelper {
    DatabaseHelper(Context context) {
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

    void insert(String query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    void update(String query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    boolean checkProtector(int userNo) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM CONTACTS WHERE userNo = " + userNo + " AND valid = 1;", null);

        int resultCount = cursor.getCount();

        cursor.close();

        return resultCount == 1;

    }

    String[] selectProtectorAll() {
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

            count++;
        }

        cursor.close();

        return retStr;
    }

    String[][] selectFallingRecordAll() {
        SQLiteDatabase db = getReadableDatabase();
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

            count++;
        }

        cursor.close();

        return retStr;
    }

    int selectTopNo() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT no FROM FALLING_RECORD ORDER BY no DESC LIMIT 1;", null);
        cursor.moveToNext();

        int retInt = cursor.getInt(9);

        cursor.close();

        return retInt;
    }
}
