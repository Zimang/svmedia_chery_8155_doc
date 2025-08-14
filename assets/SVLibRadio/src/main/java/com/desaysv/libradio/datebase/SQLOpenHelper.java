package com.desaysv.libradio.datebase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import android.util.Log;

/**
 * Created by LZM on 2019-8-9.
 * Comment 数据库的工具类
 */
public class SQLOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLOpenHelper";

    private static SQLOpenHelper instance;

    private final static String DB_NAME = "radio_list.db";

    private final static int DB_VERSION = 10;

    public static SQLOpenHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (SQLOpenHelper.class) {
                if (instance == null) {
                    instance = new SQLOpenHelper(context, DB_NAME, null, DB_VERSION);
                }
            }
        }
        return instance;
    }

    public SQLOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: table");
        db.execSQL("create table if not exists RADIO_MESSAGE_TABLE(_id integer primary key autoincrement," +
                RadioDataBaseKey.TYPE + " varchar(20)," + RadioDataBaseKey.FREQUENCY + " varchar(10)," +
                RadioDataBaseKey.BAND + " varchar(10),"+
                        RadioDataBaseKey.ENSEMBLE_ID + " varchar(20)," +
                        RadioDataBaseKey.PROGRAM_STATION_NAME + " varchar(100)," +
                        RadioDataBaseKey.ENSEMBLE_LABEL + " varchar(50)," +
                        RadioDataBaseKey.PROGRAM_TYPE + " varchar(20)," +
                        RadioDataBaseKey.SERVICE_ID + " varchar(20)," +
                        RadioDataBaseKey.SERVICE_COMPONENT_ID + " varchar(20)," +
                        RadioDataBaseKey.DYNAMIC_LABEL + " varchar(100)," +
                        RadioDataBaseKey.PROGRAM_STATION_SHORT_NAME + " varchar(100)," +
                        RadioDataBaseKey.ENSEMBLE_LABEL_SHORT_NAME + " varchar(100)," +
                        RadioDataBaseKey.SUB_SERVICE_FLAG + " varchar(20)," +
                        RadioDataBaseKey.DYNAMIC_PLUS_LABEL + " varchar(2000))");

        Log.d(TAG, "onCreate: table " + RadioDataBaseKey.TABLE_EPG);
        db.execSQL("create table if not exists " + RadioDataBaseKey.TABLE_EPG +
                        "(_id integer primary key autoincrement," +
                        RadioDataBaseKey.EPG_SERVICE_ID + " varchar(20)," +
                        RadioDataBaseKey.EPG_FREQ + " varchar(20)," +
                        RadioDataBaseKey.EPG_SERVICE_COMPONENT_ID + " varchar(20)," +
                        RadioDataBaseKey.EPG_PROGRAM_NAME + " varchar(100)," +
                        RadioDataBaseKey.EPG_YEAR + " varchar(20)," +
                        RadioDataBaseKey.EPG_MONTH + " varchar(20)," +
                        RadioDataBaseKey.EPG_DAY + " varchar(20)," +
                        RadioDataBaseKey.EPG_HOUR + " varchar(20)," +
                        RadioDataBaseKey.EPG_MIN + " varchar(20)," +
                        RadioDataBaseKey.EPG_SEC + " varchar(20))"
        );

        Log.d(TAG, "onCreate: table " + RadioDataBaseKey.TABLE_SEARCH);
        db.execSQL("create table if not exists " + RadioDataBaseKey.TABLE_SEARCH +
                "(_id integer primary key autoincrement," +
                RadioDataBaseKey.RADIO_SEARCH_TIME + " varchar(60)," +
                RadioDataBaseKey.RADIO_SEARCH_NAME + " varchar(20))"
        );

        Log.d(TAG, "onCreate: table " + RadioDataBaseKey.TABLE_RDS_NAME);
        db.execSQL("create table if not exists " + RadioDataBaseKey.TABLE_RDS_NAME +
                "(_id integer primary key autoincrement," +
                RadioDataBaseKey.FREQUENCY + " varchar(60)," +
                RadioDataBaseKey.RDS_STATION_NAME + " varchar(20))"
        );

        Log.d(TAG, "onCreate: table " + RadioDataBaseKey.TABLE_DAB_LOGO);
        db.execSQL("create table if not exists " + RadioDataBaseKey.TABLE_DAB_LOGO +
                "(_id integer primary key autoincrement," +
                RadioDataBaseKey.FREQUENCY + " varchar(10)," +
                RadioDataBaseKey.SERVICE_ID + " varchar(20)," +
                RadioDataBaseKey.SERVICE_COMPONENT_ID + " varchar(20)," +
                RadioDataBaseKey.DAB_LOGO_LEN + " varchar(100)," +
                RadioDataBaseKey.DAB_LOGO_DATA + " BLOB)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists RADIO_MESSAGE_TABLE");
        db.execSQL("drop table if exists " + RadioDataBaseKey.TABLE_EPG);
        db.execSQL("drop table if exists " + RadioDataBaseKey.TABLE_SEARCH);
        db.execSQL("drop table if exists " + RadioDataBaseKey.TABLE_RDS_NAME);
        db.execSQL("drop table if exists " + RadioDataBaseKey.TABLE_DAB_LOGO);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists RADIO_MESSAGE_TABLE");
        db.execSQL("drop table if exists " + RadioDataBaseKey.TABLE_EPG);
        db.execSQL("drop table if exists " + RadioDataBaseKey.TABLE_SEARCH);
        db.execSQL("drop table if exists " + RadioDataBaseKey.TABLE_RDS_NAME);
        db.execSQL("drop table if exists " + RadioDataBaseKey.TABLE_DAB_LOGO);
        onCreate(db);
    }
}
