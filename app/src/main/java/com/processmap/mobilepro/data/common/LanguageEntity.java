package com.processmap.mobilepro.data.common;

import android.content.ContentValues;
import android.database.Cursor;

import com.processmap.mobilepro.data.base.BaseEntity;

import org.json.JSONObject;

public class LanguageEntity extends BaseEntity {

    public Integer Id;
    public String Code;
    public String Description;
    public String NativeDesc;
    public String CultureName;

    // Database table
    private static final String TABLE_NAME              = "language";
    public static final String COLUMN_ID                = "Id";
    public static final String COLUMN_CODE              = "Code";
    public static final String COLUMN_DESCRIPTION       = "Description";
    public static final String COLUMN_NATIVE_DESC       = "NativeDesc";
    public static final String COLUMN_CULTURE_NAME      = "CultureName";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    protected static void getColumnsWithTypeArray(ContentValues pairs) {
        pairs.put(COLUMN_ID, COLUMN_TYPE_INTEGER);
        pairs.put(COLUMN_CODE, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_DESCRIPTION, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_NATIVE_DESC, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_CULTURE_NAME, COLUMN_TYPE_TEXT);

        BaseEntity.getColumnsWithTypeArray(pairs);
    }

    @Override
    public void getValues(ContentValues pairs) {
        pairs.put(COLUMN_ID, Id);
        pairs.put(COLUMN_CODE, Code);
        pairs.put(COLUMN_DESCRIPTION, Description);
        pairs.put(COLUMN_NATIVE_DESC, NativeDesc);
        pairs.put(COLUMN_CULTURE_NAME, CultureName);
        super.getValues(pairs);
    }

    //----------------------------------------------------------------------------------------------
    public LanguageEntity(JSONObject json) {
        super(json);
        Id = jsonToInt(json, COLUMN_ID);
        Code = jsonToString(json, COLUMN_CODE);
        Description = jsonToString(json, COLUMN_DESCRIPTION);
        NativeDesc = jsonToString(json, COLUMN_NATIVE_DESC);
        CultureName = jsonToString(json, COLUMN_CULTURE_NAME);
    }

    public LanguageEntity(Cursor cursor) {
        super(cursor);

        Id = dbToInt(cursor, COLUMN_ID);
        Code = dbToString(cursor, COLUMN_CODE);
        Description = dbToString(cursor, COLUMN_DESCRIPTION);
        NativeDesc = dbToString(cursor, COLUMN_NATIVE_DESC);
        CultureName = dbToString(cursor, COLUMN_CULTURE_NAME);
    }

    //----------------------------------------------------------------------------------------------
    //database functions
    //
    public static String getCreateStatement() {
        ContentValues pairs = new ContentValues();
        getColumnsWithTypeArray(pairs);
        return composeCreateStatement(TABLE_NAME, pairs);
    }

    public static String selectByIndex(Integer index) {
        return String.format("select * from %s limit 1 offset %d", TABLE_NAME, index);
    }

    public static String selectByCode(String code) {
        return String.format("select * from %s where %s='%s'", TABLE_NAME, COLUMN_CODE, code);
    }

    public static String getListStatement() {
        return String.format("select * from %s order by %s asc",  TABLE_NAME, COLUMN_DESCRIPTION);
    }

    public static String getCountStatement() {
        return "select count(*) as num from " + TABLE_NAME;
    }

    public static String getClearStatement() {
        return "delete from " + TABLE_NAME;
    }
}
