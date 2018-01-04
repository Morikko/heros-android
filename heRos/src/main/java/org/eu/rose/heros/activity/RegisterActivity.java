package org.eu.rose.heros.activity;

import android.app.Activity;
import android.os.Bundle;

import org.eu.rose.heros.application.HeRosApplication;

/**
 * Created by eric on 20/06/15.
 * Saved the current activity in application
 */
public class RegisterActivity extends Activity{
    protected HeRosApplication mMyApp;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMyApp = (HeRosApplication)this.getApplicationContext();
    }
    protected void onResume() {
        super.onResume();
        mMyApp.setCurrentActivity(this);
    }
    protected void onPause() {
        clearReferences();
        super.onPause();
    }
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = mMyApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            mMyApp.setCurrentActivity(null);
    }
}
