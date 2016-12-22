package com.example.a_currency;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DB {

    private static final String DB_NAME = "mydb";
    private static final int DB_VERSION = 1;

    private static final String DB_TABLE_CURRENCY = "currency";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Ccv = "ccv";
    public static final String COLUMN_Img = "img";
    public static final String COLUMN_Buy = "buy";
    public static final String COLUMN_Sale = "sale";
    public static final String COLUMN_Price = "Price";

    private static final String DB_TABLE_HISTORY = "history";
    public static final String COLUMN_Type = "type";
    public static final String COLUMN_Course = "course";
    public static final String COLUMN_Amount = "amount";
    public static final String COLUMN_Sum = "sum";


    private static final String DB_CREATE =
            "create table " + DB_TABLE_CURRENCY + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_Ccv + " text, " +
                    COLUMN_Img + " int, " +
                    COLUMN_Buy + " text," +
                    COLUMN_Sale + " text " +
                    ");";
    private static final String DB_CREATE_HISTORY =
            "create table " + DB_TABLE_HISTORY + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_Ccv + " text, " +
                    COLUMN_Img + " int, " +
                    COLUMN_Type + " text, " +
                    COLUMN_Course + " text, " +
                    COLUMN_Amount + " text, " +
                    COLUMN_Sum + " text " +
            ");";

    private final Context mCtx;


    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    public Cursor getAllData() {
        return mDB.query(DB_TABLE_CURRENCY, null, null, null, null, null, null);
    }

    public Cursor getAllDataHis() {
        return mDB.query(DB_TABLE_HISTORY, null, null, null, null, null, null);
    }

    public Cursor getCourse(String ccv) {
        return mDB.query(DB_TABLE_CURRENCY, new String[] {COLUMN_Buy, COLUMN_Sale}, COLUMN_Ccv +"=?", new String[] { ccv }, null, null, null);
    }

    public void addRec(String ccv, int img, String buy, String sale) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_Ccv, ccv);
        cv.put(COLUMN_Img, img);
        cv.put(COLUMN_Buy, buy);
        cv.put(COLUMN_Sale, sale);

        mDB.insert(DB_TABLE_CURRENCY, null, cv);
    }

    public void addRecHis(String ccv, int img, String type, String course, String amount, String sum) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_Ccv   , ccv);
        cv.put(COLUMN_Img   , img);
        cv.put(COLUMN_Type  , type);
        cv.put(COLUMN_Course, course);
        cv.put(COLUMN_Amount, amount);
        cv.put(COLUMN_Sum   , sum);

        mDB.insert(DB_TABLE_HISTORY, null, cv);
    }

    public void delRec() {
        mDB.delete(DB_TABLE_CURRENCY, null, null);
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
            db.execSQL(DB_CREATE_HISTORY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

}

