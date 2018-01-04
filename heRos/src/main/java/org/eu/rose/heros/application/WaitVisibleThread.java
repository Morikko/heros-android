package org.eu.rose.heros.application;

import android.app.Activity;
import android.view.View;

/**
 * Created by eric on 21/06/15.
 * Wait time before put the view element VISIBLE
 */
public class WaitVisibleThread extends Thread {
    private Activity activity;
    private View view;
    private int time;

    public WaitVisibleThread(Activity activity, View view, int time){
        this.activity = activity;
        this.view = view;
        this.time = time;
    }

    public void run(){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        activity.runOnUiThread(new Runnable() {
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        });
    }
}
