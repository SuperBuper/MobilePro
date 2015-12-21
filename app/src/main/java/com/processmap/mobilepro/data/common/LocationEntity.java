package com.processmap.mobilepro.data.common;


import android.content.ContentValues;
import android.database.Cursor;

import com.processmap.mobilepro.data.base.BaseEntity;

import org.json.JSONObject;

public class LocationEntity extends BaseEntity {

    public Integer Id;
    public String Name;
    public String Code;

    // Database table
    private static final String TABLE_NAME              = "location";
    public static final String COLUMN_ID                = "Id";
    public static final String COLUMN_NAME              = "Name";
    public static final String COLUMN_CODE              = "Code";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    protected static void getColumnsWithTypeArray(ContentValues pairs) {
        pairs.put(COLUMN_ID, COLUMN_TYPE_PRIMARY_KEY);
        pairs.put(COLUMN_NAME, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_CODE, COLUMN_TYPE_TEXT);

        BaseEntity.getColumnsWithTypeArray(pairs);
    }

    @Override
    public void getValues(ContentValues pairs) {
        pairs.put(COLUMN_ID, Id);
        pairs.put(COLUMN_NAME, Name);
        pairs.put(COLUMN_CODE, Code);
        super.getValues(pairs);
    }

    //----------------------------------------------------------------------------------------------
    public LocationEntity(JSONObject json) {
        super(json);
        Id = jsonToInt(json, COLUMN_ID);
        Name = jsonToString(json, COLUMN_NAME);
        Code = jsonToString(json, COLUMN_CODE);
    }

    public LocationEntity(Cursor cursor) {
        super(cursor);

        Id = dbToInt(cursor, COLUMN_ID);
        Name = dbToString(cursor, COLUMN_NAME);
        Code = dbToString(cursor, COLUMN_CODE);
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

    public static String getListStatement() {
        return "select * from " + TABLE_NAME;
    }

    public static String getClearStatement() {
        return "delete from " + TABLE_NAME;
    }
}
