package com.processmap.mobilepro.modules.common;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.processmap.mobilepro.data.base.BaseEntity;
import com.processmap.mobilepro.data.common.LabelEntity;
import com.processmap.mobilepro.data.common.LocationEntity;
import com.processmap.mobilepro.data.common.TenantEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LabelProvider {

    private boolean initDone = false;
    private Context mContext = null;

    private Boolean syncing = false;

    private static LabelProvider ourInstance = new LabelProvider();
    public static LabelProvider getInstance() {
        return ourInstance;
    }

    private LabelProvider() {
        if (!initDone) {
            mContext = AppContext.getInstance().getContext();

            DatabaseManager.getInstance().exec(LabelEntity.getCreateStatement());

            initDone = true;
        }
    }

    public void close() {
        initDone = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public String get(Integer Id, Integer Module) {
        String result = "";

        Cursor cursor = DatabaseManager.getInstance().query(LabelEntity.selectByIdAndModule(Id, Module));
        if(cursor.moveToFirst()) {
            LabelEntity object = new LabelEntity(cursor);
            result = object.Description;
        }
        cursor.close();
        return result;
    }

    public Boolean set(LabelEntity object) {
        if (object != null) {
            return DatabaseManager.getInstance().insert(object) != DatabaseManager.SQL_ERROR;
        }
        return false;
    }

    public void clear() {
        DatabaseManager.getInstance().exec(LabelEntity.getClearStatement());
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    //
    //
    //
//    public String dateToString(Date value) {
//        if(value == null) { return ""; }
//        DateFormat df = DateFormat.getDateInstance();
//        return df.format(value);
//    }

//    public String getString(int id) {
//        return mContext.getString(id);
//    }

    //
    // set text to views
    //
    public void setTextTo(TextView textView, Integer Id, Integer Module) {
        textView.setText(get(Id, Module));
    }

    public void setTextTo(EditText editText, Integer Id, Integer Module) {
        editText.setText(get(Id, Module));
    }

    //
    //
    //
    private String getStringResourceByName(String string) {
        String packageName = mContext.getPackageName();
        int resId = mContext.getResources().getIdentifier(string, "string", packageName);
        return mContext.getString(resId);
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public Boolean sync() {
        ConnectivityProvider connectivityProvider = ConnectivityProvider.getInstance();
        if (!syncing && connectivityProvider.isOnline()) {
            syncing = true;

            connectivityProvider.getRequest(String.format("%s%s", ConfigurationProvider.CONFIGURATION_DEFAULT_HOST, ConfigurationProvider.CONFIGURATION_SERVICE_LABELS),
                    new ConnectivityProvider.ConnectivityCallback() {
                        ArrayList<BaseEntity> list = new ArrayList<BaseEntity>();

                        @Override
                        public void backgroundTask(int responseCode, String responseData, String errorMessage) {
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                try {
                                    JSONArray dictionary = new JSONArray(responseData);
                                    for (int i = 0; i < dictionary.length(); i++) {
                                        JSONObject module = dictionary.getJSONObject(i);
                                        Integer moduleId = module.getInt("Id");
                                        JSONArray labels = module.getJSONArray("Labels");
                                        for (int j = 0; j < labels.length(); j++) {
                                            LabelEntity label = new LabelEntity(labels.getJSONObject(j));
                                            label.Module = moduleId;
                                            list.add(label);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void postExecuteTask(int responseCode, String responseData, String errorMessage) {
                            if (responseCode == HttpURLConnection.HTTP_OK && list.size() > 0) {
                                clear();
                                DatabaseManager.getInstance().insertInTransaction(list);

                                Intent notification = new Intent(ConfigurationProvider.NOTIFICATION_LABEL_UPDATED);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(notification);

                                Toast.makeText(mContext, "LabelProvider.sync() ended", Toast.LENGTH_LONG).show();
                            }
                            syncing = false;
                        }
                    });
            return true;
        } else {
            return false;
        }
    }
}
