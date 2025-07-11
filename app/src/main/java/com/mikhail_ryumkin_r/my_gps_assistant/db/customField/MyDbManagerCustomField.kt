package com.mikhail_ryumkin_r.my_gps_assistant.db.customField

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import com.mikhail_ryumkin_r.my_gps_assistant.db.MyDbHelper
import com.mikhail_ryumkin_r.my_gps_assistant.db.MyDbNameClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MyDbManagerCustomField(context: Context) {

    private val myDbHelper = MyDbHelper(context)
    var db: SQLiteDatabase? = null

    fun openDb(){
        db = myDbHelper.writableDatabase
    }

    // Функции для сохранения произвольных полей чётных поездов

    fun insertToDbCustomFieldChet(startCustomFieldChet: Int, picketStartCustomFieldChet: Int, customField: String){
        val values = ContentValues().apply {
            put(MyDbNameClass.COLUMN_START_CUSTOM_FIELD_CHET, startCustomFieldChet)
            put(MyDbNameClass.COLUMN_PICKET_START_CUSTOM_FIELD_CHET, picketStartCustomFieldChet)
            put(MyDbNameClass.COLUMN_CUSTOM_FIELD_CHET, customField)
        }
        db?.insert(MyDbNameClass.TABLE_NAME_CUSTOM_FIELD_CHET, null, values)
    }

    @SuppressLint("Range")
    suspend fun readDbDataCustomFieldChet() : ArrayList<ListItemCustomFieldChet> = withContext(Dispatchers.IO) {
        val dataListCustomFieldChet = ArrayList<ListItemCustomFieldChet>()

        val cursor = db?.query(MyDbNameClass.TABLE_NAME_CUSTOM_FIELD_CHET, null,null,null,null,null,null)

        while (cursor?.moveToNext()!!){
            val dataStartCustomFieldChet: Int = cursor.getInt(cursor.getColumnIndex(MyDbNameClass.COLUMN_START_CUSTOM_FIELD_CHET))
            val dataPicketStartCustomFieldChet: Int = cursor.getInt(cursor.getColumnIndex(MyDbNameClass.COLUMN_PICKET_START_CUSTOM_FIELD_CHET))
            val customFieldChet: String = cursor.getString(cursor.getColumnIndex(MyDbNameClass.COLUMN_CUSTOM_FIELD_CHET))
            val dataIdCustomFieldChet = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
            val itemCustomFieldChet = ListItemCustomFieldChet()
            itemCustomFieldChet.startChetCustom = dataStartCustomFieldChet
            itemCustomFieldChet.picketStartChetCustom = dataPicketStartCustomFieldChet
            itemCustomFieldChet.fieldChetCustom = customFieldChet
            itemCustomFieldChet.idChetCustom = dataIdCustomFieldChet
            dataListCustomFieldChet.add(itemCustomFieldChet)
        }
        cursor.close()

        return@withContext dataListCustomFieldChet
    }

    fun updateDbDataCustomFieldChet(startCustomFieldChet: Int, picketStartCustomFieldChet: Int, customField: String, idCustomFieldChet: Int){
        val selection = BaseColumns._ID + "=$idCustomFieldChet"
        val values = ContentValues().apply {
            put(MyDbNameClass.COLUMN_START_CUSTOM_FIELD_CHET, startCustomFieldChet)
            put(MyDbNameClass.COLUMN_PICKET_START_CUSTOM_FIELD_CHET, picketStartCustomFieldChet)
            put(MyDbNameClass.COLUMN_CUSTOM_FIELD_CHET, customField)
        }
        db?.update(MyDbNameClass.TABLE_NAME_CUSTOM_FIELD_CHET, values, selection, null)
    }

    fun deleteDbDataCustomFieldChet(idCustomFieldChet: Int){
        val selection = BaseColumns._ID + "=$idCustomFieldChet"
        db?.delete(MyDbNameClass.TABLE_NAME_CUSTOM_FIELD_CHET, selection, null)
    }

    //---------------------------------------------------------------------------------------------------//

    // Функции для сохранения произвольных полей нечётных поездов

    fun insertToDbCustomFieldNechet(startCustomFieldNechet: Int, picketStartCustomFieldNechet: Int, customField: String){
        val values = ContentValues().apply {
            put(MyDbNameClass.COLUMN_START_CUSTOM_FIELD_NECHET, startCustomFieldNechet)
            put(MyDbNameClass.COLUMN_PICKET_START_CUSTOM_FIELD_NECHET, picketStartCustomFieldNechet)
            put(MyDbNameClass.COLUMN_CUSTOM_FIELD_NECHET, customField)
        }
        db?.insert(MyDbNameClass.TABLE_NAME_CUSTOM_FIELD_NECHET, null, values)
    }

    @SuppressLint("Range")
    suspend fun readDbDataCustomFieldNechet() : ArrayList<ListItemCustomFieldNechet> = withContext(Dispatchers.IO) {
        val dataListCustomFieldNechet = ArrayList<ListItemCustomFieldNechet>()

        val cursor = db?.query(MyDbNameClass.TABLE_NAME_CUSTOM_FIELD_NECHET, null,null,null,null,null,null)

        while (cursor?.moveToNext()!!){
            val dataStartCustomFieldNechet: Int = cursor.getInt(cursor.getColumnIndex(MyDbNameClass.COLUMN_START_CUSTOM_FIELD_NECHET))
            val dataPicketStartCustomFieldNechet: Int = cursor.getInt(cursor.getColumnIndex(MyDbNameClass.COLUMN_PICKET_START_CUSTOM_FIELD_NECHET))
            val customFieldNechet: String = cursor.getString(cursor.getColumnIndex(MyDbNameClass.COLUMN_CUSTOM_FIELD_NECHET))
            val dataIdCustomFieldNechet = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
            val itemCustomFieldNechet = ListItemCustomFieldNechet()
            itemCustomFieldNechet.startNechetCustom = dataStartCustomFieldNechet
            itemCustomFieldNechet.picketStartNechetCustom = dataPicketStartCustomFieldNechet
            itemCustomFieldNechet.fieldNechetCustom = customFieldNechet
            itemCustomFieldNechet.idNechetCustom = dataIdCustomFieldNechet
            dataListCustomFieldNechet.add(itemCustomFieldNechet)
        }
        cursor.close()

        return@withContext dataListCustomFieldNechet
    }

    fun updateDbDataCustomFieldNechet(startCustomFieldNechet: Int, picketStartCustomFieldNechet: Int, customField: String, idCustomFieldNechet: Int){
        val selection = BaseColumns._ID + "=$idCustomFieldNechet"
        val values = ContentValues().apply {
            put(MyDbNameClass.COLUMN_START_CUSTOM_FIELD_NECHET, startCustomFieldNechet)
            put(MyDbNameClass.COLUMN_PICKET_START_CUSTOM_FIELD_NECHET, picketStartCustomFieldNechet)
            put(MyDbNameClass.COLUMN_CUSTOM_FIELD_NECHET, customField)
        }
        db?.update(MyDbNameClass.TABLE_NAME_CUSTOM_FIELD_NECHET, values, selection, null)
    }

    fun deleteDbDataCustomFieldNechet(idCustomFieldNechet: Int){
        val selection = BaseColumns._ID + "=$idCustomFieldNechet"
        db?.delete(MyDbNameClass.TABLE_NAME_CUSTOM_FIELD_NECHET, selection, null)
    }

    //---------------------------------------------------------------------------------------------------//

    fun  closeDb(){
        myDbHelper.close()
    }
}