package com.nurulquran.audio.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.nurulquran.audio.PacketUtility;
import com.nurulquran.audio.R;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.database.MySharePreferences;
import com.nurulquran.audio.gcm.RegistrationIntentService;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.object.Radio;
import com.nurulquran.audio.util.LanguageUtil;
import com.nurulquran.audio.util.MySharedPreferences;
import com.nurulquran.audio.util.NetworkUtil;

public class SplashActivity extends Activity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.e("aaaaaaa", "Emei : " + PacketUtility.getImei(this));

        LanguageUtil.setLocale(new MySharedPreferences(this).getLanguage(),
                this);

        GlobalValue.constructor(this);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        startMainActivity();
    }

    private void startMainActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this,
                        MainActivity.class));
                overridePendingTransition(R.anim.slide_in_left,
                        R.anim.slide_out_left);
                finish();
            }
        }, 3000);
    }

    @Override
    public void onBackPressed() {
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }


}
