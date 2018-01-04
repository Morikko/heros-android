package org.eu.rose.heros.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zerokol.views.JoystickView;

import org.eu.rose.heros.manager.GameManager;
import org.eu.rose.heros.stream.JpegViewStream;
import org.eu.rose.heros.R;
import org.eu.rose.heros.application.HeRosApplication;
import org.eu.rose.heros.application.WaitVisibleThread;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by eric on 17/06/15.
 */
public class GameActivity extends RegisterActivity implements View.OnClickListener, JoystickView.OnJoystickMoveListener {

    private final int BOMB_DELAY = 10000;
    private final int HEAL_DELAY = 10000;
    private final int BOOST_DELAY = 10000;
    private final int BOOST_USE = 5000;
    private final int POLOLU_SPEED = 50;
    private final int PIVOTING_SPEED = 31;
    private final int POLOLU_BOOST = 30;
    private final int PIVOTING_BOOST = 20;

    private int pivotingSpeed;
    private int pololuSpeed;

    private GameManager gameManager;

    private LinearLayout videoSurface;
    private Button returnButton;

    private ImageButton healButton;
    private ImageButton fireButton;
    protected ImageButton boostButton;
    private ImageButton bombButton;

    private TextView gameNameText;
    private TextView player1Text;
    private TextView player2Text;
    private TextView player3Text;
    private TextView player4Text;
    private TextView myHPText;
    private TextView myHeRosNameText;
    private TextView myTeamText;

    private JoystickView joystickView;

    private int lastAngleSent;
    private int lastPowerSent;

    private MediaPlayer targetAcquired;
    private MediaPlayer iSeeYou;
    private MediaPlayer thereYouAre;
    private MediaPlayer laserMachineGun;
    private MediaPlayer timeBomb;
    private MediaPlayer alarm;
    private MediaPlayer excited;
    private MediaPlayer surprised;

