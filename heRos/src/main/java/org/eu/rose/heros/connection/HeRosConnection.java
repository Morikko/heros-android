package org.eu.rose.heros.connection;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.media.MediaPlayer;
import android.widget.Toast;

import org.eu.rose.heros.R;
import org.eu.rose.heros.activity.GameActivity;
import org.eu.rose.heros.activity.ScanActivity;
import org.eu.rose.heros.application.HeRosApplication;
import org.eu.rose.heros.heRos.GetIPThread;
import org.eu.rose.heros.heRos.ReceptionPacketThread;
import org.eu.rose.heros.heRos.ReverseHeRosPolling;
import org.eu.rose.heros.manager.GameManager;
import org.eu.rose.heros.parse.GamePollingThread;
import org.eu.rose.heros.parse.ReverseParsePolling;
import org.eu.rose.heros.parse.SendingGameInfoThread;

/**
 * Handle all the permanent connection between HeRos <-> Phone <-> Parse
 * Keep information about the connected HeRos
 * 3 States
 * > Not Connected
 * > Connected (HeRos identified)
 * > InGame
 */
public class HeRosConnection {

    public static final int HEROS_TCP_PORT = 40008;
    public static final int HEROS_UDP_PORT = 40008;
    public static final int HEROS_BROADCAST_PORT = 55555;
    public static final int HEROS_RT_PORT = 0;

    private DatagramSocket udpSocket = null;
    private HeRosApplication applicationContext;

    /* Connection Part */
    private InetAddress HeRos_IP_ADDRESS;
    private String HeRos_Name;
    private String HeRos_MAC_ADDRESS;
    // List with informations about the connected HeRos (Address MAC, Name, Characteristics)
    private List<HashMap<String, Object>> herosInfos;

    private PollingThread pollingServer;
    private PollingThread pollingHeRos;

    private MediaPlayer thyBidding;
    /* End Part */

    /* Game part */
    private BlockingQueue<Byte> messageQueue;

    private GamePollingThread gamePollingThread;
    private ReceptionPacketThread receptionPacketThread;
    private SendingGameInfoThread sendingGameInfoThread;
    private SendUDPMessageThread realTimeUDPSendingThread;
    /* End Game Part */

    public HeRosConnection(HeRosApplication applicationContext) {
        this.applicationContext = applicationContext;
        HeRos_IP_ADDRESS = null;
    }

    // Send a message all the times
    public void sendPriorityUdpMessage(byte[] message, int size, int times){
        (new SendUDPMessageThread(udpSocket, new DatagramPacket(message, size, HeRos_IP_ADDRESS, HEROS_UDP_PORT), times)).start();
    }

    // Send messages max times and stop if a new call is made
    public void sendUdpMessage(byte[] message, int size, int times){
        // If still in run, stop it
        if(realTimeUDPSendingThread != null && realTimeUDPSendingThread.getState() == Thread.State.RUNNABLE)
            realTimeUDPSendingThread.stopSending();

        realTimeUDPSendingThread = new SendUDPMessageThread(udpSocket, new DatagramPacket(message, size, HeRos_IP_ADDRESS, HEROS_UDP_PORT), times);
        realTimeUDPSendingThread.start();
    }

    /************************
     * GAME COMMUNICATION
     *************************/
    public void startGameCommunication(GameManager gameManager, GameActivity activity, String currentGameName, int gameType){
        messageQueue = new ArrayBlockingQueue<Byte>(100);
//        if(gamePollingThread.isAlive())
//            gamePollingThread.stopPolling();
        // Information about game
        if(gameType != GameManager.EXPLORE) {
            gamePollingThread = new GamePollingThread(gameManager, currentGameName);
            gamePollingThread.start();
            sendingGameInfoThread = new SendingGameInfoThread(activity, currentGameName, HeRos_MAC_ADDRESS, messageQueue);
            sendingGameInfoThread.start();
        }else{
            gamePollingThread = null;
            sendingGameInfoThread = null;
        }

        receptionPacketThread = new ReceptionPacketThread(udpSocket, messageQueue);
        receptionPacketThread.start();


    }

