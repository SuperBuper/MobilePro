package com.processmap.mobilepro.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Util {

    public static String getTextFrom(EditText editText) {
        return editText.getText().toString();
    }

    public static void setTextTo(EditText editText, String text) {
        editText.setText(text);
    }

    public static void setTextTo(View view, int id, String text) {
        TextView textView = (TextView) view.findViewById(id);
        if (textView != null) {
            textView.setText(text);
        }
    }

    public static void sendBroadcastString(Context context, String action) {
        sendBroadcastString(context, action, null, null);
    }
    public static void sendBroadcastString(Context context, String action, String message, String string) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (message != null) {
            intent.putExtra(message, string);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
