package com.processmap.mobilepro.modules.common;


import android.content.Context;
import android.database.Cursor;

import com.processmap.mobilepro.data.base.BaseEntity;
import com.processmap.mobilepro.data.common.LabelEntity;
import com.processmap.mobilepro.data.common.TenantEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;


public class TenantProvider {

    private boolean initDone = false;
    private Context mContext = null;

    private static TenantProvider ourInstance = new TenantProvider();
    public static TenantProvider getInstance() {
        return ourInstance;
    }

    private TenantProvider() {
        if (!initDone) {
            mContext = AppContext.getInstance().getContext();

            DatabaseManager.getInstance().exec(TenantEntity.getCreateStatement());

            initDone = true;
        }
    }

    public void close() {
        initDone = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public TenantEntity get() {
        TenantEntity object = null;
        Cursor cursor = DatabaseManager.getInstance().query(TenantEntity.getListStatement());
        if (cursor.moveToFirst()) {
            object = new TenantEntity(cursor);
        }
        cursor.close();
        return object;
    }

    public void set(TenantEntity object) {
        if (object != null) {
            DatabaseManager.getInstance().insert(object);
        }
    }

    public void clear() {
        DatabaseManager.getInstance().exec(TenantEntity.getClearStatement());
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public void validateTenant(final String pin, final TenantProviderCallback callback) {
        ConnectivityProvider connectivityProvider = ConnectivityProvider.getInstance();
        if (connectivityProvider.isOnline()) {
            connectivityProvider.getRequest(String.format(ConfigurationProvider.CONFIGURATION_DEFAULT_HOST + ConfigurationProvider.CONFIGURATION_SERVICE_TENANT_VALIDATION, pin),
                    new ConnectivityProvider.ConnectivityCallback() {
                        TenantEntity tenantEntity = null;
                        ArrayList<BaseEntity> list = new ArrayList<BaseEntity>();

                        @Override
                        public void backgroundTask(int responseCode, String responseData, String errorMessage) {
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                try {
                                    JSONObject json = new JSONObject(responseData);
                                    tenantEntity = new TenantEntity(json);
                                    list.add(tenantEntity);

                                    JSONArray moduleDefaults = json.getJSONArray("ModuleDefaults");
                                    for (int i = 0; i < moduleDefaults.length(); i++) {
                                        JSONObject module = moduleDefaults.getJSONObject(i);
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
                                DatabaseManager.getInstance().insertInTransaction(list);

                                if (callback != null) {
                                    callback.callback(ConfigurationProvider.RESPONSE_OK, tenantEntity);
                                }
                            } else {
                                if (callback != null) {
                                    callback.callback(ConfigurationProvider.RESPONSE_ERROR, null);
                                }
                            }
                        }
                    });
        } else {
            if (callback != null) {
                callback.callback(ConfigurationProvider.RESPONSE_OFFLINE, null);
            }
        }
    }

    //
    //----------------------------------------------------------------------------------------------
    //



    //
    //----------------------------------------------------------------------------------------------
    //

    public interface TenantProviderCallback {
        void callback(int responseCode, TenantEntity tenant);
    }
}