    public void stopGameCommunication(){
        if(receptionPacketThread != null)
            receptionPacketThread.stopPolling();
        if(gamePollingThread != null)
            gamePollingThread.stopPolling();
        if(sendingGameInfoThread != null)
            sendingGameInfoThread.stopPolling();

        messageQueue.clear();
    }

    /************************
     * CONNECTION
     *************************/
    public boolean connecToHeRos() {
        if(HeRos_MAC_ADDRESS == null){
            applicationContext.getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(applicationContext.getCurrentActivity(), "Connection failed (NO MAC applied), please try again.", Toast.LENGTH_SHORT)
                            .show();
                }
            });
            return false;
        }

        Thread getIPThread = new Thread(new GetIPThread(HeRos_MAC_ADDRESS));
        getIPThread.start();

        // Wait the IP
        try {
            Thread.sleep(400);
            getIPThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Failed to get IP
        if(HeRos_IP_ADDRESS == null){
            applicationContext.getCurrentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(applicationContext.getCurrentActivity(), "Connection failed (NO IP found), please try again.", Toast.LENGTH_SHORT)
                            .show();
                }
            });
            return false;
        }

        // Open UDP connection for streaming
        try {
            udpSocket = new DatagramSocket(HEROS_UDP_PORT);
            // Time out ???
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Open TCP conenction

        // Start polling to HeRos and Server
        pollingHeRos = new ReverseHeRosPolling(udpSocket);
        pollingHeRos.start();
        pollingServer = new ReverseParsePolling(HeRos_MAC_ADDRESS);
        pollingServer.start();

        // Play a sound
        if(HeRosApplication.soundOn) {
            thyBidding = MediaPlayer.create(applicationContext.getCurrentActivity(), R.raw.thy_bidding);
            thyBidding.start();
        }

        return true;
    }

    public void disconnect() {
        if(pollingHeRos != null)
            pollingHeRos.stopPolling();
        if(pollingServer != null)
            pollingServer.stopPolling();

        // Wait end of connection
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (udpSocket != null) {
            // Sending 1000 times closing packet and next close the socket
            new Thread(new Runnable() {
                public void run() {
                    byte[] toSend = new byte[4];
                    toSend[0] = '$';
                    toSend[1] = 'k';
                    toSend[2] = 0;
                    toSend[3] = 0;

                    DatagramPacket sendingPacket = new DatagramPacket(toSend, 4, HeRos_IP_ADDRESS, HEROS_UDP_PORT);
                    try {
                        for (int i = 0; i < 1000; i++) {
                            udpSocket.send(sendingPacket);
                        }
                        udpSocket.close();

                        HeRos_IP_ADDRESS = null;
                        HeRos_MAC_ADDRESS = null;
                        HeRos_Name = null;
                        //HeRos_RT_PORT = 0;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }).start();

        }
    }

    /************************
     * GETTER AND SETTER
     *************************/

    public InetAddress getHeRosIP() {
        return HeRos_IP_ADDRESS;
    }

    public void setHeRosIP(String ip) {
        // Create new adress
        try {
            if(ip != null)
                HeRos_IP_ADDRESS = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public String getHeRosName() {
        return HeRos_Name;
    }

    public void setHeRosName(int index) {
        HeRos_Name = herosInfos.get(index).get("name").toString();
    }

    public void setHeRosName(String name) {
        HeRos_Name = name;
    }

    public String getHeRosMacAddress() {
        return HeRos_MAC_ADDRESS;
    }

    public void setHeRosMacAddress(int index) {
        HeRos_MAC_ADDRESS = herosInfos.get(index).get("macAddress").toString();;
    }

    public void setHeRosInfos(List<HashMap<String, Object>> herosInfos){
        this.herosInfos = herosInfos;
    }

    public ReceptionPacketThread getReceptionPacketThread(){
        return receptionPacketThread;
    }
}
