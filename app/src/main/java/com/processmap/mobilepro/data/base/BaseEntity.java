package com.processmap.mobilepro.data.base;


import android.content.ContentValues;
import android.database.Cursor;

import com.processmap.mobilepro.util.KeyValueObject;

import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


public class BaseEntity extends Object {

    public static final String COLUMN_SORT_ORDER_ASC                = "ASC";
    public static final String COLUMN_SORT_ORDER_DESC               = "DESC";

    protected static final String COLUMN_TYPE_ENTITY_ID             = "TEXT NOT NULL UNIQUE";
    protected static final String COLUMN_TYPE_TEXT                  = "TEXT";
    protected static final String COLUMN_TYPE_TEXT_NOT_NULL         = "TEXT NOT NULL";
    protected static final String COLUMN_TYPE_TEXT_UNIQUE           = "TEXT UNIQUE";
    protected static final String COLUMN_TYPE_INTEGER               = "INTEGER";
    protected static final String COLUMN_TYPE_INTEGER_NOT_NULL      = "INTEGER NOT NULL";
    protected static final String COLUMN_TYPE_INTEGER_UNIQUE        = "INTEGER UNIQUE";
    protected static final String COLUMN_TYPE_DATE                  = "INTEGER";
    protected static final String COLUMN_TYPE_BOOLEAN               = "INTEGER";
    protected static final String COLUMN_TYPE_PRIMARY_KEY			= "INTEGER UNIQUE";

    // object id in internal database
    // if null then object not stored in local database
    public String EntityId = null;
    public Boolean Saved = false;

    //column names
    public static final String COLUMN_ENTITY_ID                     = "_id";

    public BaseEntity() {
        init();
    }

    public BaseEntity(JSONObject json) {
        init();
    }

    public BaseEntity(Cursor cursor) {
        EntityId = dbToString(cursor, COLUMN_ENTITY_ID);
        Saved = true;
    }

    private void init() {
        EntityId = UUID.randomUUID().toString();
        Saved = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public String getTableName() {
        return "";
    }

    protected static void getColumnsWithTypeArray(ContentValues pairs) {
        pairs.put(COLUMN_ENTITY_ID, COLUMN_TYPE_ENTITY_ID);
    }

    protected static String composeCreateStatement(String tableName, ContentValues pairs) {
        String columns = "";

        for (String key : pairs.keySet()) {
            String value = pairs.getAsString(key);

            columns  = columns.concat(columns.length() == 0 ? key + " " + value : "," + key + " " + value);
        }

        return String.format("CREATE TABLE IF NOT EXISTS %s(%s)", tableName, columns);
    }

    public void getValues(ContentValues values) {
        values.put(COLUMN_ENTITY_ID, EntityId);
    }

    //
    //
    //


    //
    //
    // reflecting
    public void setValue(String valueName, Object value) {
        try {
            Field field = getClass().getDeclaredField(valueName);
            if(field.getType() == value.getClass()) {
                field.set(this, value);
            } else if(field.getType() == String.class) {
                field.set(this, value.toString());
            } else if((field.getType() == Integer.class) && (value.getClass() == String.class)) {
                field.set(this, Integer.valueOf((String) value));
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------------------
    // json parse utility functions
    //
    protected String jsonToString(JSONObject json, String name) {
        try {
            if(!json.isNull(name)) { return json.getString(name); }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Boolean jsonToBoolean(JSONObject json, String name) {
        try {
            return json.getBoolean(name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Integer jsonToInt(JSONObject json, String name) {
        try {
            return json.getInt(name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    //parse string /Date(1433143906213)/ to Date
    protected Date jsonToDate(JSONObject json, String name) {
        try {
            String value = json.getString(name);
            if((value != null) && (!value.toLowerCase().equals("null")) && (value.length() == 21 )) {
                String numbers = value.substring(6, value.length() - 2);
                long l = Long.parseLong(numbers);
                return new Date(l);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String dateToJSON(Date value) {
        if (value == null) {return null;}
        //return "/Date(" + Long.toString(value.getTime()) + ")/";
        return new SimpleDateFormat("MMM dd, yyyy").format(value);
    }

    //----------------------------------------------------------------------------------------------
    // database cursor parse utility functions
    //

    //
    //string
    //
    protected String dbToString(Cursor cursor, String column) {
        try {
            if (!cursor.isNull(cursor.getColumnIndex(column))) return cursor.getString(cursor.getColumnIndex(column)).replace("''", "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String stringToDB(String value) {
        if (value == null) {return "null";}
        return "'" + value.replace("'", "''") + "'";
    }

    //
    // integer
    //
    protected Integer dbToInt(Cursor cursor, String column) {
        try {
            if (!cursor.isNull(cursor.getColumnIndex(column))) return cursor.getInt(cursor.getColumnIndex(column));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String intToDB(Integer value) {
        if (value == null) {return "null";}
        return value.toString();
    }

    //
    // date
    //
    protected Date dbToDate(Cursor cursor, String column) {
        try {
            if (!cursor.isNull(cursor.getColumnIndex(column))) return new Date(cursor.getLong(cursor.getColumnIndex(column)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String dateToDB(Date value) {
        if (value == null) {return "null";}
        return Long.toString(value.getTime());
    }

    //
    // boolean
    //
    protected Boolean dbToBoolean(Cursor cursor, String column) {
        try {
            if (!cursor.isNull(cursor.getColumnIndex(column))) return (cursor.getInt(cursor.getColumnIndex(column)) == 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected String booleanToDB(Boolean value) {
        return value == Boolean.TRUE ? "1" : "0";
    }

}
