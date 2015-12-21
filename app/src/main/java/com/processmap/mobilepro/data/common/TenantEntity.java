package com.processmap.mobilepro.data.common;


import android.content.ContentValues;
import android.database.Cursor;

import com.processmap.mobilepro.data.base.BaseEntity;

import org.json.JSONObject;


public class TenantEntity extends BaseEntity {

    public Integer Id;
    public Boolean SSOEnabled;
    public String ClientName;
    public String ProductName;

    // Database table
    private static final String TABLE_NAME              = "tenant";
    public static final String COLUMN_ID                = "Id";
    public static final String COLUMN_SSO_ENABLED       = "SSOEnabled";
    public static final String COLUMN_CLIENT_NAME       = "ClientName";
    public static final String COLUMN_PRODUCT_NAME      = "ProductName";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    protected static void getColumnsWithTypeArray(ContentValues pairs) {
        pairs.put(COLUMN_ID, COLUMN_TYPE_PRIMARY_KEY);
        pairs.put(COLUMN_SSO_ENABLED, COLUMN_TYPE_BOOLEAN);
        pairs.put(COLUMN_CLIENT_NAME, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_PRODUCT_NAME, COLUMN_TYPE_TEXT);

        BaseEntity.getColumnsWithTypeArray(pairs);
    }

    @Override
    public void getValues(ContentValues pairs) {
        pairs.put(COLUMN_ID, Id);
        pairs.put(COLUMN_SSO_ENABLED, SSOEnabled);
        pairs.put(COLUMN_CLIENT_NAME, ClientName);
        pairs.put(COLUMN_PRODUCT_NAME, ProductName);

        super.getValues(pairs);
    }

    //----------------------------------------------------------------------------------------------
    public TenantEntity(JSONObject json) {
        super(json);
        Id = jsonToInt(json, COLUMN_ID);
        SSOEnabled = jsonToBoolean(json, COLUMN_SSO_ENABLED);
        ClientName = jsonToString(json, COLUMN_CLIENT_NAME);
        ProductName = jsonToString(json, COLUMN_PRODUCT_NAME);
    }

    public TenantEntity(Cursor cursor) {
        super(cursor);

        Id = dbToInt(cursor, COLUMN_ID);
        SSOEnabled = dbToBoolean(cursor, COLUMN_SSO_ENABLED);
        ClientName = dbToString(cursor, COLUMN_CLIENT_NAME);
        ProductName = dbToString(cursor, COLUMN_PRODUCT_NAME);
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

    public static String getListStatement() {
        return "select * from " + TABLE_NAME;
    }

    public static String getClearStatement() {
        return "delete from " + TABLE_NAME;
    }
}
