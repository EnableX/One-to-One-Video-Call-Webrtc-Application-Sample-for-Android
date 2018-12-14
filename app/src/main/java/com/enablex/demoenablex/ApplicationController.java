package com.enablex.demoenablex;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class ApplicationController extends Application implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2
{
    public static Context context;

    public static SharedPreferences getSharedPrefs()
    {
        return context.getSharedPreferences("APP_PREF_", MODE_PRIVATE);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState)
    {

    }

    @Override
    public void onActivityStarted(Activity activity)
    {

    }

    @Override
    public void onActivityResumed(Activity activity)
    {

    }

    @Override
    public void onActivityPaused(Activity activity)
    {

    }

    @Override
    public void onActivityStopped(Activity activity)
    {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState)
    {

    }

    @Override
    public void onActivityDestroyed(Activity activity)
    {

    }
}
