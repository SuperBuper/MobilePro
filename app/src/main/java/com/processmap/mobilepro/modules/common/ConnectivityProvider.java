package com.processmap.mobilepro.modules.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import com.processmap.mobilepro.R;
import com.processmap.mobilepro.data.common.LocationEntity;
import com.processmap.mobilepro.data.common.TenantEntity;
import com.processmap.mobilepro.data.common.UserEntity;
import com.processmap.mobilepro.util.RestClient;
import com.processmap.mobilepro.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class ConnectivityProvider extends BroadcastReceiver {

    private final static String AUTHORIZATION_HEADER_NAME = "Authorization";

    private boolean initDone = false;
    private Context mContext = null;
    private LabelProvider labelProvider = null;

    private static CookieManager cookiemanager = null;
    private boolean broadcastReceiver = false;

    private boolean networkOffline = false;

    private String authHeaderToken = null;

    private static ConnectivityProvider ourInstance = new ConnectivityProvider();
    public static ConnectivityProvider getInstance() {
        return ourInstance;
    }

    private ConnectivityProvider() {
        if (!initDone) {
            mContext = AppContext.getInstance().getContext();

            cookiemanager = new CookieManager();
            cookiemanager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookiemanager);

            /*
            *  fix for
            *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
            *       sun.security.validator.ValidatorException:
            *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
            *               unable to find valid certification path to requested target
            */
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

                    }
            };

            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            /*
            * end of the fix
            */

            labelProvider = LabelProvider.getInstance();
            registerReceiver();
            initDone = true;
        }
    }

    public void close() {
        if(broadcastReceiver) {
            mContext.unregisterReceiver(this);
            broadcastReceiver = false;
        }
        initDone = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //


    //
    //----------------------------------------------------------------------------------------------
    //

    public boolean isOnline() {
        android.net.ConnectivityManager connMgr = (android.net.ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }

    //
    //broadcast receiver functions
    //
    public void registerReceiver() {
        if(!broadcastReceiver) {
            broadcastReceiver = true;
            mContext.registerReceiver(this, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //networkOffline = intent.getBooleanExtra(android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        Util.sendBroadcastString(mContext, ConfigurationProvider.NOTIFICATION_CONNECTIVITY_CHANGED);
        Toast.makeText(context, isOnline() ? labelProvider.get(47, ConfigurationProvider.FOUNDATION_MODULE_ID) : labelProvider.get(46, ConfigurationProvider.FOUNDATION_MODULE_ID), Toast.LENGTH_SHORT).show();
    }

    //----------------------------------------------------------------------------------------------
    private class doHTTPRequest extends AsyncTask<String, Void, RestClient> {
        private RestClient client;
        private RestClient.RequestMethod method;
        private ConnectivityCallback callback;
        private int requestCode;
        private String body;

        public doHTTPRequest setParams(RestClient client, RestClient.RequestMethod method, ConnectivityCallback callback) {
            this.client = client;
            this.method = method;
            this.callback = callback;
            return this;
        }

        public doHTTPRequest addBody(String body) {
            this.body = body;
            return this;
        }

        @Override
        protected RestClient doInBackground(String... urls) {
            try {
                if (authHeaderToken != null) {
                    client.AddHeader(AUTHORIZATION_HEADER_NAME, authHeaderToken);
                }
                client.AddHeader("Accept", "application/json, text/plain, */*");
                client.AddHeader("Content-Type", "application/json; charset=UTF-8");
                client.AddHeader("ApplicationType", ConfigurationProvider.CONFIGURATION_APPLICATION_TYPE);
                String acceptLanguage = LanguageProvider.getInstance().getLanguageCode();
                if (acceptLanguage != null) {
                    client.AddHeader("Accept-Language", acceptLanguage);
                }
                TenantEntity tenantEntity = TenantProvider.getInstance().get();
                if (tenantEntity != null) {
                    client.AddHeader("ConsumerId", tenantEntity.Id.toString());
                }
                LocationEntity locationEntity = LocationProvider.getInstance().get();
                if (locationEntity != null) {
                    client.AddHeader("LocationId", locationEntity.Id.toString());
                }
                UserEntity userEntity = SecurityProvider.getInstance().get();
                if (userEntity != null) {
                    client.AddHeader("UserId", userEntity.Id.toString());
                }
                client.requestData = body;
                client.Execute(method);

                if (callback != null) {
                    callback.backgroundTask(client.responseCode, client.responseData, client.errorMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return client;
        }

        @Override
        protected void onPostExecute(RestClient client) {
            if (client.responseHeaders != null) {
                if ((client.responseHeaders.get(AUTHORIZATION_HEADER_NAME) != null) && (!client.responseHeaders.get(AUTHORIZATION_HEADER_NAME).isEmpty())) {
                    authHeaderToken = client.responseHeaders.get(AUTHORIZATION_HEADER_NAME).get(0);
                }
            }

            if (callback != null) {
                callback.postExecuteTask(client.responseCode, client.responseData, client.errorMessage);
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    public interface ConnectivityCallback {
        public void backgroundTask(int responseCode, String responseData, String errorMessage);
        public void postExecuteTask(int responseCode, String responseData, String errorMessage);
    }

//    public interface ConnectivityCallback {
//        void connectivityCallBack(int requestCode, int responseCode, String responseData, String errorMessage);
//    }

    //----------------------------------------------------------------------------------------------

    public void getRequest(String url, ConnectivityCallback callback) {
        if (isOnline()) {
            RestClient client = new RestClient(url);
            new doHTTPRequest().setParams(client, RestClient.RequestMethod.GET, callback).execute();
        } else {
            if (callback != null) {
                callback.postExecuteTask(ConfigurationProvider.RESPONSE_OFFLINE, null, null);
            }
        }
    }

    public void postRequest(String url, String json, ConnectivityCallback callback) {
        if (isOnline()) {
            RestClient client = new RestClient(url);
            new doHTTPRequest().setParams(client, RestClient.RequestMethod.POST, callback).addBody(json).execute();
        } else {
            if (callback != null) {
                callback.postExecuteTask(ConfigurationProvider.RESPONSE_OFFLINE, null, null);
            }
        }
    }
}
