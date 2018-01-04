package org.eu.rose.heros.application;

import android.app.Activity;
import android.app.Application;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

import org.eu.rose.heros.connection.HeRosConnection;
import org.eu.rose.heros.manager.GameManager;

public class HeRosApplication extends Application {
	public static boolean soundOn = true;

    private Activity mCurrentActivity = null;

	// Background Thread
	private static Thread messageThread = null;
	private static Thread receptionThread = null;
	private static Thread pollingThread = null;
	private static Thread clientThread = null;

	// Sound Option
	private final static int MAX_VOLUME = 100;
	private final static int wantedVolume = 100;
	private static float volume = (float) (1 - (Math.log(MAX_VOLUME - wantedVolume) / Math.log(MAX_VOLUME)));
	private static HeRosConnection heRosConnection;
	public static GameManager gameManager;

	@Override
	public void onCreate() {
		super.onCreate();

		heRosConnection = new HeRosConnection(this);
		gameManager = new GameManager(heRosConnection);

		ParseCrashReporting.enable(this);

		Parse.initialize(this, "XXXXXXXXX", "XXXXXXXXXXXX");
	}

	public static HeRosConnection getHeRosConnection(){
		return heRosConnection;
	}

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }

	public void onDestroy(){

	}

}
