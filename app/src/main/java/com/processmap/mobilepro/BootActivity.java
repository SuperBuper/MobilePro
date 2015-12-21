package com.processmap.mobilepro;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.processmap.mobilepro.data.common.LanguageEntity;
import com.processmap.mobilepro.data.common.LocationEntity;
import com.processmap.mobilepro.data.common.UserEntity;
import com.processmap.mobilepro.modules.common.SecurityProvider;
import com.processmap.mobilepro.modules.common.SessionProvider;
import com.processmap.mobilepro.data.common.TenantEntity;
import com.processmap.mobilepro.modules.common.AppContext;
import com.processmap.mobilepro.modules.common.ConfigurationProvider;
import com.processmap.mobilepro.modules.common.ConnectivityProvider;
import com.processmap.mobilepro.modules.common.DatabaseManager;
import com.processmap.mobilepro.modules.common.TenantProvider;
import com.processmap.mobilepro.ui.login.AccessCodeFragment;
import com.processmap.mobilepro.ui.login.LoginFragment;
import com.processmap.mobilepro.ui.main.MainActivity;


public class BootActivity extends AppCompatActivity implements AccessCodeFragment.OnAccessCodeFragmentInteractionListener, LoginFragment.OnLoginFragmentInteractionListener {

    public final static int SIGN_ACTIVITY = 1;
    public final static int LOGIN_ACTIVITY = 2;
    public final static int MAIN_ACTIVITY = 3;

    public final static String FAILED_LOGIN_ATTEMPT_COUNT = "com.processmap.mobilepro.FAILED_LOGIN_ATTEMPT_COUNT";
    private int failedAttemptsCount = 0;

    private Context mContext = null;
    private DatabaseManager databaseManager = null;
    private ConnectivityProvider connectivityProvider = null;
    private AppContext appContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);

        if (savedInstanceState != null) {
            failedAttemptsCount = savedInstanceState.getInt(FAILED_LOGIN_ATTEMPT_COUNT);
        } else {
            initialize();
        }

        if (!isConfigured()) {
            startSignActivity();
        } else {
            startLoginActivity();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(FAILED_LOGIN_ATTEMPT_COUNT, failedAttemptsCount);
    }

    @Override
    public void onDestroy() {
        //terminate();
        super.onDestroy();
    }

    private void initialize() {
        mContext = getApplicationContext();
        appContext = AppContext.getInstance();
        appContext.init(mContext);

        databaseManager = DatabaseManager.getInstance();
        connectivityProvider = ConnectivityProvider.getInstance();
    }

    private void terminate() {
        if (connectivityProvider != null) { connectivityProvider.close(); }
        if (databaseManager != null) { databaseManager.close(); }
    }

    private boolean isConfigured() {
        TenantEntity object = TenantProvider.getInstance().get();
        return (object != null);
    }

    private void startSignActivity() {
        failedAttemptsCount = 0;
        SessionProvider.getInstance().startTenantRegistration();
        AccessCodeFragment fragment = new AccessCodeFragment();
        setFragment(fragment, "AccessCodeFragment");
    }

    public void onAccessCode(final AccessCodeFragment fragment, final String pin) {
        TenantProvider.getInstance().validateTenant(pin, new TenantProvider.TenantProviderCallback() {
            @Override
            public void callback(int responseCode, TenantEntity tenant) {
                if (responseCode == ConfigurationProvider.RESPONSE_OK) {
                    SessionProvider.getInstance().tenantRegistrationEnded();
                    failedAttemptsCount = 0;
                    startLoginActivity();
                } else {
                    Toast.makeText(mContext, String.format("Invalid Access Code\n%s is invalid", pin), Toast.LENGTH_LONG).show();
                    fragment.clearPin();
                    fragment.enableControls(true);
                }
            }
        });
    }

    private void startLoginActivity() {
        SessionProvider.getInstance().startUserRegistration();
        LoginFragment fragment = new LoginFragment();
        setFragment(fragment, "LoginFragment");
    }

    public void onLogin(final LoginFragment fragment, String username, String password, final LanguageEntity language) {
        if (ConnectivityProvider.getInstance().isOnline()) {
            SecurityProvider.getInstance().validateUserOnline(username, password, new SecurityProvider.SecurityProviderCallback() {
                @Override
                public void callback(int responseCode, UserEntity user, LocationEntity location) {
                    if (responseCode == ConfigurationProvider.RESPONSE_OK) {
                        ConfigurationProvider.getInstance().set(ConfigurationProvider.CONFIGURATION_LAST_USERNAME, user.Username);
                        if (SessionProvider.getInstance().startOnlineSession(user, location, language)) {
                            fragment.clearPassword();
                            startMainActivity();
                        }
                    } else {
                        fragment.enableControls(true);
                        Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                        checkFailedAttempts();
                    }
                }
            });
        } else {
            SecurityProvider.getInstance().validateUserOffline(username, password, new SecurityProvider.SecurityProviderCallback() {
                @Override
                public void callback(int responseCode, UserEntity user, LocationEntity location) {
                    if (responseCode == ConfigurationProvider.RESPONSE_OK) {
                        if (SessionProvider.getInstance().startOfflineSession(user, location)) {
                            fragment.clearPassword();
                            startMainActivity();
                        }
                    } else {
                        fragment.enableControls(true);
                        Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                        checkFailedAttempts();
                    }
                }
            });
        }
    }

    private void startMainActivity() {
        failedAttemptsCount = 0;

        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, MAIN_ACTIVITY);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MAIN_ACTIVITY:
                startLoginActivity();
        }
    }
    private void checkFailedAttempts() {
        failedAttemptsCount++;
        if (failedAttemptsCount == ConfigurationProvider.CONFIGURATION_MAX_FAILED_LOGIN_ATTEMPT) {
            startSignActivity();
        }
    }

    private void setFragment(Fragment fragment, String fragmentName) {
        FragmentManager f = getFragmentManager();
        FragmentTransaction ft = f.beginTransaction();
        ft = ft.replace(R.id.container, fragment, fragmentName);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }
}
