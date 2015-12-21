package com.processmap.mobilepro.modules.common;

import android.content.Context;
import android.database.Cursor;

import com.processmap.mobilepro.data.common.LabelEntity;
import com.processmap.mobilepro.data.common.LocationEntity;
import com.processmap.mobilepro.data.common.UserEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.SignatureException;


public class SecurityProvider {

    private boolean initDone = false;
    private Context mContext = null;

    private UserEntity currentUser = null;

    private static SecurityProvider ourInstance = new SecurityProvider();
    public static SecurityProvider getInstance() {
        return ourInstance;
    }

    private SecurityProvider() {
        if (!initDone) {
            mContext = AppContext.getInstance().getContext();

            DatabaseManager.getInstance().exec(UserEntity.getCreateStatement());

            initDone = true;
        }
    }

    public void close() {
        initDone = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public UserEntity get() {
        if (currentUser == null) {
            Cursor cursor = DatabaseManager.getInstance().query(UserEntity.getListStatement());
            if (cursor.moveToFirst()) {
                currentUser = new UserEntity(cursor);
            }
            cursor.close();
        }
        return currentUser;
    }

    public void set(UserEntity user) {
        if (user != null) {
            currentUser = user;
            DatabaseManager.getInstance().insert(user);
        }
    }

    public void clear() {
        DatabaseManager.getInstance().exec(UserEntity.getClearStatement());
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public void validateUserOnline(final String username, final String password, final SecurityProviderCallback callback) {
        if (username != null && password != null) {
            ConnectivityProvider connectivityProvider = ConnectivityProvider.getInstance();
            if (connectivityProvider.isOnline()) {
                JSONObject param = new JSONObject();
                try {
                    param.put("Username", username);
                    param.put("Password", password);

                    connectivityProvider.postRequest(ConfigurationProvider.CONFIGURATION_DEFAULT_HOST + ConfigurationProvider.CONFIGURATION_SERVICE_USER_AUTH, param.toString(),
                            new ConnectivityProvider.ConnectivityCallback() {
                                UserEntity user = null;
                                LocationEntity location = null;

                                @Override
                                public void backgroundTask(int responseCode, String responseData, String errorMessage) {
                                    if (responseCode == HttpURLConnection.HTTP_OK) {
                                        try {
                                            JSONObject json = new JSONObject(responseData);
                                            JSONObject userDetails = json.getJSONObject("UserDetails");

                                            if (!userDetails.isNull("DefaultLocation")) {
                                                JSONObject defaultLocation = userDetails.getJSONObject("DefaultLocation");
                                                location = new LocationEntity(defaultLocation);
                                            }

                                            user = new UserEntity(userDetails);
                                            user.PasswordHash = passwordHashFromString(password);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void postExecuteTask(int responseCode, String responseData, String errorMessage) {
                                    if (responseCode == HttpURLConnection.HTTP_OK && user != null && location != null) {
                                        if (callback != null) {
                                            callback.callback(ConfigurationProvider.RESPONSE_OK, user, location);
                                        }
                                    } else {
                                        if (callback != null) {
                                            callback.callback(ConfigurationProvider.RESPONSE_ERROR, null, null);
                                        }
                                    }
                                }
                            });
                } catch (Exception e) {
                    if (callback != null) {
                        callback.callback(ConfigurationProvider.RESPONSE_ERROR, null, null);
                    }
                }
            } else {
                if (callback != null) {
                    callback.callback(ConfigurationProvider.RESPONSE_OFFLINE, null, null);
                }
            }
        } else {
            if (callback != null) {
                callback.callback(ConfigurationProvider.RESPONSE_ERROR, null, null);
            }
        }
    }

    public void validateUserOffline(final String username, final String password, final SecurityProviderCallback callback) {
        if (username != null && password != null) {
            UserEntity user = null;
            LocationEntity location = null;

            Cursor cursor = DatabaseManager.getInstance().query(UserEntity.selectByNameAndPassword(username, passwordHashFromString(password)));
            if (cursor.moveToFirst()) {
                user = new UserEntity(cursor);
            }
            cursor.close();

            if (user != null) {
                location = LocationProvider.getInstance().getById(user.Location);
                if (callback != null) {
                    callback.callback(ConfigurationProvider.RESPONSE_OK, user, location);
                }
            } else {
                if (callback != null) {
                    callback.callback(ConfigurationProvider.RESPONSE_ERROR, null, null);
                }
            }
        } else {
            if (callback != null) {
                callback.callback(ConfigurationProvider.RESPONSE_ERROR, null, null);
            }
        }
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public interface SecurityProviderCallback {
        void callback(int responseCode, UserEntity user, LocationEntity location);
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public String passwordHashFromString(String password) {
        return getDigest("SHA-256", password);
    }

    private String getDigest(String algorithm, String data) {
        try {
            MessageDigest mac = MessageDigest.getInstance(algorithm);
            mac.update(data.getBytes("UTF-8"));
            return toHex(mac.digest()).toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }

    private String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}
