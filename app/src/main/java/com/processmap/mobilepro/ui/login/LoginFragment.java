package com.processmap.mobilepro.ui.login;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.processmap.mobilepro.R;
import com.processmap.mobilepro.data.common.LanguageEntity;
import com.processmap.mobilepro.data.common.TenantEntity;
import com.processmap.mobilepro.modules.common.AppContext;
import com.processmap.mobilepro.modules.common.ConfigurationProvider;
import com.processmap.mobilepro.modules.common.ConnectivityProvider;
import com.processmap.mobilepro.modules.common.LabelProvider;
import com.processmap.mobilepro.modules.common.LanguageProvider;
import com.processmap.mobilepro.modules.common.TenantProvider;


public class LoginFragment extends Fragment {

    View fragment;

    TextView mConnectivityStatus;

    TextView mTitle;

    TextView mCleintName;
    TextView mProductName;

    TextView mUsernameIcon;
    TextView mPasswordIcon;
    TextView mLanguageIcon;

    EditText mUsername;
    EditText mPassword;
    Spinner mLanguage;

    TextInputLayout mUsernameLayout;
    TextInputLayout mPasswordLayout;

    Button mButton;

    Typeface mIconFont;

    LanguageEntity selectedLanguage = null;
    BroadcastReceiver mLanguageUpdated;
    BroadcastReceiver mConnectivityChanged;

    private OnLoginFragmentInteractionListener mListener;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment = (View) inflater.inflate(R.layout.fragment_login, container, false);

        mConnectivityStatus = (TextView) fragment.findViewById(R.id.LoginFragment_ConnectivityStatus);

        mTitle = (TextView) fragment.findViewById(R.id.LoginFragment_Title);

        mCleintName = (TextView) fragment.findViewById(R.id.LoginFragment_ClientName);
        mProductName = (TextView) fragment.findViewById(R.id.LoginFragment_ProductName);

        mUsernameIcon = (TextView) fragment.findViewById(R.id.LoginFragment_UsernameIcon);
        mPasswordIcon = (TextView) fragment.findViewById(R.id.LoginFragment_PasswordIcon);
        mLanguageIcon = (TextView) fragment.findViewById(R.id.LoginFragment_LanguageIcon);

        mUsername = (EditText) fragment.findViewById(R.id.LoginFragment_UsernameEditText);
        mPassword = (EditText) fragment.findViewById(R.id.LoginFragment_PasswordEditText);
        mLanguage = (Spinner) fragment.findViewById(R.id.LoginFragment_LanguageSpinner);

        mUsernameLayout = (TextInputLayout) fragment.findViewById(R.id.LoginFragment_UsernameLayout);
        mPasswordLayout = (TextInputLayout) fragment.findViewById(R.id.LoginFragment_PasswordLayout);

        mButton = (Button) fragment.findViewById(R.id.LoginFragment_Button);

        mIconFont = Typeface.createFromAsset(getActivity().getAssets(), ConfigurationProvider.CONFIGURATION_ICON_FONT);

        mUsernameIcon.setTypeface(mIconFont);
        mPasswordIcon.setTypeface(mIconFont);
        mLanguageIcon.setTypeface(mIconFont);

        TenantEntity tenant = TenantProvider.getInstance().get();
        mCleintName.setText(tenant.ClientName);
        mProductName.setText(tenant.ProductName);

        mUsernameIcon.setText("\uE7FD");
        mPasswordIcon.setText("\uE897");
        mLanguageIcon.setText("\uE8E2");

        LabelProvider labels = LabelProvider.getInstance();

        mTitle.setText(labels.get(4, ConfigurationProvider.FOUNDATION_MODULE_ID));
        mUsernameLayout.setHint(labels.get(1, ConfigurationProvider.FOUNDATION_MODULE_ID));
        mPasswordLayout.setHint(labels.get(2, ConfigurationProvider.FOUNDATION_MODULE_ID));
        mLanguage.setPrompt(labels.get(1, ConfigurationProvider.FOUNDATION_MODULE_ID));
        mButton.setText(labels.get(4, ConfigurationProvider.FOUNDATION_MODULE_ID));