    private AlertDialog dlgAlert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_screen);
        gameManager = HeRosApplication.gameManager;

        lastAngleSent = -1;
        lastPowerSent = -1;

        videoSurface = (LinearLayout) findViewById(R.id.video_view);

        returnButton = (Button) findViewById(R.id.return_button);
        returnButton.setOnClickListener(this);

        healButton = (ImageButton) findViewById(R.id.heal_button);
        healButton.setOnClickListener(this);
        fireButton = (ImageButton) findViewById(R.id.fire_direct_button);
        fireButton.setOnClickListener(this);
        boostButton = (ImageButton) findViewById(R.id.boost_button);
        boostButton.setOnClickListener(this);
        bombButton = (ImageButton) findViewById(R.id.fire_global_button);
        bombButton.setOnClickListener(this);

        gameNameText = (TextView) findViewById(R.id.game_name);
        player1Text = (TextView) findViewById(R.id.player1_info);
        player2Text = (TextView) findViewById(R.id.player2_info);
        player3Text = (TextView) findViewById(R.id.player3_info);
        player4Text = (TextView) findViewById(R.id.player4_info);
        myHPText = (TextView) findViewById(R.id.current_hp);
        myHeRosNameText = (TextView) findViewById(R.id.player_name);
        myTeamText = (TextView) findViewById(R.id.assigned_team);

        joystickView = (JoystickView) findViewById(R.id.joystick_view);
        joystickView.setOnJoystickMoveListener(this, JoystickView.DEFAULT_LOOP_INTERVAL);

        // Sound
        iSeeYou = MediaPlayer.create(this, R.raw.i_see_you);
        targetAcquired = MediaPlayer.create(this, R.raw.target_acquired);
        thereYouAre = MediaPlayer.create(this, R.raw.there_you_are);
        laserMachineGun = MediaPlayer.create(this, R.raw.laser_machine_gun);
        timeBomb = MediaPlayer.create(this, R.raw.time_bomb);
        alarm = MediaPlayer.create(this, R.raw.r2d2_alarm);
        excited = MediaPlayer.create(this, R.raw.r2d2_excited);
        surprised = MediaPlayer.create(this, R.raw.r2d2_surprised);
    }

    public void onResume(){
        super.onResume();
        displayGame();
    }

    public void onStop(){
        super.onStop();
        gameManager.stopPlaying();
    }

    public void displayGame(){
        // Prepare display
        // Select display for good game type
        switch(gameManager.getGameType()) {
            case GameManager.EXPLORE:
                bombButton.setVisibility(View.GONE);
                boostButton.setVisibility(View.VISIBLE);
                fireButton.setVisibility(View.GONE);
                myHPText.setVisibility(View.GONE);
                healButton.setVisibility(View.GONE);
                myTeamText.setVisibility(View.GONE);
                gameNameText.setText("Explore");
                break;
            case GameManager.DEATHMATCH:
                bombButton.setVisibility(View.VISIBLE);
                boostButton.setVisibility(View.VISIBLE);
                fireButton.setVisibility(View.VISIBLE);
                myHPText.setVisibility(View.VISIBLE);
                healButton.setVisibility(View.GONE);
                myTeamText.setVisibility(View.GONE);
                gameNameText.setText(gameManager.getGameName());
                break;
            case GameManager.TEAMMATCH:
                bombButton.setVisibility(View.VISIBLE);
                boostButton.setVisibility(View.VISIBLE);
                fireButton.setVisibility(View.VISIBLE);
                myHPText.setVisibility(View.VISIBLE);
                healButton.setVisibility(View.VISIBLE);
                myTeamText.setVisibility(View.VISIBLE);
                gameNameText.setText(gameManager.getGameName());
                break;
        }
        player1Text.setVisibility(View.GONE);
        player2Text.setVisibility(View.GONE);
        player3Text.setVisibility(View.GONE);
        player4Text.setVisibility(View.GONE);

        myHeRosNameText.setText(gameManager.getHeRosName());

        // Update screen value
        switch(gameManager.getTeam()) {
            case 0:
                myTeamText.setTextColor(Color.rgb(0, 255, 0));
                myHeRosNameText.setTextColor(Color.rgb(0, 255, 0));
                myTeamText.setText("Green Team");
                break;
            case 1:
                myTeamText.setTextColor(Color.rgb(0, 0, 255));
                myHeRosNameText.setTextColor(Color.rgb(0, 0, 255));
                myTeamText.setText("Blue Team");
                break;
            case 2:
                myTeamText.setTextColor(Color.rgb(255, 255, 255));
                myHeRosNameText.setTextColor(Color.rgb(255, 255, 255));
                myTeamText.setText("White Team");
                break;
        }

        gameManager.startPlaying(this);
        pololuSpeed = POLOLU_SPEED;
        pivotingSpeed = PIVOTING_SPEED;

    }

    public void updateHp(final String hp){
        this.runOnUiThread(new Runnable() {
            public void run() {
                myHPText.setText(hp);
                if (Integer.parseInt(hp) <= 20) {
                    myHPText.setTextColor(Color.rgb(255, 0, 0));
                } else {
                    myHPText.setTextColor(Color.rgb(0, 255, 0));
                }
            }
        });
    }

    public void updateGamePlayerInfo(final ArrayList<Integer> hp,final ArrayList<String> herosNames, final ArrayList<Integer> teams){
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (!herosNames.get(0).equals("")) {
                    player1Text.setText(herosNames.get(0) + ": " + hp.get(0));
                    player1Text.setTextColor(Color.rgb(0, (1 - teams.get(0)) * 255, teams.get(0) * 255));
                    player1Text.setVisibility(View.VISIBLE);
                } else {
                    player1Text.setVisibility(View.GONE);
                }

                if (!herosNames.get(1).equals("")) {
                    player2Text.setText(herosNames.get(1) + ": " + hp.get(1));
                    player2Text.setTextColor(Color.rgb(0, (1 - teams.get(1)) * 255, teams.get(1) * 255));
                    player2Text.setVisibility(View.VISIBLE);
                } else {
                    player2Text.setVisibility(View.GONE);
                }

                if (!herosNames.get(2).equals("")) {
                    player3Text.setText(herosNames.get(2) + ": " + hp.get(2));
                    player3Text.setTextColor(Color.rgb(0, (1 - teams.get(2)) * 255, teams.get(2) * 255));
                    player3Text.setVisibility(View.VISIBLE);
                } else {
                    player3Text.setVisibility(View.GONE);
                }

                if (!herosNames.get(3).equals("")) {
                    player4Text.setText(herosNames.get(3) + ": " + hp.get(3));
                    player4Text.setTextColor(Color.rgb(0, (1 - teams.get(3)) * 255, teams.get(3) * 255));
                    player4Text.setVisibility(View.VISIBLE);
                } else {
                    player4Text.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.return_button:
                exitActivity();
                break;
            case R.id.fire_direct_button:
                sendFireMessage();
                break;
            case R.id.fire_global_button:
                sendBombMessage();
                break;
            case R.id.heal_button:
                sendHealMessage();
                break;
            case R.id.boost_button:
                sendBoostMessage();
                break;
        }
    }

    // Link background UI and jpeg stream
    public void setJpegStream(JpegViewStream mjv) {
        videoSurface.addView(mjv);
    }

    @Override
    public void onValueChanged(int angle, int power, int direction) {
        // TODO Auto-generated method stub

        final byte[] toSend = new byte[4];
        if (lastAngleSent == -1 | lastPowerSent == -1) {
            lastAngleSent = angle;
            lastPowerSent = power;
        } else if (Math.abs(angle - lastAngleSent) >= 5 | Math.abs(power - lastPowerSent) >= 5) {
            switch (direction) {
                case JoystickView.FRONT:
                    toSend[0] = '$';
                    toSend[1] = 'z';
                    toSend[2] = (byte) (int) Math.min(pololuSpeed, Math.floor(power * pololuSpeed / 100));
                    toSend[3] = 0;
                    break;
                case JoystickView.FRONT_RIGHT:
                    toSend[0] = '$';
                    toSend[1] = 'd';
                    toSend[2] = (byte) (int) Math.min(pololuSpeed, Math.floor(power * pololuSpeed / 100));
                    toSend[3] = (byte) (int) Math.min(pololuSpeed, Math.floor(31 * (Math.abs(angle) - 15) / 60));
                    break;
                case JoystickView.RIGHT:
                    toSend[0] = '$';
                    toSend[1] = 'e';
                    toSend[2] = (byte) (int) Math.min(pivotingSpeed, Math.floor(power * pivotingSpeed / 100));
                    toSend[3] = 0;
                    break;
                case JoystickView.BACK_RIGHT:
                    toSend[0] = '$';
                    toSend[1] = 'c';
                    toSend[2] = (byte) (int) Math.min(pololuSpeed, Math.floor(power * pololuSpeed / 100));
                    toSend[3] = (byte) (int) Math.min(pololuSpeed, Math.floor(31 * (60 - (Math.abs(angle) - 105)) / 60));
                    break;
                case JoystickView.BACK:
                    toSend[0] = '$';
                    toSend[1] = 's';
                    toSend[2] = (byte) (int) Math.min(pololuSpeed, Math.floor(power * pololuSpeed / 100));
                    toSend[3] = 0;
                    break;
                case JoystickView.BACK_LEFT:
                    toSend[0] = '$';
                    toSend[1] = 'w';
                    toSend[2] = (byte) (int) Math.min(pololuSpeed, Math.floor(power * pololuSpeed / 100));
                    toSend[3] = (byte) (int) Math.min(pololuSpeed, Math.floor(31 * (60 - (Math.abs(angle) - 105)) / 60));
                    break;
                case JoystickView.LEFT:
                    toSend[0] = '$';
                    toSend[1] = 'a';
                    toSend[2] = (byte) (int) Math.min(pivotingSpeed, Math.floor(power * pivotingSpeed / 100));
                    toSend[3] = 0;
                    break;
                case JoystickView.FRONT_LEFT:
                    toSend[0] = '$';
                    toSend[1] = 'q';
                    toSend[2] = (byte) (int) Math.min(pololuSpeed, Math.floor(power * pololuSpeed / 100));
                    toSend[3] = (byte) (int) Math.min(pololuSpeed, Math.floor(31 * (Math.abs(angle) - 15) / 60));
                    break;
                default:
                    toSend[0] = '$';
                    toSend[1] = 'r';
                    toSend[2] = 0;
                    toSend[3] = 0;
                    // Preference to be in a controlled state
                    gameManager.sendPriorityMessage(toSend, 4, 20);
            }

            gameManager.sendMessage(toSend, 4, 5);

            lastAngleSent = angle;
            lastPowerSent = power;
        }

    }


    public void sendFireMessage() {
        byte[] toSend = new byte[2];
        toSend[0] = '$';
        toSend[1] = 'u';

        gameManager.sendMessage(toSend, 2, 30);
    }


    public void sendBombMessage() {
        byte[] toSend = new byte[2];
        toSend[0] = '$';
        toSend[1] = 'i';

        gameManager.sendMessage(toSend, 2, 30);

        if(HeRosApplication.soundOn) {
            Random r = new Random();
            int i = r.nextInt(5);
            switch (i) {
                case 0:
                    timeBomb.start();
                    break;
                case 1:
                    targetAcquired.start();
                    break;
                case 2:
                    thereYouAre.start();
                    break;
                case 3:
                    iSeeYou.start();
                    break;
                case 4:
                    laserMachineGun.start();
                    break;
            }
        }
        bombButton.setVisibility(View.INVISIBLE);
        // Thread that will re-enable button after wait
        new WaitVisibleThread(this, bombButton, BOMB_DELAY).start();
    }

    public void sendHealMessage() {
        byte[] toSend = new byte[2];
        toSend[0] = '$';
        toSend[1] = 'o';

        gameManager.sendMessage(toSend, 2, 30);

        Random r = new Random();
        int i = r.nextInt(2);

        switch (i) {
            case 0:
                excited.start();
                break;
            case 1:
                alarm.start();
                break;
        }

        healButton.setVisibility(View.INVISIBLE);
        new WaitVisibleThread(this, healButton, HEAL_DELAY).start();
    }

    public void sendBoostMessage() {
        final byte[] toSend = new byte[4];
        // Start Boost
        if (pololuSpeed == POLOLU_SPEED && pivotingSpeed == PIVOTING_SPEED) {
            pololuSpeed = POLOLU_SPEED + POLOLU_BOOST;
            pivotingSpeed = PIVOTING_SPEED + PIVOTING_BOOST;

            // Sound
            if(HeRosApplication.soundOn)
                surprised.start();

            boostButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
            // Stop boost automatically after BOOST_USE
            new BoostThread(this).start();


        // Boost in use : stop it
        } else {
            pololuSpeed = POLOLU_SPEED;
            pivotingSpeed = PIVOTING_SPEED;
        }
    }

    protected class BoostThread extends Thread {
        private Activity activity;
        private final int SAMPLE = 10;

        public BoostThread(Activity activity){
            this.activity = activity;
        }

        public void run(){
            // Wait end of availability of boost
            for(int i=0; i<SAMPLE; i++) {
                // If boost ended : exit the loop
                if(pololuSpeed == POLOLU_SPEED || pivotingSpeed == PIVOTING_SPEED){
                    break;
                }
                try {
                    Thread.sleep(BOOST_USE / SAMPLE);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // Stop boost
            pololuSpeed = POLOLU_SPEED;
            pivotingSpeed = PIVOTING_SPEED;
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    boostButton.setVisibility(View.INVISIBLE);
                    boostButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_boost));
                }
            });
            // Wait time delay before re-enable the boost
            try {
                Thread.sleep(BOOST_DELAY);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    boostButton.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            exitActivity();
        }
        return false;
    }

    private void exitActivity(){
        finish();
        gameManager.stopPlaying();
        Intent menuIntent = new Intent(this, MainMenuActivity.class);
        menuIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(menuIntent);
    }

    public void endOfGame(boolean win){
        gameManager.stopPlaying();
        AlertDialog.Builder dlgAlertBuilder = new AlertDialog.Builder(this);
        if(win)
            dlgAlertBuilder.setMessage("You have won!");
        else
            dlgAlertBuilder.setMessage("Oh nooo, you have lost...");
        dlgAlertBuilder.setTitle("Game over");
        dlgAlertBuilder.setCancelable(true);
        dlgAlertBuilder.setPositiveButton("Back to Menu",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                        exitActivity();
                    }
                });
        dlgAlert = dlgAlertBuilder.create();
        dlgAlert.show();
    }


}

