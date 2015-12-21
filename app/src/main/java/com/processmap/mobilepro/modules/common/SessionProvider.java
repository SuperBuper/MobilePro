package com.processmap.mobilepro.modules.common;


import android.content.Context;

import com.processmap.mobilepro.data.common.LanguageEntity;
import com.processmap.mobilepro.data.common.LocationEntity;
import com.processmap.mobilepro.data.common.UserEntity;

public class SessionProvider {

    private boolean initDone = false;
    private Context mContext = null;

    private Object lockSession = new Object();

    private static SessionProvider ourInstance = new SessionProvider();
    public static SessionProvider getInstance() {
        return ourInstance;
    }

    private SessionProvider() {
        if (!initDone) {
            mContext = AppContext.getInstance().getContext();

            initDone = true;
        }
    }

    public void close() {
        initDone = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public void startTenantRegistration() {
        synchronized(lockSession) {
            resetTenant();
        }
    }

    public void tenantRegistrationEnded() {
        synchronized(lockSession) {
            ;
        }
    }

    public void startUserRegistration() {
        synchronized(lockSession) {
            LanguageProvider.getInstance().sync();
        }
    }

 public Boolean startOnlineSession(UserEntity user, LocationEntity location, LanguageEntity language) {
        synchronized(lockSession) {
            if (user != null && location != null) {

                String lastLanguage = ConfigurationProvider.getInstance().get(ConfigurationProvider.CONFIGURATION_LAST_LANGUAGE);
                UserEntity lastUser = SecurityProvider.getInstance().get();

                if (lastUser != null && lastUser.Id == user.Id && language != null && language.Code.equals(lastLanguage)) {

                    user.EntityId = lastUser.EntityId;
                    user.Location = lastUser.Location;

                    SecurityProvider.getInstance().set(user);
                    resetModuleListData();
                } else {
                    resetUser();

                    SecurityProvider.getInstance().set(user);
                    setCurrentLocation(location);

                    if (language != null) {
                        LanguageProvider.getInstance().setAsCurrent(language);
                    }
                }
                startOnlineSync();

                return true;
            }
            return false;
        }
    }

    public Boolean startOfflineSession(UserEntity user, LocationEntity location) {
        synchronized(lockSession) {
            SecurityProvider.getInstance().set(user);

            return true;
        }
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    private void setCurrentLocation(LocationEntity location) {
        if (LocationProvider.getInstance().setAsCurrent(location)) {
            UserEntity user = SecurityProvider.getInstance().get();
            user.Location = location.Id;
            SecurityProvider.getInstance().set(user);
        }
    }

    public void changeLocation(LocationEntity location) {
        resetLocation();
        setCurrentLocation(location);
        startOnlineSyncForLocation();
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    private void startOnlineSync() {
        synchronized(lockSession) {
            //[[OutBoxManager sharedInstance] upload];
            LabelProvider.getInstance().sync();
            startOnlineSyncForLocation();
        }
    }

    private void startOnlineSyncForLocation() {
        synchronized(lockSession) {
            ModuleProvider.getInstance().sync();
            LocationProvider.getInstance().sync();
        }
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    private void resetTenant() {
        synchronized(lockSession) {
            TenantProvider.getInstance().clear();
            LanguageProvider.getInstance().clear();
            LabelProvider.getInstance().clear();
            ConfigurationProvider.getInstance().clear();
            resetUser();
        }
    }

    private void resetUser() {
        synchronized(lockSession) {
            SecurityProvider.getInstance().clear();
            LocationProvider.getInstance().clear();
            resetLocation();
        }
    }

    private void resetLocation() {
        synchronized(lockSession) {
            resetModulesData();
            resetModuleListData();
        }
    }

    private void resetModuleListData() {
        ModuleProvider.getInstance().clear();
    }

    private void resetModulesData() {
//        [[ModuleBBSManager sharedInstance] clear];
    }
}
