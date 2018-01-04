package org.eu.rose.heros.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.eu.rose.heros.R;
import org.eu.rose.heros.application.HeRosApplication;
import org.eu.rose.heros.parse.JoinGameThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by eric on 17/06/15.
 */
public class JoinGameActivity extends StandingActivity implements View.OnClickListener {
    private Button disconnectedButton;
    private TextView connectionStatus;
    private Button joinGameButton;
    private Button cancelButton;
    private Spinner gamesSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_game_view);

        connectionStatus = (TextView) findViewById(R.id.connection_status);
        gamesSpinner = (Spinner) findViewById(R.id.games_spinner);
        disconnectedButton = (Button) findViewById(R.id.disconnect_button);
        disconnectedButton.setOnClickListener(this);
        joinGameButton = (Button) findViewById(R.id.join_button);
        joinGameButton.setOnClickListener(this);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(this);

    }

    public void onStart(){
        super.onStart();
        String connect = "Conected to " + HeRosApplication.getHeRosConnection().getHeRosName();
        connectionStatus.setText(connect);

        getGames();
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
            case R.id.join_button:
                joinSelectedGame();
                break;
            case R.id.cancel_button:
                finish();
                Intent cancelIntent = new Intent(this, MainMenuActivity.class);
                cancelIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(cancelIntent);
                break;
        }
    }

    // Ask parse server, the current launched games
    private void getGames() {
        startStandingState("Searching for games", "Searching for games, please wait");

        HashMap<String, Object> params = new HashMap<String, Object>();

        ParseCloud.callFunctionInBackground("getGames", params, new FunctionCallback<ArrayList<String>>() {
            public void done(ArrayList<String> result, ParseException e) {
                if (e == null) {

                    List<String> listGames = new ArrayList<String>();
                    for (int i = 0; i< result.size(); i++) {
                        listGames.add(result.get(i));
                    }
                    updateGameSpinner(listGames);

                } else {
                    Toast.makeText(((HeRosApplication) getApplicationContext()).getCurrentActivity(), "Unable to reach servers, please try again", Toast.LENGTH_SHORT)
                            .show();
                }
                stopStandingState();
            }
        });
    }

    // Put the games received in the spinner
    private void updateGameSpinner(final List<String> listGames){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ArrayAdapter<String> adapterGames = new ArrayAdapter<String>(getActivity(),
                        R.layout.spinner_item, listGames);
                adapterGames.setDropDownViewResource(R.layout.spinner_item);
                gamesSpinner.setAdapter(adapterGames);

                joinGameButton.setVisibility(View.VISIBLE);
            }
        });

    }
    // Try to start the game selected in the spinner
    private void joinSelectedGame(){
        if (gamesSpinner.getSelectedItemPosition() == -1) {
            Toast.makeText(this, "You need to select a game!", Toast.LENGTH_SHORT)
                    .show();
        } else {
            String joinedGameName = gamesSpinner.getSelectedItem().toString();
            Thread join = new JoinGameThread(this, joinedGameName);
            join.start();
        }
    }

    private JoinGameActivity getActivity(){
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
