package com.mapbox.services.android.navigation.v5.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

public final class ContextUtils {


    @SuppressLint("WrongConstant")
    public static Intent registerReceiver(Context context, BroadcastReceiver receiver, IntentFilter filter) {
        final int receiverFlags = 0x4;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.registerReceiver(receiver, filter, receiverFlags);
        } else {
            return context.registerReceiver(receiver, filter);
        }
    }

    public static Context getActivityContext(Context context){
        Context currentContext;
        if (context instanceof ContextWrapper){
            if (context instanceof Activity)
            {
                currentContext = context;
            }
            else
            {
                currentContext = (((ContextWrapper) context).getBaseContext());
            }
        }else {
            currentContext = context;
        }
        return currentContext;
    }

}
