package org.eu.rose.heros.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.eu.rose.heros.R;
import org.eu.rose.heros.application.HeRosApplication;
import org.eu.rose.heros.manager.GameManager;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by eric on 17/06/15.
 * Menu is reachable only with a connected HeRos
 */
public class MainMenuActivity extends RegisterActivity implements View.OnClickListener{

    private TextView connectionStatus;
    private EditText renameHeros;
    private Button   renameButton;
    private Button   disconnectButton;
    private Button   createGameButton;
    private Button   joinGameButton;
    private Button   exploreButton;

    private MediaPlayer goodnight, shuttingDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_menu_view);

        goodnight = MediaPlayer.create(this, R.raw.goodnight);
        shuttingDown = MediaPlayer.create(this, R.raw.shutting_down);

        connectionStatus = (TextView) findViewById(R.id.connection_status);
        renameHeros = (EditText) findViewById(R.id.rename_heros);
        renameButton = (Button) findViewById(R.id.rename_button);
        renameButton.setOnClickListener(this);
        disconnectButton = (Button) findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(this);
        createGameButton = (Button) findViewById(R.id.create_game_button);
        createGameButton.setOnClickListener(this);
        joinGameButton = (Button) findViewById(R.id.join_game_button);
        joinGameButton.setOnClickListener(this);
        exploreButton = (Button) findViewById(R.id.explore_button);
        exploreButton.setOnClickListener(this);

    }

    public void onStart(){
        super.onStart();
        String connect = "Conected to " + HeRosApplication.getHeRosConnection().getHeRosName();
        connectionStatus.setText(connect);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.disconnect_button:
                finish();
                HeRosApplication.getHeRosConnection().disconnect();
                Intent disconnectIntent = new Intent(this, ScanActivity.class);
                disconnectIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(disconnectIntent);
                break;
            case R.id.rename_button:
                renameHeros();
                break;
            case R.id.create_game_button:
                Intent createIntent = new Intent(this, CreateGameActivity.class);
                createIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(createIntent);
                break;
            case R.id.join_game_button:
                Intent joinIntent = new Intent(this, JoinGameActivity.class);
                joinIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(joinIntent);
                break;
            case R.id.explore_button:
                HeRosApplication.gameManager.setGameConfiguration("Explore", 0, 0, GameManager.EXPLORE);
                Intent exploreIntent = new Intent(this, GameActivity.class);
                exploreIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(exploreIntent);
                break;
        }

    }

    private void renameHeros(){
        final String newName = renameHeros.getText().toString();

        if (newName.equals(HeRosApplication.getHeRosConnection().getHeRosName()) || newName.length() < 4) {

            Toast.makeText(this, "You need to type a new name first (> 3 characters)!", Toast.LENGTH_SHORT)
                    .show();

        } else {

            final ProgressDialog dlg = new ProgressDialog(this);
            dlg.setTitle("Renaming HeRos");
            dlg.setMessage("Renaming your HeRos, please wait");
            dlg.show();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("newName", newName);
            params.put("macAddress", HeRosApplication.getHeRosConnection().getHeRosMacAddress());

            ParseCloud.callFunctionInBackground("renameHeros", params, new FunctionCallback<Object>() {
                public void done(Object result, ParseException e) {
                    if (e == null) {
                        Toast.makeText(((HeRosApplication) getApplicationContext()).getCurrentActivity(), "Success! Disconnect and rescan to see the change!", Toast.LENGTH_SHORT)
                                .show();
                        HeRosApplication.getHeRosConnection().setHeRosName(newName);
                        dlg.dismiss();

                    } else {
                        Toast.makeText(((HeRosApplication) getApplicationContext()).getCurrentActivity(), "Unable to reach servers, please try again", Toast.LENGTH_SHORT)
                                .show();
                        dlg.dismiss();
                    }
                }
            });

        }
    }

    private void disconnect() {
        Random r = new Random();
        int i = r.nextInt(2);
        if (i == 0) {
            shuttingDown.start();
        } else {
            goodnight.start();
        }

        HeRosApplication.getHeRosConnection().disconnect();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
            HeRosApplication.getHeRosConnection().disconnect();
            Intent disconnectIntent = new Intent(this, ScanActivity.class);
            disconnectIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(disconnectIntent);
        }
        return false;
    }

}
