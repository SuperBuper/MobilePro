package com.processmap.mobilepro.modules.common;

import android.content.Context;
import android.database.Cursor;

import com.processmap.mobilepro.data.common.ConfigurationEntity;
import com.processmap.mobilepro.data.common.TenantEntity;


public class ConfigurationProvider {

    public final static int RESPONSE_HTTP_ERROR                             = -1;
    public final static int RESPONSE_OK                                     = 0;
    public final static int RESPONSE_ERROR                                  = 1;
    public final static int RESPONSE_OFFLINE                                = 2;

    public final static String CONFIGURATION_DEFAULT_HOST                   = "sandbox.pmapconnect.com/svc";
    public final static String CONFIGURATION_SERVICE_TENANT_VALIDATION      = "/papi/tenant/code/%s";
    public final static String CONFIGURATION_SERVICE_USER_AUTH              = "/papi/auth/";
    public final static String CONFIGURATION_SERVICE_MODULE_MENU            = "/papi/modulemenus";
    public final static String CONFIGURATION_SERVICE_LOCATIONS              = "/papi/user/%d/locations";
    public final static String CONFIGURATION_SERVICE_BBSLIST                = "/papi/bbs/observations";
    public final static String CONFIGURATION_SERVICE_BBS_LOOKUPS            = "/papi/bbs/lookups";
    public final static String CONFIGURATION_SERVICE_LANGUAGES              = "/papi/languages";
    public final static String CONFIGURATION_SERVICE_SYNC                   = "/papi/sync";
    public final static String CONFIGURATION_SERVICE_SYNC_CHECK             = "/papi/sync/batch/%s/status";
    public final static String CONFIGURATION_SERVICE_SYNC_DELETE            = "/papi/sync/batch/%s";
    public final static String CONFIGURATION_SERVICE_LABELS                 = "/papi/labels";

    public final static String CONFIGURATION_APPLICATION_NAME               = "Mobile Pro";
    public final static String CONFIGURATION_APPLICATION_LANGUAGE           = "en";
    public final static String CONFIGURATION_APPLICATION_TYPE               = "4";

    public final static int CALENDAR_MODULE_ID                              = 6;
    public final static int FOUNDATION_MODULE_ID                            = 9;
    public final static int AUDIT_MANAGEMENT_MODULE_ID                      = 11;
    public final static int BBS_MODULE_ID                                   = 21;
    public final static int WELLNESS_MODULE_ID                              = 27;

    public final static String CONFIGURATION_LAST_USERNAME                  = "LastUsername";
    public final static String CONFIGURATION_LAST_LANGUAGE                  = "LastLanguage";

    public final static String NOTIFICATION_CONNECTIVITY_CHANGED            = "com.processmap.connectivityprovider.notification.connectivity.changed";
    public final static String NOTIFICATION_LANGUAGE_CHANGED                = "com.processmap.languageprovider.notification.current.language.changed";
    public final static String NOTIFICATION_LANGUAGE_UPDATED                = "com.processmap.languageprovider.notification.listupdated";
    public final static String NOTIFICATION_LOCATION_CHANGED                = "com.processmap.locationprovider.notification.current.location.changed";
    public final static String NOTIFICATION_LOCATION_UPDATED                = "com.processmap.locationprovider.notification.listupdated";
    public final static String NOTIFICATION_LABEL_UPDATED                   = "com.processmap.labelprovider.notification.list.updated";
    public final static String NOTIFICATION_MODULE_UPDATED                   = "com.processmap.moduleprovider.notification.list.updated";

    public final static String CONFIGURATION_ICON_FONT                      = "fonts/MaterialIcons-Regular.ttf";

    public final static int CONFIGURATION_MAX_FAILED_LOGIN_ATTEMPT          = 3;

    private boolean initDone = false;
    private Context mContext = null;

    private static ConfigurationProvider ourInstance = new ConfigurationProvider();
    public static ConfigurationProvider getInstance() {
        return ourInstance;
    }
    private ConfigurationProvider() {
        if (!initDone) {
            mContext = AppContext.getInstance().getContext();

            DatabaseManager.getInstance().exec(ConfigurationEntity.getCreateStatement());

            initDone = true;
        }
    }

    public void close() {
        initDone = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    public String get(String name) {
        String result = null;

        Cursor cursor = DatabaseManager.getInstance().query(ConfigurationEntity.getSelectByName(name));
        if (cursor.moveToFirst()) {
            ConfigurationEntity object = new ConfigurationEntity(cursor);
            result = object.Value;
        }
        cursor.close();

        return result;
    }

    public void set(String name, String value) {
        if (value != null && name != null) {
            ConfigurationEntity config = new ConfigurationEntity(name, value);
            DatabaseManager.getInstance().insert(config);
        }
    }

    public void clear() {
        DatabaseManager.getInstance().exec(ConfigurationEntity.getClearStatement());
    }

    //
    //----------------------------------------------------------------------------------------------
    //




    //----------------------------------------------------------------------------------------------
    //
    // DatabaseManager.DatabaseCreationSupport
    //

}
