package com.processmap.mobilepro.data.common;

import android.content.ContentValues;
import android.database.Cursor;

import com.processmap.mobilepro.data.base.BaseEntity;

public class ConfigurationEntity extends BaseEntity {

    public String Name;
    public String Value;

    // Database table
    private static final String TABLE_NAME              = "configuration";
    public static final String COLUMN_NAME              = "Name";
    public static final String COLUMN_VALUE             = "Value";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    protected static void getColumnsWithTypeArray(ContentValues pairs) {
        pairs.put(COLUMN_NAME, COLUMN_TYPE_TEXT_UNIQUE);
        pairs.put(COLUMN_VALUE, COLUMN_TYPE_TEXT);

        BaseEntity.getColumnsWithTypeArray(pairs);
    }

    @Override
    public void getValues(ContentValues pairs) {
        pairs.put(COLUMN_NAME, Name);
        pairs.put(COLUMN_VALUE, Value);
        super.getValues(pairs);
    }

    //----------------------------------------------------------------------------------------------
    public ConfigurationEntity(String name, String value) {
        this.Name = name;
        this.Value = value;
    }

    public ConfigurationEntity(Cursor cursor) {
        super(cursor);

        Name = dbToString(cursor, COLUMN_NAME);
        Value = dbToString(cursor, COLUMN_VALUE);
    }

    //----------------------------------------------------------------------------------------------
    //database functions
    //
    public static String getCreateStatement() {
        ContentValues pairs = new ContentValues();
        getColumnsWithTypeArray(pairs);
        return composeCreateStatement(TABLE_NAME, pairs);
    }

    public static String getSelectByGUID(String guid) {
        return "select * from " + TABLE_NAME + " where guid='" + guid + "'";
    }

    public static String getSelectByName(String Name) {
        return String.format("select * from %s where %s='%s'", TABLE_NAME, COLUMN_NAME, Name);
    }

    public static String getListStatement() {
        return "select * from " + TABLE_NAME;
    }

    public static String getClearStatement() {
        return "delete from " + TABLE_NAME;
    }
}
