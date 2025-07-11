package com.mikhail_ryumkin_r.my_gps_assistant.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDbHelper(context: Context) : SQLiteOpenHelper(context, MyDbNameClass.DATABASE_NAME,
    null, MyDbNameClass.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(MyDbNameClass.CREATE_TABLE_REDACKTOR_CHET)
        db?.execSQL(MyDbNameClass.CREATE_TABLE_REDACKTOR_NECHET)
        db?.execSQL(MyDbNameClass.CREATE_TABLE_LIMITATIONS_CHET)
        db?.execSQL(MyDbNameClass.CREATE_TABLE_LIMITATIONS_NECHET)
        db?.execSQL(MyDbNameClass.CREATE_TABLE_PANTOGRAPH_CHET)
        db?.execSQL(MyDbNameClass.CREATE_TABLE_PANTOGRAPH_NECHET)
        db?.execSQL(MyDbNameClass.CREATE_TABLE_BRAKE_CHET)
        db?.execSQL(MyDbNameClass.CREATE_TABLE_BRAKE_NECHET)
        db?.execSQL(MyDbNameClass.CREATE_TABLE_CUSTOM_FIELD_CHET)
        db?.execSQL(MyDbNameClass.CREATE_TABLE_CUSTOM_FIELD_NECHET)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(MyDbNameClass.SQL_DELETE_TABLE_REDACKTOR_CHET)
        db?.execSQL(MyDbNameClass.SQL_DELETE_TABLE_REDACKTOR_NECHET)
        db?.execSQL(MyDbNameClass.SQL_DELETE_TABLE_LIMITATIONS_CHET)
        db?.execSQL(MyDbNameClass.SQL_DELETE_TABLE_LIMITATIONS_NECHET)
        db?.execSQL(MyDbNameClass.SQL_DELETE_TABLE_PANTOGRAPH_CHET)
        db?.execSQL(MyDbNameClass.SQL_DELETE_TABLE_PANTOGRAPH_NECHET)
        db?.execSQL(MyDbNameClass.SQL_DELETE_TABLE_BRAKE_CHET)
        db?.execSQL(MyDbNameClass.SQL_DELETE_TABLE_BRAKE_NECHET)
        db?.execSQL(MyDbNameClass.SQL_DELETE_TABLE_CUSTOM_FIELD_CHET)
        db?.execSQL(MyDbNameClass.SQL_DELETE_TABLE_CUSTOM_FIELD_NECHET)
        onCreate(db)
    }

}