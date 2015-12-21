package com.processmap.mobilepro.data.common;


import android.content.ContentValues;
import android.database.Cursor;

import com.processmap.mobilepro.data.base.BaseEntity;

import org.json.JSONObject;

public class LabelEntity extends BaseEntity {

    public Integer Id;
    public Integer Module;
    public String Description;

    // Database table
    private static final String TABLE_NAME              = "label";
    public static final String COLUMN_ID                = "Id";
    public static final String COLUMN_MODULE            = "Module";
    public static final String COLUMN_DESCRIPTION       = "Description";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    protected static void getColumnsWithTypeArray(ContentValues pairs) {
        pairs.put(COLUMN_ID, COLUMN_TYPE_INTEGER);
        pairs.put(COLUMN_MODULE, COLUMN_TYPE_INTEGER);
        pairs.put(COLUMN_DESCRIPTION, COLUMN_TYPE_TEXT);

        BaseEntity.getColumnsWithTypeArray(pairs);
    }

    @Override
    public void getValues(ContentValues pairs) {
        pairs.put(COLUMN_ID, Id);
        pairs.put(COLUMN_MODULE, Module);
        pairs.put(COLUMN_DESCRIPTION, Description);
        super.getValues(pairs);
    }

    //----------------------------------------------------------------------------------------------
    public LabelEntity(JSONObject json) {
        super(json);
        Id = jsonToInt(json, COLUMN_ID);
        Module = jsonToInt(json, COLUMN_MODULE);
        Description = jsonToString(json, COLUMN_DESCRIPTION);
    }

    public LabelEntity(Cursor cursor) {
        super(cursor);

        Id = dbToInt(cursor, COLUMN_ID);
        Module = dbToInt(cursor, COLUMN_MODULE);
        Description = dbToString(cursor, COLUMN_DESCRIPTION);
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

    public static String selectByIdAndModule(Integer Id, Integer Module) {
        return String.format("select * from %s where %s=%d and %s=%d", TABLE_NAME, COLUMN_ID, Id, COLUMN_MODULE, Module);
    }

    public static String getListStatement() {
        return "select * from " + TABLE_NAME;
    }

    public static String getClearStatement() {
        return "delete from " + TABLE_NAME;
    }
}
