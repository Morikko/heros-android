package org.eu.rose.heros.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.eu.rose.heros.R;
import org.eu.rose.heros.application.HeRosApplication;
import org.eu.rose.heros.connection.HeRosConnection;
import org.eu.rose.heros.manager.GameManager;
import org.eu.rose.heros.parse.JoinGameThread;

import java.util.HashMap;

/**
 * Created by eric on 17/06/15.
 */
public class CreateGameActivity extends StandingActivity implements View.OnClickListener {
    private TextView connectionStatus;
    private TextView gameName;
    private Button disconnectedButton;
    private Button cancelButton;
    private ImageButton deathMatchButton;
    private ImageButton teamMatchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_game_view);

        connectionStatus = (TextView) findViewById(R.id.connection_status);
        gameName = (TextView) findViewById(R.id.game_name_text);

        disconnectedButton = (Button) findViewById(R.id.disconnect_button);
        disconnectedButton.setOnClickListener(this);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);
        deathMatchButton = (ImageButton) findViewById(R.id.deathmatch_button);
        deathMatchButton.setOnClickListener(this);
        teamMatchButton = (ImageButton) findViewById(R.id.team_deathmatch_button);
        teamMatchButton.setOnClickListener(this);
    }

    public void onStart(){
        super.onStart();
        String connect = "Conected to " + HeRosApplication.getHeRosConnection().getHeRosName();
        connectionStatus.setText(connect);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.disconnect_button:
                finish();
                HeRosApplication.getHeRosConnection().disconnect();
                Intent disconnectIntent = new Intent(this, ScanActivity.class);
                disconnectIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(disconnectIntent);
                break;
            case R.id.cancel_button:
                finish();
                Intent cancelIntent = new Intent(this, MainMenuActivity.class);
                cancelIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(cancelIntent);
                break;
            case R.id.deathmatch_button:
                createDeathMatch();
                break;
            case R.id.team_deathmatch_button:
                createTeamMatch();
                break;
        }
    }


    private void createTeamMatch(){
        final String newGameName = gameName.getText().toString();

        if (newGameName.length() < 4) {
            Toast.makeText(this, "You need to enter valid game parameters!", Toast.LENGTH_SHORT)
                    .show();
        } else {
            createGame(newGameName, GameManager.TEAMMATCH);
        }

    }

    private void createDeathMatch() {

        final String newGameName = gameName.getText().toString();

        if (newGameName.length() < 4) {
            Toast.makeText(this, "You need to enter valid game parameters!", Toast.LENGTH_SHORT)
                    .show();
        } else {
            createGame(newGameName, GameManager.DEATHMATCH);
        }

    }

    private void createGame(final String gameName, final int type) {

        startStandingState("Creating game", "Creating the game, please wait");

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("gameName", gameName);
        params.put("maxNbrOfPlayers", 4);
        params.put("gameType", type);
        if (type == 0) {
            params.put("nbrOfTeams", 1); // Deathmatch
        } else {
            params.put("nbrOfTeams", 2); // Team Deathmatch
        }

        ParseCloud.callFunctionInBackground("createGame", params, new FunctionCallback<Boolean>() {
            public void done(Boolean alreadyExists, ParseException e) {
                if (e == null) {
                    if (alreadyExists) {
                        Toast.makeText(getApplicationContext(), "That game name is already taken!", Toast.LENGTH_SHORT)
                                .show();
                        stopStandingState();
                    } else {
                        stopStandingState();
                        startStandingState("Starting in progress...", "Game is coming soon !");
                        finish();
                        Thread join = new JoinGameThread(getThis(), gameName);
                        join.start();
                    }
                } else {
                    Toast.makeText(((HeRosApplication) getApplicationContext()).getCurrentActivity(), "Unable to create game, please try again", Toast.LENGTH_SHORT)
                            .show();
                    stopStandingState();
                }
            }
        });

    }

    protected CreateGameActivity getThis(){
        return this;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
            Intent cancelIntent = new Intent(this, MainMenuActivity.class);
            cancelIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(cancelIntent);
        }
        return false;
    }
}
