package com.processmap.mobilepro.data.common;

import android.content.ContentValues;
import android.database.Cursor;

import com.processmap.mobilepro.data.base.BaseEntity;

import org.json.JSONObject;

public class ModuleEntity extends BaseEntity {

    public Integer Id;
    public String Name;
    public String AbbreviatedName;

    // Database table
    private static final String TABLE_NAME              = "module";
    public static final String COLUMN_ID                = "Id";
    public static final String COLUMN_NAME              = "Description";
    public static final String COLUMN_ABBREVIATED_NAME  = "AbbreviatedName";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    protected static void getColumnsWithTypeArray(ContentValues pairs) {
        pairs.put(COLUMN_ID, COLUMN_TYPE_PRIMARY_KEY);
        pairs.put(COLUMN_NAME, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_ABBREVIATED_NAME, COLUMN_TYPE_TEXT);

        BaseEntity.getColumnsWithTypeArray(pairs);
    }

    @Override
    public void getValues(ContentValues pairs) {
        pairs.put(COLUMN_ID, Id);
        pairs.put(COLUMN_NAME, Name);
        pairs.put(COLUMN_ABBREVIATED_NAME, AbbreviatedName);
        super.getValues(pairs);
    }

    //----------------------------------------------------------------------------------------------
    public ModuleEntity(JSONObject json) {
        super(json);
        Id = jsonToInt(json, COLUMN_ID);
        Name = jsonToString(json, COLUMN_NAME);
        AbbreviatedName = jsonToString(json, COLUMN_ABBREVIATED_NAME);
    }

    public ModuleEntity(Cursor cursor) {
        super(cursor);

        Id = dbToInt(cursor, COLUMN_ID);
        Name = dbToString(cursor, COLUMN_NAME);
        AbbreviatedName = dbToString(cursor, COLUMN_ABBREVIATED_NAME);
    }

    //----------------------------------------------------------------------------------------------
    //database functions
    //
    public static String getCreateStatement() {
        ContentValues pairs = new ContentValues();
        getColumnsWithTypeArray(pairs);
        return composeCreateStatement(TABLE_NAME, pairs);
    }

    public static String selectByGUID(String guid) {
        return "select * from " + TABLE_NAME + " where guid='" + guid + "'";
    }

    public static String selectById(Integer Id) {
        return String.format("select * from %s where %s=%d", TABLE_NAME, COLUMN_ID, Id);
    }

    public static String selectByIndex(Integer index) {
        return String.format("select * from %s limit 1 offset %d", TABLE_NAME, index);
    }

    public static String getListStatement() {
        return String.format("select * from %s", TABLE_NAME);
    }

    public static String getCountStatement() {
        return "select count(*) as num from " + TABLE_NAME;
    }

    public static String getClearStatement() {
        return "delete from " + TABLE_NAME;
    }
}