        String lastUsername = ConfigurationProvider.getInstance().get(ConfigurationProvider.CONFIGURATION_LAST_USERNAME);
        if (lastUsername != null) {
            mUsername.setText(lastUsername);
            mPassword.requestFocus();
        }
        selectedLanguage = LanguageProvider.getInstance().getByCode(ConfigurationProvider.getInstance().get(ConfigurationProvider.CONFIGURATION_LAST_LANGUAGE));
        if (selectedLanguage == null) {
            selectedLanguage = LanguageProvider.getInstance().getByCode(ConfigurationProvider.CONFIGURATION_APPLICATION_LANGUAGE);
        }

        updateOnConnectivity();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin();
            }
        });

        mLanguage.setAdapter(new LanguageAdapter(getActivity(), LanguageProvider.getInstance().getList()));
        mLanguage.setSelection(((LanguageAdapter) mLanguage.getAdapter()).findByCode(selectedLanguage != null ? selectedLanguage.Code : ConfigurationProvider.CONFIGURATION_APPLICATION_LANGUAGE));
        mLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                selectedLanguage = new LanguageEntity(cursor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ;
            }
        });

        mLanguageUpdated = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((LanguageAdapter) mLanguage.getAdapter()).changeCursor(LanguageProvider.getInstance().getList());
                mLanguage.setSelection(((LanguageAdapter) mLanguage.getAdapter()).findByCode(selectedLanguage != null ? selectedLanguage.Code : ConfigurationProvider.CONFIGURATION_APPLICATION_LANGUAGE));
            }
        };

        mConnectivityChanged = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateOnConnectivity();
            }
        };

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnLoginFragmentInteractionListener) {
            mListener = (OnLoginFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnLoginFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mLanguageUpdated, new IntentFilter(ConfigurationProvider.NOTIFICATION_LANGUAGE_UPDATED));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mConnectivityChanged, new IntentFilter(ConfigurationProvider.NOTIFICATION_CONNECTIVITY_CHANGED));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mLanguageUpdated);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mConnectivityChanged);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void onLogin() {
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();
        if (username.length() > 0 && password.length() > 0) {
            if (mListener != null) {
                enableControls(false);
                mListener.onLogin(this, username, password, selectedLanguage);
            }
        }
    }

    public void enableControls(Boolean enable) {
        mUsernameLayout.setEnabled(enable);
        mUsername.setEnabled(enable);
        mPasswordLayout.setEnabled(enable);
        mPassword.setEnabled(enable);
        mLanguage.setEnabled(enable);
        mButton.setEnabled(enable);
    }

    public void clearPassword() {
        mPassword.setText("");
    }

    private void updateOnConnectivity() {
        Boolean onLine = ConnectivityProvider.getInstance().isOnline();
        LabelProvider labels = LabelProvider.getInstance();

        TableRow row = (TableRow) fragment.findViewById(R.id.LoginFragment_LanguageRow);
        row.setVisibility(onLine ? View.VISIBLE : View.INVISIBLE);

        mConnectivityStatus.setText(onLine ? labels.get(47, ConfigurationProvider.FOUNDATION_MODULE_ID) : labels.get(46, ConfigurationProvider.FOUNDATION_MODULE_ID));
        mConnectivityStatus.setTextColor(getResources().getColor(onLine ? R.color.colorOnline : R.color.colorOffline));
    }

    public interface OnLoginFragmentInteractionListener {
        void onLogin(LoginFragment fragment, String username, String password, LanguageEntity language);
    }

    private class LanguageAdapter extends CursorAdapter {

        public LanguageAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            LanguageEntity language = new LanguageEntity(cursor);

            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);

            text1.setText(language.Description);
            if (text2 != null) {
                text2.setText(language.NativeDesc);
            }
        }

        @Override
        public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        @Override
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
        }

        public Integer findByCode(String code) {
            Cursor cursor = getCursor();
            for (Integer i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    LanguageEntity language = new LanguageEntity(cursor);
                    if (language.Code.equals(code)) {
                        return i;
                    }
                }
            }
            return 0;
        }
    }
}
