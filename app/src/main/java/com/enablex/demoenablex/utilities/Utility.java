package com.enablex.demoenablex.utilities;


import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;


import androidx.core.app.ActivityCompat;



public class Utility {

    /***
     * Method to get the current time
     * @return
     */


    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public static void goToPermissionPage(Activity activity){
        try{
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", activity.getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }



}

