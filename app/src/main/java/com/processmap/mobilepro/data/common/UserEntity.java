package com.processmap.mobilepro.data.common;

import android.content.ContentValues;
import android.database.Cursor;

import com.processmap.mobilepro.data.base.BaseEntity;

import org.json.JSONObject;

public class UserEntity extends BaseEntity {

    public Integer Id;
    public String Username;
    public String Password;
    public String PasswordHash;
    public String FirstName;
    public String LastName;
    public String FullName;
    public Integer Location;

    // Database table
    private static final String TABLE_NAME              = "user";
    public static final String COLUMN_USER_ID           = "UserId";
    public static final String COLUMN_USERNAME          = "Username";
    public static final String COLUMN_PASSWORD          = "Password";
    public static final String COLUMN_PASSWORD_HASH     = "PasswordHash";
    public static final String COLUMN_FIRSTNAME         = "FirstName";
    public static final String COLUMN_LASTNAME          = "LastName";
    public static final String COLUMN_FULLNAME          = "FullName";
    public static final String COLUMN_LOCATION          = "Location";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    protected static void getColumnsWithTypeArray(ContentValues pairs) {
        pairs.put(COLUMN_USER_ID, COLUMN_TYPE_PRIMARY_KEY);
        pairs.put(COLUMN_USERNAME, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_PASSWORD_HASH, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_FIRSTNAME, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_LASTNAME, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_FULLNAME, COLUMN_TYPE_TEXT);
        pairs.put(COLUMN_LOCATION, COLUMN_TYPE_INTEGER);

        BaseEntity.getColumnsWithTypeArray(pairs);
    }

    @Override
    public void getValues(ContentValues pairs) {
        pairs.put(COLUMN_USER_ID, Id);
        pairs.put(COLUMN_USERNAME, Username);
        pairs.put(COLUMN_PASSWORD_HASH, PasswordHash);
        pairs.put(COLUMN_FIRSTNAME, FirstName);
        pairs.put(COLUMN_LASTNAME, LastName);
        pairs.put(COLUMN_FULLNAME, FullName);
        pairs.put(COLUMN_LOCATION, Location);
        super.getValues(pairs);
    }

    //----------------------------------------------------------------------------------------------
    public UserEntity(JSONObject json) {
        super(json);
        Id = jsonToInt(json, COLUMN_USER_ID);
        Username = jsonToString(json, COLUMN_USERNAME);
        FirstName = jsonToString(json, COLUMN_FIRSTNAME);
        LastName = jsonToString(json, COLUMN_LASTNAME);
        FullName = jsonToString(json, COLUMN_FULLNAME);
    }

    public UserEntity(Cursor cursor) {
        super(cursor);

        Id = dbToInt(cursor, COLUMN_USER_ID);
        Username = dbToString(cursor, COLUMN_USERNAME);
        PasswordHash = dbToString(cursor, COLUMN_PASSWORD_HASH);
        FirstName = dbToString(cursor, COLUMN_FIRSTNAME);
        LastName = dbToString(cursor, COLUMN_LASTNAME);
        FullName = dbToString(cursor, COLUMN_FULLNAME);
        Location = dbToInt(cursor, COLUMN_LOCATION);
    }

    //----------------------------------------------------------------------------------------------
    //database functions
    //
    public static String getCreateStatement() {
        ContentValues pairs = new ContentValues();
        getColumnsWithTypeArray(pairs);
        return composeCreateStatement(TABLE_NAME, pairs);
    }

    public static String getListStatement() {
        return "select * from " + TABLE_NAME;
    }

    public static String selectByNameAndPassword(String name, String password) {
        return String.format("select * from %s where %s='%s' and %s='%s'", TABLE_NAME, COLUMN_USERNAME, name, COLUMN_PASSWORD_HASH, password);
    }

    public static String getClearStatement() {
        return "delete from " + TABLE_NAME;
    }
}
