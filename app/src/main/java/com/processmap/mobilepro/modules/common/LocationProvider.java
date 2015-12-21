package com.processmap.mobilepro.modules.common;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.processmap.mobilepro.data.base.BaseEntity;
import com.processmap.mobilepro.data.common.LocationEntity;
import com.processmap.mobilepro.data.common.UserEntity;

import org.json.JSONArray;

import java.net.HttpURLConnection;
import java.util.ArrayList;


public class LocationProvider {

    private boolean initDone = false;
    private Context mContext = null;

    private LocationEntity currentLocation = null;
    private Boolean syncing = false;

    private static LocationProvider ourInstance = new LocationProvider();
    public static LocationProvider getInstance() {
        return ourInstance;
    }

    private LocationProvider() {
        if (!initDone) {
            mContext = AppContext.getInstance().getContext();

            DatabaseManager.getInstance().exec(LocationEntity.getCreateStatement());

            initDone = true;
        }
    }

    public void close() {
        initDone = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public LocationEntity get() {
        if (currentLocation == null) {
            UserEntity user = SecurityProvider.getInstance().get();
            if (user != null) {
                currentLocation = getById(user.Location);
            }
        }
        return currentLocation;
    }

    public LocationEntity getById(Integer Id) {
        LocationEntity location = null;

        Cursor cursor = DatabaseManager.getInstance().query(LocationEntity.selectById(Id));
        if (cursor.moveToFirst()) {
            location = new LocationEntity(cursor);
        }
        cursor.close();
        return location;
    }

//    public LocationEntity getByIndex:(long)index withFilter:(NSString *)filter {
//        LocationEntity *object = nil;
//
//        DBResultSet *cursor = [[DatabaseManager sharedInstance] query:[LocationEntity selectStatementByIndex:index withFilter:[self createFilterByString:filter]]];
//        if([cursor next]) {
//            object = [LocationEntity createFromDataSet:cursor];
//        }
//        [cursor close];
//        return object;
//    }

    public Boolean set(LocationEntity object) {
        if (object != null) {
            return DatabaseManager.getInstance().insert(object) != DatabaseManager.SQL_ERROR;
        }
        return false;
    }

    public Boolean setAsCurrent(LocationEntity location) {
        if (location != null) {
            currentLocation = location;
            Intent notification = new Intent(ConfigurationProvider.NOTIFICATION_LOCATION_CHANGED);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(notification);
            return set(location);
        }
        return false;
    }

    public void clear() {
        DatabaseManager.getInstance().exec(LocationEntity.getClearStatement());
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public void sync() {
        ConnectivityProvider connectivityProvider = ConnectivityProvider.getInstance();
        if (!syncing && connectivityProvider.isOnline()) {
            syncing = true;

            connectivityProvider.getRequest(String.format("%s%s", ConfigurationProvider.CONFIGURATION_DEFAULT_HOST, String.format(ConfigurationProvider.CONFIGURATION_SERVICE_LOCATIONS, SecurityProvider.getInstance().get().Id)),
                    new ConnectivityProvider.ConnectivityCallback() {
                        ArrayList<BaseEntity> list = new ArrayList<BaseEntity>();

                        @Override
                        public void backgroundTask(int responseCode, String responseData, String errorMessage) {
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                try {
                                    JSONArray locations = new JSONArray(responseData);
                                    for (int i = 0; i < locations.length(); i++) {
                                        LocationEntity location = new LocationEntity(locations.getJSONObject(i));
                                        list.add(location);
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

                                Intent notification = new Intent(ConfigurationProvider.NOTIFICATION_LOCATION_UPDATED);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(notification);

                                Toast.makeText(mContext, "LocationProvider.sync() ended", Toast.LENGTH_LONG).show();
                            }
                            syncing = false;
                        }
                    });
        }
    }
}
