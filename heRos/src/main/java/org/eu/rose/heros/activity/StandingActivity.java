package org.eu.rose.heros.activity;

import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * Created by eric on 23/06/15.
 * Activity that can open an process dialog to stay in standing state
 */
public class StandingActivity extends RegisterActivity {
    private ProgressDialog dlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dlg = new ProgressDialog(this);
    }

    public void startStandingState(final String title, final String message){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dlg.setTitle(title);
                dlg.setMessage(message);
                dlg.show();
            }
        });
    }

    public void stopStandingState(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dlg.dismiss();
            }
        });
    }

}
