package com.processmap.mobilepro.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.processmap.mobilepro.R;
import com.processmap.mobilepro.data.common.LabelEntity;
import com.processmap.mobilepro.data.common.LocationEntity;
import com.processmap.mobilepro.data.common.ModuleEntity;
import com.processmap.mobilepro.modules.common.ConfigurationProvider;
import com.processmap.mobilepro.modules.common.ConnectivityProvider;
import com.processmap.mobilepro.modules.common.LabelProvider;
import com.processmap.mobilepro.modules.common.LocationProvider;
import com.processmap.mobilepro.modules.common.ModuleProvider;
import com.processmap.mobilepro.ui.location.LocationFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Menu mainMenu;

    TextView mLocationLabel;
    TextView mConnectivityStatus;

    BroadcastReceiver mLabelUpdated;
    BroadcastReceiver mMenuUpdated;
    BroadcastReceiver mConnectivityUpdated;
    BroadcastReceiver mLocationChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LinearLayout header = (LinearLayout) navigationView.getHeaderView(0);
        mLocationLabel = (TextView) header.findViewById(R.id.NavHeader_Location);
        mConnectivityStatus = (TextView) header.findViewById(R.id.NavHeader_ConnectivityStatus);

        mainMenu = navigationView.getMenu();

        updateLabels();
        updateMenu();
        updateOnConnectivity();
        updateLocation();

        mMenuUpdated = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateMenu();
            }
        };

        mLabelUpdated = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateLabels();
            }
        };

        mConnectivityUpdated = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateOnConnectivity();
            }
        };

        mLocationChanged = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateLocation();
            }
        };

        drawer.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawer.openDrawer(GravityCompat.START);
            }
        }, 500);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mLabelUpdated, new IntentFilter(ConfigurationProvider.NOTIFICATION_LABEL_UPDATED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMenuUpdated, new IntentFilter(ConfigurationProvider.NOTIFICATION_MODULE_UPDATED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mConnectivityUpdated, new IntentFilter(ConfigurationProvider.NOTIFICATION_CONNECTIVITY_CHANGED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationChanged, new IntentFilter(ConfigurationProvider.NOTIFICATION_LOCATION_CHANGED));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLabelUpdated);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMenuUpdated);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectivityUpdated);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationChanged);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.main_menu_calendar) {
            setMainFragment();
        } else if (id == R.id.main_menu_audit_management) {
            setMainFragment();
        } else if (id == R.id.main_menu_bbs) {
            setMainFragment();
        } else if (id == R.id.main_menu_wellness) {
            setMainFragment();
        } else if (id == R.id.main_menu_change_location) {
            setLocationFragment();
        } else if (id == R.id.main_menu_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    //
    //----------------------------------------------------------------------------------------------
    //

    private void updateMenu() {
        setMenuItem(R.id.main_menu_calendar, ConfigurationProvider.CALENDAR_MODULE_ID);
        setMenuItem(R.id.main_menu_audit_management, ConfigurationProvider.AUDIT_MANAGEMENT_MODULE_ID);
        setMenuItem(R.id.main_menu_bbs, ConfigurationProvider.BBS_MODULE_ID);
        setMenuItem(R.id.main_menu_wellness, ConfigurationProvider.WELLNESS_MODULE_ID);
    }

    private void setMenuItem(Integer menuId, Integer moduleId) {
        MenuItem menuItem = mainMenu.findItem(menuId);
        if (menuItem != null) {
            ModuleEntity module = ModuleProvider.getInstance().getById(moduleId);
            if (module != null) {
                menuItem.setVisible(true);
                menuItem.setTitle(module.Name);
            } else {
                menuItem.setVisible(false);
            }
        }
    }

    private void updateOnConnectivity() {
        Boolean onLine = ConnectivityProvider.getInstance().isOnline();
        LabelProvider labels = LabelProvider.getInstance();

        MenuItem locationItem = mainMenu.findItem(R.id.main_menu_change_location);
        locationItem.setVisible(onLine);

        mConnectivityStatus.setText(onLine ? labels.get(47, ConfigurationProvider.FOUNDATION_MODULE_ID) : labels.get(46, ConfigurationProvider.FOUNDATION_MODULE_ID));
        mConnectivityStatus.setTextColor(getResources().getColor(onLine ? R.color.colorOnline : R.color.colorOffline));
    }

    private void updateLabels() {
        LabelProvider labels = LabelProvider.getInstance();

        MenuItem locationItem = mainMenu.findItem(R.id.main_menu_change_location);
        locationItem.setTitle(labels.get(40, ConfigurationProvider.FOUNDATION_MODULE_ID));

        MenuItem logoutItem = mainMenu.findItem(R.id.main_menu_logout);
        logoutItem.setTitle(labels.get(41, ConfigurationProvider.FOUNDATION_MODULE_ID));

        updateOnConnectivity();
        updateMenu();
    }

    private void updateLocation() {
        LocationEntity location = LocationProvider.getInstance().get();
        if (location != null) {
            mLocationLabel.setText(location.Name);
        }
    }

    private void setMainFragment() {
        MainFragment fragment = new MainFragment();
        setFragment(fragment, "MainFragment");
    }

    private void setLocationFragment() {
        LocationFragment fragment = new LocationFragment();
        setFragment(fragment, "LocationFragment");
    }

    private void setFragment(Fragment fragment, String fragmentName) {
        FragmentManager f = getFragmentManager();
        FragmentTransaction ft = f.beginTransaction();
        ft = ft.replace(R.id.main_activity_container, fragment, fragmentName);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }
}
