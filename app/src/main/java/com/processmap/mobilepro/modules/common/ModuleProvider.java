package com.processmap.mobilepro.modules.common;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.processmap.mobilepro.data.base.BaseEntity;
import com.processmap.mobilepro.data.common.LanguageEntity;
import com.processmap.mobilepro.data.common.ModuleEntity;

import org.json.JSONArray;

import java.net.HttpURLConnection;
import java.util.ArrayList;


public class ModuleProvider {
    private boolean initDone = false;
    private Context mContext = null;

    private Boolean syncing = false;

    private static ModuleProvider ourInstance = new ModuleProvider();
    public static ModuleProvider getInstance() {
        return ourInstance;
    }

    private ModuleProvider() {
        if (!initDone) {
            mContext = AppContext.getInstance().getContext();

            DatabaseManager.getInstance().exec(ModuleEntity.getCreateStatement());

            initDone = true;
        }
    }

    public void close() {
        initDone = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public ModuleEntity getById(Integer Id) {
        ModuleEntity object = null;
        Cursor cursor = DatabaseManager.getInstance().query(ModuleEntity.selectById(Id));
        if(cursor.moveToFirst()) {
            object = new ModuleEntity(cursor);
        }
        cursor.close();
        return object;
    }

    public ModuleEntity getByIndex(Integer index) {
        ModuleEntity object = null;
        Cursor cursor = DatabaseManager.getInstance().query(ModuleEntity.selectByIndex(index));
        if(cursor.moveToFirst()) {
            object = new ModuleEntity(cursor);
        }
        cursor.close();
        return object;
    }

    public void clear() {
        DatabaseManager.getInstance().exec(ModuleEntity.getClearStatement());
    }

    public Integer count() {
        Integer i = 0;

        Cursor cursor = DatabaseManager.getInstance().query(ModuleEntity.getCountStatement());
        if (cursor.moveToFirst()) {
            i = cursor.getInt(0);
        }
        cursor.close();

        return i;
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public void sync() {
        ConnectivityProvider connectivityProvider = ConnectivityProvider.getInstance();
        if (!syncing && connectivityProvider.isOnline()) {
            syncing = true;

            connectivityProvider.getRequest(String.format("%s%s", ConfigurationProvider.CONFIGURATION_DEFAULT_HOST, ConfigurationProvider.CONFIGURATION_SERVICE_MODULE_MENU),
                    new ConnectivityProvider.ConnectivityCallback() {
                        ArrayList<BaseEntity> list = new ArrayList<BaseEntity>();
                        
                        @Override
                        public void backgroundTask(int responseCode, String responseData, String errorMessage) {
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                try {
                                    JSONArray modules = new JSONArray(responseData);
                                    for (int i = 0; i < modules.length(); i++) {
                                        ModuleEntity module = new ModuleEntity(modules.getJSONObject(i));
                                        list.add(module);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void postExecuteTask(int responseCode, String responseData, String errorMessage) {
                            if (responseCode == HttpURLConnection.HTTP_OK && list.size() > 0) {
                                DatabaseManager.getInstance().insertInTransaction(list);

                                Intent notification = new Intent(ConfigurationProvider.NOTIFICATION_MODULE_UPDATED);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(notification);

                                Toast.makeText(mContext, "ModuleProvider.sync() ended", Toast.LENGTH_LONG).show();
                            }
                            syncing = false;
                        }
                    });
        }
    }
}
