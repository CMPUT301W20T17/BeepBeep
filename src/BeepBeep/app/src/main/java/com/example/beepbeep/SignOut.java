package com.example.beepbeep;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;

class SignOut {
    /**
     * Will delete all shared pref data and restart app, effectively sign out user
     * @param c context
     */
    static void now(Context c){
        // delete all shared preferences saved on the machine
        File sharedPreferenceFile = new File(c.getFilesDir().getParentFile().getAbsolutePath() + File.separator + "shared_prefs");
        File[] listFiles = sharedPreferenceFile.listFiles();
        if(listFiles != null){
            for (File file : listFiles) {
                boolean a = file.delete();
            }
        }else{
            Log.d("Acount", "fuck");
        }

        // relaunch app
        Intent mStartActivity = new Intent(c, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(c, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }
}
