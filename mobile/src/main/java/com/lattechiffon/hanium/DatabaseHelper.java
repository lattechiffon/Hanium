package com.lattechiffon.hanium;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite 데이터베이스의 쿼리 처리를 담당하는 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
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

    /**
     * 데이터베이스 삽입 질의를 수행하는 메서드입니다.
     *
     * @param query 데이터베이스 삽입 질의
     */
    void insert(String query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    /**
     * 데이터베이스 수정 질의를 수행하는 메서드입니다.
     *
     * @param query 데이터베이스 수정 질의
     */
    void update(String query) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    /**
     * 특정 연락처의 보호자 지정 여부를 반환하는 메서드입니다.
     *
     * @param userNo 연락처 관리 번호
     * @return 보호자 지정 여부
     */
    boolean checkProtector(int userNo) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM CONTACTS WHERE userNo = " + userNo + " AND valid = 1;", null);

        int resultCount = cursor.getCount();

        cursor.close();

        return resultCount == 1;

    }

    /**
     * 보호자로 지정된 모든 연락처의 관리 번호를 반환하는 메서드입니다.
     *
     * @return 모든 보호자의 연락처 관리 번호
     */
    String[] selectProtectorAll() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM CONTACTS WHERE valid = 1 ORDER BY no DESC;", null);

        int count = 0;
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

    /**
     * 보호자로 지정된 연락처의 수를 반환하는 메서드입니다.
     *
     * @return 등록된 보호자 수
     */
    int countProtector() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM CONTACTS WHERE valid = 1 ORDER BY no DESC;", null);

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    /**
     * 모든 낙상사고 발생 기록을 반환하는 메서드입니다.
     *
     * @return 모든 낙상사고 발생 기록 관리 번호
     */
    String[][] selectFallingRecordAll() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM FALLING_RECORD WHERE valid = 1 ORDER BY no DESC;", null);

        int count = 0;
        int resultCount = cursor.getCount();

        if (resultCount == 0) {
            String[][] retStr = new String[1][4];

            retStr[0][0] = "0";
            retStr[0][1] = "null";
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

    /**
     * 가장 최근에 등록된 낙상사고 발생 기록을 반환하는 메서드입니다.
     *
     * @return 가장 최근에 등록된 낙상사고 발생 기록 관리 번호
     */
    int selectTopNo() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT no FROM FALLING_RECORD ORDER BY no DESC LIMIT 1;", null);

        cursor.moveToNext();

        int retInt = cursor.getInt(0);

        cursor.close();

        return retInt;
    }
}
