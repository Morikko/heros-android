package org.eu.rose.heros.manager;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.widget.Toast;

import org.eu.rose.heros.application.HeRosApplication;
import org.eu.rose.heros.stream.JpegViewStream;
import org.eu.rose.heros.R;
import org.eu.rose.heros.activity.GameActivity;
import org.eu.rose.heros.connection.HeRosConnection;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by eric on 21/06/15.
 * General class to be the bridge between the connection part and the UI part
 * Keep all the information for the party
 */
public class GameManager {

    public static final int DEATHMATCH = 0;
    public static final int TEAMMATCH = 1;
    public static final int EXPLORE = 2;

    private HeRosConnection heRosConnection;
    private GameActivity gameActivity;

    private JpegViewStream jpegStreaming;

    private int gameType, team, address;
    private String gameName;

    private MediaPlayer laugh;
    private Vibrator vibrator;

    private long gameStartTime;

    private MediaPlayer evilRobot;
    private MediaPlayer isAnyoneThere;
    private MediaPlayer hereICome;
    private MediaPlayer hereWeGo;
    private MediaPlayer iAmSoReady;
    private MediaPlayer iAmTheMan;
    private MediaPlayer itIsMeAgain;
    private MediaPlayer whereAreWeGoing;

    private boolean isPlaying;

    public GameManager(HeRosConnection heRosConnection){
        this.heRosConnection = heRosConnection;
        isPlaying = false;
    }

    /**
     * Ask the UI to update display information and control the victory
     * @param hp
     * @param winners
     * @param herosNames
     * @param teams
     */
    public void updateGameState(ArrayList<Integer> hp, ArrayList<String> winners, ArrayList<String> herosNames, final ArrayList<Integer> teams){
        gameActivity.updateGamePlayerInfo(hp, herosNames, teams);

        // Get win
        for (int i = 0; i < winners.size(); i++) {
            if (winners.get(i).equals(getHeRosName())) {
                laugh.start();
//                Toast.makeText(gameActivity, "You won!", Toast.LENGTH_SHORT)
//                        .show();
                vibrator.vibrate(500);
                // Stop polling
                gameActivity.endOfGame(true);
            }
        }
        for (int i = 0; i < hp.size(); i++) {
            if (hp.get(i) == 0 && herosNames.get(i).equals(getHeRosName())) {
                laugh.start();
//                Toast.makeText(gameActivity, "You won!", Toast.LENGTH_SHORT)
//                        .show();
                vibrator.vibrate(500);
                // Stop polling
                // Lost a party
                byte[] toSend = new byte[2];
                toSend[0] = '$';
                toSend[1] = 'l';
                heRosConnection.sendUdpMessage(toSend, 2, 30);
                gameActivity.endOfGame(false);
            }
        }

    }

    // Save the main information about the game
    public void setGameConfiguration(String gameName, int address, int team, int type){
        this.gameName = gameName;
        this.address = address;
        this.team = team;
        this.gameType = type;
    }

    public void startPlaying(GameActivity gameActivity) {
        isPlaying = true;
        this.gameActivity = gameActivity;

        evilRobot = MediaPlayer.create(gameActivity, R.raw.evil_robot);
        isAnyoneThere = MediaPlayer.create(gameActivity, R.raw.is_anyone_there);
        hereICome = MediaPlayer.create(gameActivity, R.raw.here_i_come);
        hereWeGo = MediaPlayer.create(gameActivity, R.raw.here_we_go);
        iAmSoReady = MediaPlayer.create(gameActivity, R.raw.i_am_so_ready);
        iAmTheMan = MediaPlayer.create(gameActivity, R.raw.i_am_the_man);
        itIsMeAgain = MediaPlayer.create(gameActivity, R.raw.it_is_me_again);
        whereAreWeGoing = MediaPlayer.create(gameActivity, R.raw.where_are_we_going);

        vibrator = (Vibrator) gameActivity.getSystemService(Context.VIBRATOR_SERVICE);
        laugh = MediaPlayer.create(gameActivity, R.raw.r2d2_laughing);

        // Start thread
        heRosConnection.startGameCommunication(this, gameActivity, gameName, gameType);

        jpegStreaming = new JpegViewStream(gameActivity, heRosConnection.getReceptionPacketThread());

        // Link ui and stream
        gameActivity.setJpegStream(jpegStreaming);
        // Start Stream
        jpegStreaming.start();
        jpegStreaming.setDisplayMode(JpegViewStream.SIZE_BEST_FIT);
        jpegStreaming.showFps(true);

        gameStartTime = System.currentTimeMillis();

        // Start Camera
        byte[] toSend = new byte[6];
        toSend[0] = '$';
        toSend[1] = 'm';
        toSend[2] = (byte) address;
        toSend[3] = (byte) team;
        toSend[4] = '$';
        toSend[5] = 'n';

        //while(camera_started)
        heRosConnection.sendPriorityUdpMessage(toSend, 6, 100);

        if(HeRosApplication.soundOn) {
            // Starting sound
            Random r = new Random();
            int i = r.nextInt(8);

            switch (i) {
                case 0:
                    evilRobot.start();
                    break;
                case 1:
                    isAnyoneThere.start();
                    break;
                case 2:
                    hereICome.start();
                    break;
                case 3:
                    hereWeGo.start();
                    break;
                case 4:
                    iAmSoReady.start();
                    break;
                case 5:
                    iAmTheMan.start();
                    break;
                case 6:
                    itIsMeAgain.start();
                    break;
                case 7:
                    whereAreWeGoing.start();
                    break;
            }
        }
    }

    public void stopPlaying() {
        isPlaying = false;
        jpegStreaming.stopPlayback();

        heRosConnection.stopGameCommunication();

        // Send a lot of messages asking for stopping the robot, avoid craey robot
        byte[] toSend = new byte[2];
        toSend[0] = '$';
        toSend[1] = 'r';
        heRosConnection.sendUdpMessage(toSend, 2, 1000);
    }

    public void sendMessage(byte[] message, int size, int times){
        heRosConnection.sendUdpMessage(message, size, times);
    }

    public void sendPriorityMessage(byte[] message, int size, int times){
        heRosConnection.sendPriorityUdpMessage(message, size, times);
    }

    public int getTeam() {
        return team;
    }

    public int getGameType() {
        return gameType;
    }

    public String getGameName() {
        return gameName;
    }

    public String getHeRosName() {
        return heRosConnection.getHeRosName();
    }

    public long getGameStartTime() {
        return gameStartTime;
    }
}
