package com.processmap.mobilepro.modules.common;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.processmap.mobilepro.data.base.BaseEntity;
import com.processmap.mobilepro.data.common.LanguageEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Locale;

public class LanguageProvider {

    private boolean initDone = false;
    private Context mContext = null;

    private LanguageEntity currentLanguage = null;
    private Boolean syncing = false;

    private static LanguageProvider ourInstance = new LanguageProvider();
    public static LanguageProvider getInstance() {
        return ourInstance;
    }

    private LanguageProvider() {
        if (!initDone) {
            mContext = AppContext.getInstance().getContext();

            DatabaseManager.getInstance().exec(LanguageEntity.getCreateStatement());

            initDone = true;
        }
    }

    public void close() {
        initDone = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public LanguageEntity get() {
        if (currentLanguage == null) {
            currentLanguage = getByCode(ConfigurationProvider.CONFIGURATION_APPLICATION_LANGUAGE);
        }
        return currentLanguage;
    }

    public String getLanguageCode() {
        if (currentLanguage == null) {
            String lastLanguageCode = ConfigurationProvider.getInstance().get(ConfigurationProvider.CONFIGURATION_LAST_LANGUAGE);
            return lastLanguageCode != null ? lastLanguageCode : Locale.getDefault().getLanguage();
        } else {
            return currentLanguage.Code;
        }
    }

    public LanguageEntity getByIndex(Integer index) {
        LanguageEntity object = null;
        Cursor cursor = DatabaseManager.getInstance().query(LanguageEntity.selectByIndex(index));
        if (cursor.moveToFirst()) {
            object = new LanguageEntity(cursor);
        }
        cursor.close();
        return object;
    }

    public LanguageEntity getByCode(String code) {
        LanguageEntity object = null;
        Cursor cursor = DatabaseManager.getInstance().query(LanguageEntity.selectByCode(code));
        if (cursor.moveToFirst()) {
            object = new LanguageEntity(cursor);
        }
        cursor.close();
        return object;
    }

    public Boolean set(LanguageEntity object) {
        if (object != null) {
            return DatabaseManager.getInstance().insert(object) != DatabaseManager.SQL_ERROR;
        }
        return false;
    }

    public void setAsCurrent(LanguageEntity object) {
        if (object != null) {
            if (set(object)) {
                ConfigurationProvider.getInstance().set(ConfigurationProvider.CONFIGURATION_LAST_LANGUAGE, object.Code);
                currentLanguage = object;
                Intent notification = new Intent(ConfigurationProvider.NOTIFICATION_LANGUAGE_CHANGED);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(notification);
            }
        }
    }

    public void clear() {
        DatabaseManager.getInstance().exec(LanguageEntity.getClearStatement());
    }

    public Integer count() {
        Integer i = 0;

        Cursor cursor = DatabaseManager.getInstance().query(LanguageEntity.getCountStatement());
        if (cursor.moveToFirst()) {
            i = cursor.getInt(0);
        }
        cursor.close();

        return i;
    }

    public Cursor getList() {
        return DatabaseManager.getInstance().query(LanguageEntity.getListStatement());
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public void sync() {
        ConnectivityProvider connectivityProvider = ConnectivityProvider.getInstance();
        if (!syncing && connectivityProvider.isOnline()) {
            syncing = true;

            connectivityProvider.getRequest(String.format("%s%s", ConfigurationProvider.CONFIGURATION_DEFAULT_HOST, ConfigurationProvider.CONFIGURATION_SERVICE_LANGUAGES),
                    new ConnectivityProvider.ConnectivityCallback() {
                        ArrayList<BaseEntity> list = new ArrayList<BaseEntity>();

                        @Override
                        public void backgroundTask(int responseCode, String responseData, String errorMessage) {
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                try {
                                    JSONArray languages = new JSONArray(responseData);
                                    for (int i = 0; i < languages.length(); i++) {
                                        LanguageEntity language = new LanguageEntity(languages.getJSONObject(i));
                                        list.add(language);
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

                                Intent notification = new Intent(ConfigurationProvider.NOTIFICATION_LANGUAGE_UPDATED);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(notification);

                                Toast.makeText(mContext, "LanguageProvider.sync() ended", Toast.LENGTH_LONG).show();
                            }
                            syncing = false;
                        }
                    });

        }
    }
}
