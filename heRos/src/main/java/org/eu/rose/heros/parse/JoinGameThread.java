package org.eu.rose.heros.parse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.eu.rose.heros.activity.GameActivity;
import org.eu.rose.heros.activity.StandingActivity;
import org.eu.rose.heros.application.HeRosApplication;
import org.eu.rose.heros.manager.GameManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by eric on 21/06/15.
 */
public class JoinGameThread extends Thread {
    private String gameName;
    private StandingActivity activity;

    // Try to join the game in parameter
    public JoinGameThread(StandingActivity activity, String gameName){
        this.gameName = gameName;
        this.activity = activity;
    }

    public void run(){
        activity.startStandingState("Starting in progress...", "Game is coming soon !");
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("macAddress", HeRosApplication.getHeRosConnection().getHeRosMacAddress());
        params.put("gameName", gameName);
        params.put("herosName", HeRosApplication.getHeRosConnection().getHeRosName());

        ParseCloud.callFunctionInBackground("joinGame", params, new FunctionCallback<ArrayList<Integer>>() {
            public void done(ArrayList<Integer> assignedParameters, ParseException e) {
                if (e == null) {

                    if (assignedParameters.get(0) == -1 && assignedParameters.get(1) == -1) {
                        Toast.makeText(activity, "You already joined " + gameName + " and died!", Toast.LENGTH_LONG)
                                .show();
                    } else if (assignedParameters.get(0) == -1 && assignedParameters.get(1) == 0) {
                        Toast.makeText(activity, "Game " + gameName + " is full!", Toast.LENGTH_LONG)
                                .show();
                    } else {

                        Toast.makeText(activity, "You joined " + gameName + ". Good luck!", Toast.LENGTH_SHORT)
                                .show();

                        int assignedAddress = assignedParameters.get(0);
                        int assignedTeam = assignedParameters.get(1);
                        // DeathMatch = 0 | TeamMatch = 1
                        int type = assignedParameters.get(2);

                        HeRosApplication.gameManager.setGameConfiguration(gameName, assignedAddress, assignedTeam, type);

                        activity.stopStandingState();

                        Intent gameIntent = new Intent(activity, GameActivity.class);
                        gameIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        activity.startActivity(gameIntent);

                    }
                } else {
                    Toast.makeText(activity, "Unable to join game, please try again", Toast.LENGTH_SHORT)
                            .show();
                }
                activity.stopStandingState();
            }
        });

    }
}
